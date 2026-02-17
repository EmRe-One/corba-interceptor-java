package tr.akguel.config

import java.io.IOException
import java.util.*

/**
 * Configuration for the CORBA Monitor Agent.
 * Reads from monitor.properties, system properties, or environment variables.
 *
 * Priority: System Property > Environment Variable > Properties File > Default
 */
class MonitorConfig private constructor() {
    val apiBaseUrl: String?
    val apiToken: String?
    val batchSize: Int
    val flushIntervalMs: Int
    val httpTimeoutMs: Int
    val httpPoolSize: Int
    val enabled: Boolean
    val captureRequestData: Boolean
    val captureResponseData: Boolean
    val maxPayloadBytes: Int
    val nameserverHost: String?
    val nameserverPort: Int
    val scanIntervalSeconds: Int
    val scanEnabled: Boolean

    init {
        val props: Properties = loadProperties()

        this.apiBaseUrl = resolve(props, "monitor.api.url", "CORBA_MONITOR_API_URL", "http://localhost:8080/api")!!
        this.apiToken = resolve(props, "monitor.api.token", "CORBA_MONITOR_API_TOKEN", "")!!
        this.batchSize = resolve(props, "monitor.batch.size", "CORBA_MONITOR_BATCH_SIZE", "50")!!.toInt()
        this.flushIntervalMs = resolve(props, "monitor.flush.interval.ms", "CORBA_MONITOR_FLUSH_MS", "1000")!!.toInt()
        this.httpTimeoutMs = resolve(props, "monitor.http.timeout.ms", "CORBA_MONITOR_HTTP_TIMEOUT", "5000")!!.toInt()
        this.httpPoolSize = resolve(props, "monitor.http.pool.size", "CORBA_MONITOR_HTTP_POOL", "4")!!.toInt()
        this.enabled = resolve(props, "monitor.enabled", "CORBA_MONITOR_ENABLED", "true")!!.toBoolean()
        this.captureRequestData =
            resolve(props, "monitor.capture.request", "CORBA_MONITOR_CAPTURE_REQ", "true")!!.toBoolean()
        this.captureResponseData =
            resolve(props, "monitor.capture.response", "CORBA_MONITOR_CAPTURE_RES", "true")!!.toBoolean()
        this.maxPayloadBytes = resolve(props, "monitor.max.payload.bytes", "CORBA_MONITOR_MAX_PAYLOAD", "65536")!!.toInt()
        this.nameserverHost = resolve(props, "monitor.nameserver.host", "CORBA_NAMESERVER_HOST", "localhost")!!
        this.nameserverPort = resolve(props, "monitor.nameserver.port", "CORBA_NAMESERVER_PORT", "2809")!!.toInt()
        this.scanIntervalSeconds =
            resolve(props, "monitor.scan.interval.seconds", "CORBA_MONITOR_SCAN_INTERVAL", "30")!!.toInt()
        this.scanEnabled = resolve(props, "monitor.scan.enabled", "CORBA_MONITOR_SCAN_ENABLED", "true")!!.toBoolean()
    }

    private fun loadProperties(): Properties {
        val props = Properties()
        try {
            javaClass.getClassLoader().getResourceAsStream(MonitorConfig.Companion.PROPS_FILE).use { `is` ->
                if (`is` != null) {
                    props.load(`is`)
                }
            }
        } catch (e: IOException) {
            // Ignore — use defaults
        }
        return props
    }

    /**
     * Resolve a config value: System Property > Env Var > Properties > Default
     */
    private fun resolve(props: Properties, propKey: String, envKey: String?, defaultValue: String?): String? {
        var value = System.getProperty(propKey)
        if (value != null && !value.isEmpty()) return value

        value = System.getenv(envKey)
        if (value != null && !value.isEmpty()) return value

        value = props.getProperty(propKey)
        if (value != null && !value.isEmpty()) return value

        return defaultValue
    }

    val trafficEndpoint: String
        get() = "$apiBaseUrl/traffic"
    val batchEndpoint: String
        get() = "$apiBaseUrl/traffic/batch"
    val nameserverReportEndpoint: String
        get() = "$apiBaseUrl/nameserver/report"
    val healthEndpoint: String
        get() = "$apiBaseUrl/health"

    override fun toString(): String {
        return String.format(
            "MonitorConfig{api=%s, batch=%d, flush=%dms, capture_req=%b, capture_res=%b, ns=%s:%d, scan=%b/%ds}",
            apiBaseUrl, batchSize, flushIntervalMs, captureRequestData, captureResponseData,
            nameserverHost, nameserverPort, scanEnabled, scanIntervalSeconds
        )
    }

    companion object {
        private const val PROPS_FILE = "monitor.properties"

        @get:Synchronized
        var instance: MonitorConfig? = null
            get() {
                if (field == null) {
                    this.instance = MonitorConfig()
                }
                return field
            }
            private set
    }
}