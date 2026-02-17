package tr.akguel
import org.omg.CORBA.ORB
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tr.akguel.api.MonitorApiClient
import tr.akguel.config.MonitorConfig
import tr.akguel.nameserver.NameserverScanner
import java.util.*
import kotlin.Array
import kotlin.Exception


/**
 * CORBA Monitor Agent — Standalone entry point.
 *
 * This agent can run in two modes:
 *
 * 1. STANDALONE MODE (this main class):
 * Starts its own ORB, connects to the nameserver, and scans it periodically.
 * Interceptors are registered but only capture traffic if this ORB is used
 * as a client to invoke CORBA objects.
 *
 * Usage:
 * java -jar corba-interceptor.jar
 *
 * 2. EMBEDDED MODE (ORBInitializer):
 * Add the JAR to an existing CORBA application's classpath and register
 * the interceptor via JVM property. All traffic through that ORB is captured.
 *
 * Usage:
 * java -Dorg.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer= \
 * -cp "your-app.jar:corba-interceptor.jar" \
 * com.yourapp.Main
 *
 * In both modes, traffic events are sent asynchronously to the Laravel Monitor API.
 */
class CORBAMonitorAgent {

    private lateinit var orb: ORB
    private lateinit var scanner: NameserverScanner
    private lateinit var apiClient: MonitorApiClient

    fun start(args: Array<String>?) {
        log.info("╔══════════════════════════════════════════════════╗")
        log.info("║       CORBA Monitor Agent v1.0.0                ║")
        log.info("║       Standalone Mode                           ║")
        log.info("╚══════════════════════════════════════════════════╝")

        val config: MonitorConfig = MonitorConfig.instance!!
        log.info("Configuration: {}", config)

        if (!config.enabled) {
            log.warn("Monitor is DISABLED. Set monitor.enabled=true to activate.")
            return
        }

        // Initialize API client
        apiClient = MonitorApiClient.instance!!

        // Health check
        if (apiClient.healthCheck()) {
            log.info("✓ Monitor API is reachable at {}", config.apiBaseUrl)
        } else {
            log.warn(
                "✗ Monitor API is NOT reachable at {} — events will be queued",
                config.apiBaseUrl
            )
        }

        // Initialize ORB with interceptor registration
        initORB(args, config)

        // Start nameserver scanner
        scanner = NameserverScanner(orb)
        scanner.start()

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread({ this.shutdown() }, "corba-monitor-shutdown"))

        log.info("Agent running. Press Ctrl+C to stop.")

        // Keep alive — the ORB run loop keeps the agent responsive
        try {
            orb.run()
        } catch (e: Exception) {
            log.info("ORB run loop ended: {}", e.message)
        }
    }

    /**
     * Initialize the ORB with JacORB properties and interceptor registration.
     */
    private fun initORB(args: Array<String>?, config: MonitorConfig) {
        val orbProps = Properties()

        // JacORB configuration
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB")
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton")

        // Register our interceptor initializer
        orbProps.setProperty(
            "org.omg.PortableInterceptor.ORBInitializerClass.tr.akguel.interceptor.MonitorORBInitializer",
            ""
        )

        // Nameserver connection
        val corbaloc = String.format(
            "corbaloc:iiop:%s:%d/NameService",
            config.nameserverHost, config.nameserverPort
        )
        orbProps.setProperty("ORBInitRef.NameService", corbaloc)

        // JacORB-specific tuning
        orbProps.setProperty("jacorb.connection.client.connect_timeout", "5000")
        orbProps.setProperty("jacorb.retries", "3")
        orbProps.setProperty("jacorb.retry_interval", "500")
        orbProps.setProperty("jacorb.log.default.verbosity", "1")

        log.info(
            "Initializing ORB (nameserver={}:{})",
            config.nameserverHost,
            config.nameserverPort
        )

        orb = ORB.init(args, orbProps)

        log.info("✓ ORB initialized")
    }

    /**
     * Graceful shutdown.
     */
    private fun shutdown() {
        log.info("Shutting down CORBA Monitor Agent...")

        scanner.stop()

        apiClient.shutdown()

        try {
            orb.shutdown(false)
        } catch (ignored: Exception) {
        }

        CORBAMonitorAgent.Companion.log.info(
            "Agent stopped. Stats: sent={}, failed={}, dropped={}",
            apiClient.sentCount,
            apiClient.failedCount,
            apiClient.droppedCount
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CORBAMonitorAgent::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val agent = CORBAMonitorAgent()
            agent.start(args)
        }
    }
}
