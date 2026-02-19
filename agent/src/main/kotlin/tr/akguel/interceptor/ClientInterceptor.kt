package tr.akguel.interceptor

import org.omg.CORBA.Any
import org.omg.CORBA.BAD_PARAM
import org.omg.CORBA.LocalObject
import org.omg.CORBA.TCKind
import org.omg.CORBA.portable.ObjectImpl
import org.omg.PortableInterceptor.ClientRequestInfo
import org.omg.PortableInterceptor.ClientRequestInterceptor
import org.omg.PortableInterceptor.ForwardRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tr.akguel.api.MonitorApiClient
import tr.akguel.api.TrafficEvent
import tr.akguel.config.MonitorConfig
import java.net.InetAddress
import java.util.*
import kotlin.math.min


/**
 * Client-side Portable Interceptor.
 *
 * Intercepts all outgoing CORBA requests and their replies/exceptions
 * on the consumer side. Captures:
 * - Operation name, interface, repository ID
 * - Target IOR host/port
 * - Request/reply timing (latency)
 * - Arguments and return values (when configured)
 * - CORBA system/user exceptions
 */
class ClientInterceptor(private val slotId: Int) : LocalObject(), ClientRequestInterceptor {

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

        log.info("ClientInterceptor initialized (slot={})", slotId)
    }

    override fun name(): String {
        return "CORBAMonitorClientInterceptor"
    }


    /**
     * Check if this request should be skipped (infrastructure call).
     */
    private fun shouldSkip(ri: ClientRequestInfo): Boolean {
        try {
            // Skip by operation name
            val op = ri.operation()
            if (op != null && SKIP_OPERATIONS.contains(op)) {
                return true
            }

            // Skip by target interface (if available)
            try {
                val target = ri.target()
                if (target != null) {
                    val ids = (target as ObjectImpl)._ids()
                    if (ids != null) {
                        for (id in ids) {
                            for (fragment in SKIP_INTERFACE_FRAGMENTS) {
                                if (id.contains(fragment)) {
                                    return true
                                }
                            }
                        }
                    }
                }
            } catch (ignored: java.lang.Exception) {
                // Can't determine target — don't skip, but be safe in buildBaseEvent
            }
        } catch (ignored: java.lang.Exception) {
        }

        return false
    }

    /**
     * Called before a request is sent to the server.
     * We record the start time and capture request parameters.
     */
    @Throws(ForwardRequest::class)
    override fun send_request(ri: ClientRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            // Store start time for latency calculation
            val startNanos = System.nanoTime()
            RequestTimingStore.setStartTime(ri.request_id().toString().toByteArray(Charsets.UTF_8), startNanos)

            // Build and send event
            val event: TrafficEvent = buildBaseEvent(ri, "send_request")
                .direction("request")
                .status("success")
                .messageType("Request")

            // Capture request arguments
            if (config.captureRequestData) {
                event.requestData(extractArguments(ri))
            }

            apiClient.submit(event)
        } catch (e: Exception) {
            // Never let monitoring break the actual CORBA call
            log.debug("Error in send_request interceptor: {}", e.message)
        }
    }

    /**
     * Called when a reply is received from the server.
     */
    override fun receive_reply(ri: ClientRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            val latency: Double? = calculateLatency(ri.request_id().toString().toByteArray(Charsets.UTF_8))

            val event: TrafficEvent = buildBaseEvent(ri, "receive_reply")
                .direction("reply")
                .status("success")
                .latencyMs(latency)
                .messageType("Reply")

            // Capture response
            if (config.captureResponseData) {
                event.responseData(extractResult(ri))
            }

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in receive_reply interceptor: {}", e.message)
        }
    }

    /**
     * Called when a system exception is received.
     */
    @Throws(ForwardRequest::class)
    override fun receive_exception(ri: ClientRequestInfo) {
        if (shouldSkip(ri)) return;

        try {
            val latency: Double? = calculateLatency(ri.request_id().toString().toByteArray(Charsets.UTF_8))

            var exceptionId = "UNKNOWN"
            try {
                exceptionId = ri.received_exception_id()
            } catch (ignored: Exception) {
            }

            // Parse exception type from repository ID
            // e.g., "IDL:omg.org/CORBA/TRANSIENT:1.0" → "CORBA::TRANSIENT"
            val exceptionType: String = parseExceptionType(exceptionId)
            val errorMessage = "CORBA::" + exceptionType

            val event: TrafficEvent = buildBaseEvent(ri, "receive_exception")
                .direction("reply")
                .status(
                    if (exceptionType.contains("TRANSIENT") || exceptionType.contains("TIMEOUT"))
                        "timeout"
                    else
                        "exception"
                )
                .latencyMs(latency)
                .errorMessage(errorMessage)
                .exceptionType(
                    if (exceptionId.contains("USER_EXCEPTION"))
                        "USER_EXCEPTION"
                    else
                        "SYSTEM_EXCEPTION"
                )
                .messageType("Reply")

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in receive_exception interceptor: {}", e.message)
        }
    }

    /**
     * Called when the request results in a location forward (redirect).
     */
    @Throws(ForwardRequest::class)
    override fun receive_other(ri: ClientRequestInfo) {
        if (shouldSkip(ri)) return

        try {
            val event: TrafficEvent = buildBaseEvent(ri, "receive_other")
                .direction("reply")
                .status("success")
                .messageType("LocateReply")

            apiClient.submit(event)
        } catch (e: Exception) {
            log.debug("Error in receive_other interceptor: {}", e.message)
        }
    }

    override fun send_poll(ri: ClientRequestInfo?) {
        // Not commonly used
    }

    override fun destroy() {
        log.info("ClientInterceptor destroyed")
    }

    // ─── Helper Methods ───────────────────────────────────────────────
    /**
     * Build a base TrafficEvent with common fields from the RequestInfo.
     */
    private fun buildBaseEvent(ri: ClientRequestInfo, interceptorPoint: String?): TrafficEvent {
        val event = TrafficEvent()
            .requestId(formatRequestId(ri.request_id().toString().toByteArray(Charsets.UTF_8)))
            .operation(ri.operation())
            .interceptorPoint(interceptorPoint)
            .sourceHost(localHost)
            .giopVersion("1.2")

        // Extract interface/repository info
        try {
            val target = ri.target()
            if (target != null) {
                try {
                    val ids = (target as ObjectImpl)
                        ._ids()
                    if (ids != null && ids.size > 0) {
                        event.repositoryId(ids[0])
                        event.interfaceName(parseInterfaceName(ids[0]))
                    }
                } catch (ignored: Exception) {
                }
            }
        } catch (ignored: Exception) {
        }

        // Extract target host/port from effective_target IOR
        try {
            val effectiveTarget = ri.effective_target()
            if (effectiveTarget != null) {
                val ior: String = effectiveTarget.toString()
                parseIORAddress(ior, event)
            }
        } catch (ignored: Exception) {
        }

        // Service contexts
        try {
            val ctxData: MutableMap<String, kotlin.Any> = extractServiceContexts(ri)
            if (!ctxData.isEmpty()) {
                event.contextData(ctxData)
            }
        } catch (ignored: Exception) {
        }

        return event
    }

    /**
     * Extract arguments from the request (if available).
     */
    private fun extractArguments(ri: ClientRequestInfo): MutableMap<String, kotlin.Any> {
        val args: MutableMap<String, kotlin.Any> = LinkedHashMap<String, kotlin.Any>()
        try {
            val params = ri.arguments()
            if (params != null) {
                args["_idl_type"] = ri.operation().toString().toByteArray(Charsets.UTF_8)
                val paramMap: MutableMap<String, kotlin.Any> = LinkedHashMap<String, kotlin.Any>()
                for (i in params.indices) {
                    val key = "arg$i"
                    try {
                        paramMap[key] = anyToObject(params[i]!!.argument) ?: "<unavailable>"
                    } catch (e: Exception) {
                        paramMap[key] = "<unavailable>"
                    }
                }
                args["params"] = paramMap
            }
        } catch (bp: BAD_PARAM) {
            // Arguments not available at this interception point
            args["_note"] = "Arguments not available (BAD_PARAM)"
        } catch (e: Exception) {
            args["_note"] = "Could not extract arguments: " + e.message
        }
        return args
    }

    /**
     * Extract the return value from the reply.
     */
    private fun extractResult(ri: ClientRequestInfo): MutableMap<String, kotlin.Any> {
        val result: MutableMap<String, kotlin.Any> = LinkedHashMap<String, kotlin.Any>()
        try {
            val returnValue = ri.result()
            if (returnValue != null && returnValue.type().kind() != TCKind.tk_void && returnValue.type()
                    .kind() != TCKind.tk_null
            ) {
                result["_idl_type"] = ri.operation() + "::_return"
                result["return_value"] = anyToObject(returnValue) ?: "<unavailable>" // TODO: Handle null value properly
            }
        } catch (bp: BAD_PARAM) {
            // Result not available
        } catch (e: Exception) {
            result["_note"] = "Could not extract result: " + e.message
        }
        return result
    }

    /**
     * Convert a CORBA Any to a Java Object for JSON serialization.
     */
    private fun anyToObject(any: Any?): kotlin.Any? {
        if (any == null) return null

        try {
            val kind = any.type().kind()
            when (kind.value()) {
                TCKind._tk_boolean -> return any.extract_boolean()
                TCKind._tk_char -> return any.extract_char().toString()
                TCKind._tk_wchar -> return any.extract_wchar().toString()
                TCKind._tk_octet -> return any.extract_octet().toInt()
                TCKind._tk_short -> return any.extract_short()
                TCKind._tk_ushort -> return any.extract_ushort()
                TCKind._tk_long -> return any.extract_long()
                TCKind._tk_ulong -> return any.extract_ulong()
                TCKind._tk_longlong -> return any.extract_longlong()
                TCKind._tk_ulonglong -> return any.extract_ulonglong()
                TCKind._tk_float -> return any.extract_float()
                TCKind._tk_double -> return any.extract_double()
                TCKind._tk_string -> return any.extract_string()
                TCKind._tk_wstring -> return any.extract_wstring()
                TCKind._tk_enum -> return any.type().member_name(any.create_input_stream().read_long())
                TCKind._tk_void, TCKind._tk_null -> return null
                else -> {
                    // For complex types, return type info
                    val complex: MutableMap<String, kotlin.Any> = LinkedHashMap<String, kotlin.Any>()
                    complex["_type"] = any.type().name()
                    complex["_kind"] = kind.value()
                    complex["_id"] = any.type().id()
                    return complex
                }
            }
        } catch (e: Exception) {
            return "<" + e.javaClass.getSimpleName() + ">"
        }
    }

    /**
     * Extract service contexts from the request.
     */
    private fun extractServiceContexts(ri: ClientRequestInfo): MutableMap<String, kotlin.Any> {
        val contexts: MutableMap<String, kotlin.Any> = LinkedHashMap<String, kotlin.Any>()
        val contextIds = intArrayOf(0, 1, 6, 15, 0x4F545300) // CodeSets, BI_DIR_IIOP, SendingContextRunTime, TAO
        for (id in contextIds) {
            try {
                val sc = ri.get_request_service_context(id)
                if (sc != null && sc.context_data != null) {
                    contexts["ctx_$id"] = mapOf(
                        "context_id" to id,
                        "data_length" to sc.context_data.size
                    )
                }
            } catch (ignored: BAD_PARAM) {
                // Context not present
            }
        }
        return contexts
    }

    /**
     * Parse interface name from repository ID.
     * "IDL:FleetManagement/VehicleTracker:1.0" → "VehicleTracker"
     */
    private fun parseInterfaceName(repositoryId: String?): String? {
        if (repositoryId == null) return null
        try {
            var body: String? = repositoryId
            if (body.startsWith("IDL:")) body = body.substring(4)
            val colonIdx = body.lastIndexOf(':')
            if (colonIdx > 0) body = body.substring(0, colonIdx)
            val slashIdx = body.lastIndexOf('/')
            return if (slashIdx >= 0) body.substring(slashIdx + 1) else body
        } catch (e: Exception) {
            return repositoryId
        }
    }

    /**
     * Parse exception type from exception ID.
     */
    private fun parseExceptionType(exceptionId: String?): String {
        if (exceptionId == null) return "UNKNOWN"
        // "IDL:omg.org/CORBA/TRANSIENT:1.0" → "TRANSIENT"
        try {
            var body: String? = exceptionId
            val lastColon = body.lastIndexOf(':')
            if (lastColon > 0) body = body.substring(0, lastColon)
            val lastSlash = body.lastIndexOf('/')
            return if (lastSlash >= 0) body.substring(lastSlash + 1) else body
        } catch (e: Exception) {
            return exceptionId
        }
    }

    /**
     * Parse host and port from an IOR string.
     * This is a best-effort attempt for common IOR formats.
     */
    private fun parseIORAddress(ior: String?, event: TrafficEvent) {
        if (ior == null) return
        // JacORB's toString() may give "IOR:..." or the corbaloc form
        // We try to extract from the object reference implementation
        try {
            if (ior.contains("://")) {
                // corbaloc format: corbaloc::host:port/...
                val parts = ior.split("://".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size > 1) {
                    val hostPort = parts[1].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    val hp = hostPort.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (hp.size >= 1) event.targetHost(hp[0])
                    if (hp.size >= 2) event.targetPort(hp[1].toInt())
                }
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * Format request ID bytes as a short hex string.
     */
    private fun formatRequestId(requestId: ByteArray?): String {
        if (requestId == null || requestId.isEmpty()) return UUID.randomUUID().toString()
        val sb = StringBuilder()
        for (i in 0..<min(requestId.size, 16)) {
            sb.append(String.format("%02x", requestId[i].toInt() and 0xFF))
        }
        return sb.toString()
    }

    /**
     * Calculate latency from stored start time.
     */
    private fun calculateLatency(requestId: ByteArray?): Double? {
        val id: String = formatRequestId(requestId)
        val startNanos: Long? = RequestTimingStore.getAndRemoveStartTime(id)
        if (startNanos != null) {
            return (System.nanoTime() - startNanos) / 1000000.0
        }
        return null
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ClientInterceptor::class.java)
        private const val TIMESTAMP_SLOT_ID = 0

        /**
         * Operations and interfaces that should NOT be intercepted.
         * These are CORBA infrastructure calls that would cause recursion or TRANSIENT errors
         * if the interceptor tries to process them (especially during ORB/Naming bootstrap).
         */
        val SKIP_OPERATIONS: MutableSet<String> =
            mutableSetOf<String>( // CORBA built-in operations (called during narrow, _is_a, etc.)
                "_is_a", "_non_existent", "_get_interface_def", "_get_component",
                "_get_domain_managers", "_get_policy", "_repository_id",  // COS Naming Service operations
                "resolve", "resolve_str", "bind", "rebind", "unbind",
                "bind_context", "rebind_context", "bind_new_context",
                "list", "to_name", "to_string", "destroy",
                "new_context", "to_url",  // ORB/POA internals
                "resolve_initial_references"
            )

        val SKIP_INTERFACE_FRAGMENTS: MutableSet<String> = mutableSetOf<String>(
            "CosNaming",  // CosNaming::NamingContext, NamingContextExt
            "NamingContext",  // Any naming context interface
            "PortableServer",  // POA internals
            "IORTable",  // IOR table lookups
            "CORBA/Repository",  // Interface repository
            "InitialReferences" // ORB initial references
        )
    }
}