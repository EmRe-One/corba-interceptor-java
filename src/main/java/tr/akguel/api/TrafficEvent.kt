package tr.akguel.api

import com.google.gson.annotations.SerializedName
import java.time.Instant


/**
 * Data Transfer Object for a single CORBA traffic event.
 * Maps to POST /api/traffic on the Laravel backend.
 */
class TrafficEvent {
    @SerializedName("request_id")
    var requestId: String? = null
        private set

    @SerializedName("operation")
    var operation: String? = null
        private set

    @SerializedName("interface_name")
    var interfaceName: String? = null
        private set

    @SerializedName("repository_id")
    private var repositoryId: String? = null

    @SerializedName("direction")
    var direction: String? = null // "request" or "reply"
        private set

    @SerializedName("status")
    var status: String? = null // "success", "error", "timeout", "exception"
        private set

    @SerializedName("source_host")
    private var sourceHost: String? = null

    @SerializedName("source_port")
    private var sourcePort: Int? = null

    @SerializedName("target_host")
    private var targetHost: String? = null

    @SerializedName("target_port")
    private var targetPort: Int? = null

    @SerializedName("source_service_name")
    private var sourceServiceName: String? = null

    @SerializedName("target_service_name")
    private var targetServiceName: String? = null

    @SerializedName("request_data")
    private var requestData: Any? = null

    @SerializedName("response_data")
    private var responseData: Any? = null

    @SerializedName("error_message")
    private var errorMessage: String? = null

    @SerializedName("exception_type")
    private var exceptionType: String? = null

    @SerializedName("latency_ms")
    var latencyMs: Double? = null
        private set

    @SerializedName("giop_version")
    private var giopVersion: String? = null

    @SerializedName("message_type")
    private var messageType: String? = null

    @SerializedName("request_size_bytes")
    private var requestSizeBytes: Int? = null

    @SerializedName("response_size_bytes")
    private var responseSizeBytes: Int? = null

    @SerializedName("interceptor_point")
    private var interceptorPoint: String? = null

    @SerializedName("context_data")
    private var contextData: MutableMap<String, Any>? = null

    @SerializedName("timestamp")
    var timestamp: String?
        private set

    init {
        this.timestamp = Instant.now().toString()
    }

    // Builder-style setters
    fun requestId(requestId: String?): TrafficEvent {
        this.requestId = requestId
        return this
    }

    fun operation(operation: String?): TrafficEvent {
        this.operation = operation
        return this
    }

    fun interfaceName(interfaceName: String?): TrafficEvent {
        this.interfaceName = interfaceName
        return this
    }

    fun repositoryId(repositoryId: String?): TrafficEvent {
        this.repositoryId = repositoryId
        return this
    }

    fun direction(direction: String?): TrafficEvent {
        this.direction = direction
        return this
    }

    fun status(status: String?): TrafficEvent {
        this.status = status
        return this
    }

    fun sourceHost(sourceHost: String?): TrafficEvent {
        this.sourceHost = sourceHost
        return this
    }

    fun sourcePort(sourcePort: Int?): TrafficEvent {
        this.sourcePort = sourcePort
        return this
    }

    fun targetHost(targetHost: String?): TrafficEvent {
        this.targetHost = targetHost
        return this
    }

    fun targetPort(targetPort: Int?): TrafficEvent {
        this.targetPort = targetPort
        return this
    }

    fun sourceServiceName(name: String?): TrafficEvent {
        this.sourceServiceName = name
        return this
    }

    fun targetServiceName(name: String?): TrafficEvent {
        this.targetServiceName = name
        return this
    }

    fun requestData(data: Any?): TrafficEvent {
        this.requestData = data
        return this
    }

    fun responseData(data: Any?): TrafficEvent {
        this.responseData = data
        return this
    }

    fun errorMessage(msg: String?): TrafficEvent {
        this.errorMessage = msg
        return this
    }

    fun exceptionType(type: String?): TrafficEvent {
        this.exceptionType = type
        return this
    }

    fun latencyMs(ms: Double?): TrafficEvent {
        this.latencyMs = ms
        return this
    }

    fun giopVersion(version: String?): TrafficEvent {
        this.giopVersion = version
        return this
    }

    fun messageType(type: String?): TrafficEvent {
        this.messageType = type
        return this
    }

    fun requestSizeBytes(bytes: Int?): TrafficEvent {
        this.requestSizeBytes = bytes
        return this
    }

    fun responseSizeBytes(bytes: Int?): TrafficEvent {
        this.responseSizeBytes = bytes
        return this
    }

    fun interceptorPoint(point: String?): TrafficEvent {
        this.interceptorPoint = point
        return this
    }

    fun contextData(data: MutableMap<String, Any>?): TrafficEvent {
        this.contextData = data
        return this
    }

    fun timestamp(ts: String?): TrafficEvent {
        this.timestamp = ts
        return this
    }

    override fun toString(): String {
        return String.format(
            "TrafficEvent{op=%s, iface=%s, dir=%s, status=%s, latency=%.1fms}",
            operation, interfaceName, direction, status, if (latencyMs != null) latencyMs else 0.0
        )
    }
}
