package tr.akguel.server

import FleetManagement.FleetManagement.VehicleTracker
import FleetManagement.FleetManagement.VehicleTrackerHelper
import org.omg.CORBA.ORB
import org.omg.CosNaming.NamingContextExtHelper
import org.omg.CosNaming.NamingContextPackage.AlreadyBound
import org.omg.PortableServer.POAHelper
import java.util.Properties
import kotlin.math.min

/**
 * CORBA Supplier (Server) — Registers VehicleTracker with the Naming Service.
 *
 * Usage:
 * java -cp corba-demo.jar tr.akguel.server.Supplier [nameserver-host] [nameserver-port]
 */
object Supplier {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val nsHost: String? = if (args.size > 0) args[0] else "localhost"
            val nsPort: String? = if (args.size > 1) args[1] else "2809"

            println("╔══════════════════════════════════════════╗")
            println("║   VehicleTracker — CORBA Supplier       ║")
            println("╚══════════════════════════════════════════╝")
            println("Nameserver: " + nsHost + ":" + nsPort)

            // ── ORB Properties ──────────────────────────────
            val props = Properties()
            props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB")
            props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton")
            props.setProperty(
                "ORBInitRef.NameService",
                "corbaloc:iiop:" + nsHost + ":" + nsPort + "/NameService"
            )

            // ── (Optional) Register CORBA Monitor interceptors ──
            // Uncomment to enable monitoring:
            // props.setProperty(
            //     "org.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer",
            //     ""
            // );

            // ── Initialize ORB ──────────────────────────────
            val orb = ORB.init(args, props)
            println("✓ ORB initialized")

            // ── Activate POA ────────────────────────────────
            val rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"))
            rootPOA!!.the_POAManager().activate()
            println("✓ POA activated")

            // ── Create Servant ──────────────────────────────
            val servant = VehicleTrackerImpl(orb)
            val ref = rootPOA.servant_to_reference(servant)
            val vehicleTracker: VehicleTracker? = VehicleTrackerHelper.narrow(ref)
            println("✓ VehicleTracker servant created")

            // ── Register with Naming Service ────────────────
            val nsObj = orb.resolve_initial_references("NameService")
            val namingCtx = NamingContextExtHelper.narrow(nsObj)

            // Create context path: FleetManagement/VehicleTracker
            try {
                // Create the "FleetManagement" context if it doesn't exist
                val fleetCtxPath = namingCtx!!.to_name("FleetManagement")
                try {
                    namingCtx.bind_new_context(fleetCtxPath)
                    println("  Created context: FleetManagement/")
                } catch (e: AlreadyBound) {
                    // Context already exists, that's fine
                }
            } catch (e: Exception) {
                println("  Context FleetManagement/ already exists")
            }

            // Bind the VehicleTracker object
            val bindingName = "FleetManagement/VehicleTracker"
            val name = namingCtx!!.to_name(bindingName)
            try {
                namingCtx.rebind(name, vehicleTracker)
            } catch (e: Exception) {
                namingCtx.bind(name, vehicleTracker)
            }
            println("✓ Registered as: " + bindingName)

            // ── Print IOR (useful for debugging) ────────────
            val ior = orb.object_to_string(vehicleTracker)
            println("\nIOR (first 80 chars): " + ior.substring(0, min(80, ior.length)) + "...")
            println("\n══════════════════════════════════════════")
            println("  Supplier is RUNNING. Waiting for calls...")
            println("  Press Ctrl+C to stop.")
            println("══════════════════════════════════════════\n")

            // ── Run ORB event loop ──────────────────────────
            orb.run()

            println("Supplier stopped.")
        } catch (e: Exception) {
            System.err.println("ERROR: " + e.message)
            e.printStackTrace()
        }
    }
}