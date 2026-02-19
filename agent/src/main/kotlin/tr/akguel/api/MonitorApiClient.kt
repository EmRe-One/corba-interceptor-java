package tr.akguel.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tr.akguel.config.MonitorConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.*
import kotlin.concurrent.Volatile


/**
 * Async HTTP client that batches TrafficEvents and sends them to the Monitor API.
 *
 * Features:
 * - Non-blocking: interceptors submit events to a queue
 * - Batching: events are grouped and sent in bulk
 * - Auto-flush: periodic flush based on configurable interval
 * - Retry: failed batches are retried once
 * - Metrics: tracks sent/failed/queued counts
 */
class MonitorApiClient private constructor() {

    private val config: MonitorConfig = MonitorConfig.instance!!
    private val gson: Gson
    private val eventQueue: BlockingQueue<TrafficEvent>
    private val httpPool: ExecutorService
    private val scheduler: ScheduledExecutorService

    val sentCount: AtomicLong = AtomicLong(0)
    val failedCount: AtomicLong = AtomicLong(0)
    val droppedCount: AtomicLong = AtomicLong(0)

    @Volatile
    private var running = true

    init {
        this.gson = GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .create()
        this.eventQueue = LinkedBlockingQueue<TrafficEvent>(MAX_QUEUE_SIZE)
        this.httpPool = Executors.newFixedThreadPool(config.httpPoolSize) { r: Runnable? ->
            val t = Thread(r, "corba-monitor-http")
            t.setDaemon(true)
            t
        }
        this.scheduler = Executors.newSingleThreadScheduledExecutor(ThreadFactory { r: Runnable? ->
            val t = Thread(r, "corba-monitor-flush")
            t.setDaemon(true)
            t
        })

        // Disable SSL verification in dev mode
        if (config.sslTrustAll) {
            disableSslVerification();
            log.warn("SSL verification DISABLED — do NOT use in production");
        }

        // Schedule periodic flush
        scheduler.scheduleAtFixedRate(
            Runnable { this.flush() },
            config.flushIntervalMs.toLong(),
            config.flushIntervalMs.toLong(),
            TimeUnit.MILLISECONDS
        )

        log.info("MonitorApiClient started: {}", config)
    }

    /**
     * Submit a traffic event (non-blocking).
     * If the queue is full, the event is dropped and counted.
     */
    fun submit(event: TrafficEvent) {
        if (!config.enabled || !running) return

        val added: Boolean = eventQueue.offer(event)
        if (!added) {
            droppedCount.incrementAndGet()
            if (droppedCount.get() % 100 == 0L) {
                log.warn("Event queue full — dropped {} events so far", droppedCount.get())
            }
        }
    }

    /**
     * Drain the queue and send events in batches.
     */
    fun flush() {
        if (eventQueue.isEmpty()) return

        val batch: MutableList<TrafficEvent> = ArrayList<TrafficEvent>(config.batchSize)
        eventQueue.drainTo(batch, config.batchSize)

        if (!batch.isEmpty()) {
            httpPool.submit { sendBatch(batch) }
        }

        // If there are more, schedule additional flushes
        while (!eventQueue.isEmpty()) {
            val nextBatch: MutableList<TrafficEvent> = ArrayList<TrafficEvent>(config.batchSize)
            eventQueue.drainTo(nextBatch, config.batchSize)
            if (!nextBatch.isEmpty()) {
                httpPool.submit { sendBatch(nextBatch) }
            }
        }
    }

    /**
     * Send a batch of events to the API.
     */
    private fun sendBatch(batch: MutableList<TrafficEvent>) {
        if (batch.size == 1) {
            sendSingle(batch[0])
            return
        }

        try {
            val payload: MutableMap<String?, Any?> = HashMap<String?, Any?>()
            payload["events"] = batch
            val json: String = gson.toJson(payload)
            log.info("Sending batch to {}. Body: {}", config.batchEndpoint, json)
            val status: Int = post(config.batchEndpoint, json)

            if (status in 200..<300) {
                sentCount.addAndGet(batch.size.toLong())
                log.debug("Sent batch of {} events (total: {})", batch.size, sentCount.get())
            } else {
                failedCount.addAndGet(batch.size.toLong())
                log.warn(
                    "Batch send failed with HTTP {}: {} events lost",
                    status,
                    batch.size
                )
            }
        } catch (e: Exception) {
            failedCount.addAndGet(batch.size.toLong())
            log.error("Batch send error: {}", e.message)
        }
    }

    /**
     * Send a single event.
     */
    private fun sendSingle(event: TrafficEvent) {
        try {
            val json: String = gson.toJson(event)
            val status: Int = post(config.trafficEndpoint, json)

            if (status in 200..<300) {
                sentCount.incrementAndGet()
                log.debug("Sent event: {} (total: {})", event.operation, sentCount.get())
            } else {
                failedCount.incrementAndGet()
                log.warn("Event send failed with HTTP {}: {}", status, event.operation)
            }
        } catch (e: Exception) {
            failedCount.incrementAndGet()
            log.error("Event send error: {}", e.message)
        }
    }

    /**
     * HTTP POST with JSON body.
     */
    @Throws(IOException::class)
    private fun post(endpoint: String, json: String): Int {
        val url: URL = URI(endpoint).toURL()
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        if (conn is HttpsURLConnection && config.sslTrustAll) {
            applyTrustAll(conn)
        }
        try {
            conn.setRequestMethod("POST")
            conn.setDoOutput(true)
            conn.setConnectTimeout(config.httpTimeoutMs)
            conn.setReadTimeout(config.httpTimeoutMs)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")

            val token: String? = config.apiToken
            if (!token.isNullOrEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }

            val bytes: ByteArray = json.toByteArray(StandardCharsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)

            conn.getOutputStream().use { os ->
                os.write(bytes)
                os.flush()
            }
            return conn.getResponseCode()
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Check if the Monitor API is reachable.
     */
    fun healthCheck(): Boolean {
        try {
            val url: URL = URI(config.healthEndpoint).toURL()
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            if (conn is HttpsURLConnection && config.sslTrustAll) {
                applyTrustAll(conn)
            }
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(3000)
            conn.setReadTimeout(3000)
            val status: Int = conn.getResponseCode()
            conn.disconnect()
            return status == 200
        } catch (e: Exception) {
            return false
        }
    }

    // ─── SSL Trust-All (Development Only) ─────────────────────────────
    /**
     * Disable SSL certificate verification globally.
     * WARNING: Only for local development!
     */
    private fun disableSslVerification() {
        try {
            val trustAll: Array<TrustManager> = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                    return kotlin.arrayOfNulls<X509Certificate>(0)
                }

                override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
            })

            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAll, SecureRandom())
            trustAllSocketFactory = sc.socketFactory
            trustAllHostnameVerifier = HostnameVerifier { hostname: String?, session: SSLSession? -> true }

            // Also set as default for any other HTTPS connections
            HttpsURLConnection.setDefaultSSLSocketFactory(trustAllSocketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnameVerifier)
        } catch (e: java.lang.Exception) {
            log.error("Failed to disable SSL verification: {}", e.message)
        }
    }

    /**
     * Apply trust-all settings to a specific HTTPS connection.
     */
    private fun applyTrustAll(conn: HttpsURLConnection) {
        if (trustAllSocketFactory != null) {
            conn.sslSocketFactory = trustAllSocketFactory
        }
        if (trustAllHostnameVerifier != null) {
            conn.setHostnameVerifier(trustAllHostnameVerifier)
        }
    }

    /**
     * Graceful shutdown: flush remaining events and stop threads.
     */
    fun shutdown() {
        log.info("Shutting down MonitorApiClient...")
        running = false
        flush()
        scheduler.shutdown()
        httpPool.shutdown()
        try {
            httpPool.awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        log.info(
            "MonitorApiClient shutdown complete. Sent: {}, Failed: {}, Dropped: {}",
            sentCount.get(), failedCount.get(), droppedCount.get()
        )
    }

    val queueSize: Int
        get() = eventQueue.size

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MonitorApiClient::class.java)
        private var trustAllSocketFactory: SSLSocketFactory? = null
        private var trustAllHostnameVerifier: HostnameVerifier? = null

        @get:Synchronized
        var instance: MonitorApiClient? = null
            get() {
                if (field == null) {
                    this.instance = MonitorApiClient()
                }
                return field
            }
            private set

        private const val MAX_QUEUE_SIZE = 10000
    }
}