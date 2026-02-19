package tr.akguel.interceptor

import org.omg.CORBA.BAD_PARAM
import org.omg.CORBA.LocalObject
import org.omg.PortableInterceptor.ForwardRequest
import org.omg.PortableInterceptor.ServerRequestInfo
import org.omg.PortableInterceptor.ServerRequestInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tr.akguel.api.MonitorApiClient
import tr.akguel.api.TrafficEvent
import tr.akguel.config.MonitorConfig
import tr.akguel.interceptor.RequestTimingStore.getAndRemoveStartTime
import java.net.InetAddress
import java.util.*
import kotlin.math.min

/**
 * Server-side Portable Interceptor.
 *
 * Intercepts all incoming CORBA requests on the supplier side.
 * Captures the server's view of the interaction.
 */
class ServerInterceptor : LocalObject(), ServerRequestInterceptor {

    private val config: MonitorConfig = MonitorConfig.instance!!
    private val apiClient: MonitorApiClient = MonitorApiClient.instance!!
    private val localHost: String?

    init {
        var host: String?
        try {
            host = InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            host = "unknown"
        }
        this.localHost = host

        log.info("ServerInterceptor initialized")
    }

    override fun name(): String {
        return "CORBAMonitorServerInterceptor"
    }

    private fun shouldSkip(ri: ServerRequestInfo): Boolean {
        try {
            val op = ri.operation()
            return op != null && SKIP_OPERATIONS.contains(op)
        } catch (e: java.lang.Exception) {
            return false
        }
    }

    /**
     * Called when a request arrives at the server, before dispatch.
     */
    override fun receive_request_service_contexts(ri: ServerRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            RequestTimingStore.setStartTime(ri.request_id().toString().toByteArray(Charsets.UTF_8), System.nanoTime())
        } catch (e: Exception) {
            log.debug("Error storing start time: {}", e.message)
        }
    }

    /**
     * Called after service contexts are processed, with full request info.
     */
    @Throws(ForwardRequest::class)
    override fun receive_request(ri: ServerRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            val event: TrafficEvent = buildBaseEvent(ri, "receive_request")
                .direction("request")
                .status("success")
                .targetHost(localHost)
                .messageType("Request")

            // Capture arguments
            if (config.captureRequestData) {
                event.requestData(extractArguments(ri))
            }

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in receive_request interceptor: {}", e.message)
        }
    }

    /**
     * Called after the servant has processed the request, before sending reply.
     */
    override fun send_reply(ri: ServerRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            val latency: Double? = calculateLatency(ri.request_id().toString().toByteArray(Charsets.UTF_8))

            val event: TrafficEvent = buildBaseEvent(ri, "send_reply")
                .direction("reply")
                .status("success")
                .latencyMs(latency)
                .targetHost(localHost)
                .messageType("Reply")

            if (config.captureResponseData) {
                event.responseData(extractResult(ri))
            }

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in send_reply interceptor: {}", e.message)
        }
    }

    /**
     * Called when the servant throws an exception.
     */
    @Throws(ForwardRequest::class)
    override fun send_exception(ri: ServerRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            val latency: Double? = calculateLatency(ri.request_id().toString().toByteArray(Charsets.UTF_8))

            var exceptionId: String? = "UNKNOWN"
            try {
                exceptionId = ri.sending_exception().type().id()
            } catch (ignored: Exception) {
            }

            val event: TrafficEvent = buildBaseEvent(ri, "send_exception")
                .direction("reply")
                .status("exception")
                .latencyMs(latency)
                .errorMessage(exceptionId)
                .exceptionType("SYSTEM_EXCEPTION")
                .targetHost(localHost)
                .messageType("Reply")

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in send_exception interceptor: {}", e.message)
        }
    }

    @Throws(ForwardRequest::class)
    override fun send_other(ri: ServerRequestInfo?) {
        // Location forward or similar
    }

    override fun destroy() {
        log.info("ServerInterceptor destroyed")
    }

    // ─── Helpers ──────────────────────────────────────────────────────
    private fun buildBaseEvent(ri: ServerRequestInfo, interceptorPoint: String?): TrafficEvent {
        val event = TrafficEvent()
            .requestId(formatRequestId(ri.request_id().toString().toByteArray(Charsets.UTF_8)))
            .operation(ri.operation())
            .interceptorPoint(interceptorPoint)
            .giopVersion("1.2")

        // Try to get adapter/object info
        try {
            val adapterId = ri.adapter_id()
            val objectId = ri.object_id()
            if (objectId != null && objectId.isNotEmpty()) {
                event.targetServiceName(String(objectId).trim { it <= ' ' })
            }
        } catch (ignored: Exception) {
        }

        // Repository ID from target's most-derived interface
        try {
            val ids =
                ri.target_most_derived_interface().split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (ids.isNotEmpty()) {
                event.repositoryId(ri.target_most_derived_interface())
                val name = ids[ids.size - 1]
                val colon = name.indexOf(':')
                event.interfaceName(if (colon > 0) name.substring(0, colon) else name)
            }
        } catch (ignored: Exception) {
        }

        return event
    }

    private fun extractArguments(ri: ServerRequestInfo): MutableMap<String, Any> {
        val args: MutableMap<String, Any> = LinkedHashMap<String, Any>()
        try {
            val params = ri.arguments()
            if (params != null) {
                args["_idl_type"] = ri.operation()
                val paramMap: MutableMap<String, Any> = LinkedHashMap<String, Any>()
                for (i in params.indices) {
                    paramMap["arg$i"] = "<captured>"
                }
                args["params"] = paramMap
            }
        } catch (ignored: BAD_PARAM) {
        } catch (e: Exception) {
            args["_note"] = e.message ?: "Error capturing arguments"
        }
        return args
    }

    private fun extractResult(ri: ServerRequestInfo?): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = LinkedHashMap<String, Any>()
        try {
            result["_note"] = "Server-side result captured"
        } catch (ignored: Exception) {
        }
        return result
    }

    private fun formatRequestId(requestId: ByteArray?): String {
        if (requestId == null || requestId.isEmpty()) return UUID.randomUUID().toString()
        val sb = StringBuilder()
        for (i in 0..<min(requestId.size, 16)) {
            sb.append(String.format("%02x", requestId[i].toInt() and 0xFF))
        }
        return sb.toString()
    }

    private fun calculateLatency(requestId: ByteArray?): Double? {
        val id: String = formatRequestId(requestId)
        val startNanos = getAndRemoveStartTime(id)
        if (startNanos != null) {
            return (System.nanoTime() - startNanos) / 1000000.0
        }
        return null
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ServerInterceptor::class.java)

        /**
         * Operations to skip (CORBA infrastructure / Naming Service internals).
         */
        val SKIP_OPERATIONS: MutableSet<String> = mutableSetOf<String>(
            "_is_a", "_non_existent", "_get_interface_def", "_get_component",
            "_get_domain_managers", "_get_policy", "_repository_id",
            "resolve", "resolve_str", "bind", "rebind", "unbind",
            "bind_context", "rebind_context", "bind_new_context",
            "list", "to_name", "to_string", "destroy", "new_context", "to_url"
        )
    }
}