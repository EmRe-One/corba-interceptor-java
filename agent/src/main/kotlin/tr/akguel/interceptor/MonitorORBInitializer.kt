package tr.akguel.interceptor

import org.omg.CORBA.LocalObject
import org.omg.PortableInterceptor.ORBInitInfo
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
import org.omg.PortableInterceptor.ORBInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * ORB Initializer that registers the Client and Server interceptors.
 *
 * This is registered via:
 * -Dorg.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer
 *
 * Or in orb.properties:
 * org.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer=
 */
class MonitorORBInitializer : LocalObject(), ORBInitializer {
    override fun pre_init(info: ORBInitInfo) {
        log.info("╔══════════════════════════════════════════════════╗")
        log.info("║       CORBA Monitor Interceptor Agent           ║")
        log.info("╚══════════════════════════════════════════════════╝")

        try {
            // Allocate a slot for timing data
            val slotId = info.allocate_slot_id()

            // Register client-side interceptor
            val clientInterceptor = ClientInterceptor(slotId)
            info.add_client_request_interceptor(clientInterceptor)
            log.info("✓ ClientRequestInterceptor registered")

            // Register server-side interceptor
            val serverInterceptor = ServerInterceptor()
            info.add_server_request_interceptor(serverInterceptor)
            log.info("✓ ServerRequestInterceptor registered")
        } catch (e: DuplicateName) {
            log.error("Duplicate interceptor name: {}", e.name)
        } catch (e: Exception) {
            log.error("Failed to register interceptors: {}", e.message, e)
        }
    }

    override fun post_init(info: ORBInitInfo?) {
        log.info("CORBA Monitor interceptors active — forwarding traffic to API")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MonitorORBInitializer::class.java)
    }
}
