package tr.akguel.client

import FleetManagement.FleetManagement.*
import org.omg.CORBA.ORB
import org.omg.CosNaming.NamingContextExtHelper
import java.util.*

/**
 * CORBA Consumer (Client) — Looks up VehicleTracker from Naming Service and invokes operations.
 *
 * Usage:
 * java -cp corba-demo.jar tr.akguel.client.Consumer [nameserver-host] [nameserver-port]
 */
object Consumer {
    private lateinit var tracker: VehicleTracker

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val nsHost: String? = if (args.size > 0) args[0] else "localhost"
            val nsPort: String? = if (args.size > 1) args[1] else "2809"

            println("╔══════════════════════════════════════════╗")
            println("║   VehicleTracker — CORBA Consumer        ║")
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

            // ── Resolve Naming Service ──────────────────────
            val nsObj = orb.resolve_initial_references("NameService")
            val namingCtx = NamingContextExtHelper.narrow(nsObj)
            println("✓ Connected to Naming Service")

            // ── Look up VehicleTracker ──────────────────────
            val bindingName = "FleetManagement/VehicleTracker"
            val objRef = namingCtx!!.resolve_str(bindingName)
            tracker = VehicleTrackerHelper.narrow(objRef)
            println("✓ Found VehicleTracker: " + bindingName)

            // ── Demo: call all operations ───────────────────
            println("\n══════════════════════════════════════════")
            println("  Running demo calls...")
            println("══════════════════════════════════════════\n")

            demoPing()
            demoGetVehicleCount()
            demoListVehicles()
            demoGetVehicle("VH-0001")
            demoGetVehicle("VH-0003")
            demoUpdatePosition("VH-0002", 39.94, 32.87, 55.0f, 45.toShort())
            demoGetVehicle("VH-0002") // should show updated position
            demoGetVehicleNotFound("VH-9999")

            // ── Interactive mode ────────────────────────────
            println("\n══════════════════════════════════════════")
            println("  Interactive Mode")
            println("  Commands: ping\n" +
                    "            count\n" +
                    "            list\n" +
                    "            get <id>\n" +
                    "            update <id> <lat> <lon> <speed>\n" +
                    "            loop <count>\n" +
                    "            quit")
            println("══════════════════════════════════════════\n")

            val scanner = Scanner(System.`in`)
            while (true) {
                print("corba> ")
                val line = scanner.nextLine().trim { it <= ' ' }
                if (line.isEmpty()) continue

                val parts: Array<String?> = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val cmd = parts[0]!!.lowercase(Locale.getDefault())

                try {
                    when (cmd) {
                        "ping" -> demoPing()
                        "count" -> demoGetVehicleCount()
                        "list" -> demoListVehicles()
                        "get" -> {
                            if (parts.size < 2) {
                                println("Usage: get <vehicle_id>")
                                break
                            }
                            demoGetVehicle(parts[1])
                        }

                        "update" -> {
                            if (parts.size < 5) {
                                println("Usage: update <id> <lat> <lon> <speed>")
                                break
                            }
                            demoUpdatePosition(
                                parts[1],
                                parts[2]!!.toDouble(),
                                parts[3]!!.toDouble(),
                                parts[4]!!.toFloat(),
                                0.toShort()
                            )
                        }

                        "loop" -> {
                            val n = if (parts.size > 1) parts[1]!!.toInt() else 10
                            demoLoop(n)
                        }

                        "quit", "exit" -> {
                            println("Bye!")
                            orb.shutdown(false)
                            return
                        }

                        "shutdown" -> {
                            println("Sending shutdown to supplier...")
                            tracker.shutdown()
                            return
                        }

                        else -> println("Unknown command: " + cmd)
                    }
                } catch (e: Exception) {
                    println("Error: " + e.message)
                }
            }
        } catch (e: Exception) {
            System.err.println("ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    // ─── Demo Operations ─────────────────────────────────────────────
    private fun demoPing() {
        println("→ ping()")
        val result: String? = tracker.ping()
        println("  ← " + result)
    }

    private fun demoGetVehicleCount() {
        println("→ getVehicleCount()")
        val count: Int = tracker.getVehicleCount()
        println("  ← " + count + " vehicles")
    }

    private fun demoListVehicles() {
        println("→ listVehicles()")
        val vehicles: Array<VehicleInfo> = tracker.listVehicles()
        println("  ← " + vehicles.size + " vehicles:")
        for (v in vehicles) {
            System.out.printf(
                "    [%s] %-15s  %s  lat=%.4f lon=%.4f speed=%.1f fuel=%.0f%%%n",
                v.vehicle_id, v.driver_name, statusStr(v.status),
                v.position.latitude, v.position.longitude,
                v.position.speed_kmh, v.fuel_level_pct
            )
        }
    }

    private fun demoGetVehicle(id: String?) {
        println("→ getVehicle(\"" + id + "\")")
        try {
            val v: VehicleInfo = tracker.getVehicle(id)
            System.out.printf(
                "  ← %s | %s | %s | lat=%.4f lon=%.4f speed=%.1f heading=%d°%n",
                v.vehicle_id, v.driver_name, statusStr(v.status),
                v.position.latitude, v.position.longitude,
                v.position.speed_kmh, v.position.heading
            )
            System.out.printf("    fuel=%.1f%% odometer=%,d km%n", v.fuel_level_pct, v.odometer_km)
        } catch (e: VehicleNotFound) {
            println("  ← VehicleNotFound: " + e.message)
        }
    }

    private fun demoUpdatePosition(id: String?, lat: Double, lon: Double, speed: Float, heading: Short) {
        System.out.printf("→ updatePosition(\"%s\", lat=%.4f, lon=%.4f, speed=%.1f)%n", id, lat, lon, speed)
        try {
            val pos: GeoPosition = GeoPosition(lat, lon, speed, heading)
            tracker.updatePosition(id, pos)
            println("  ← OK")
        } catch (e: VehicleNotFound) {
            println("  ← VehicleNotFound: " + e.message)
        }
    }

    private fun demoGetVehicleNotFound(id: String?) {
        println("→ getVehicle(\"" + id + "\")  [expecting exception]")
        try {
            tracker.getVehicle(id)
            println("  ← Unexpectedly succeeded!")
        } catch (e: VehicleNotFound) {
            println("  ← VehicleNotFound exception caught: " + e.message)
        }
    }

    /**
     * Fire N rapid calls — useful for generating traffic in the monitor.
     */
    private fun demoLoop(count: Int) {
        println("→ Running " + count + " rapid calls...")
        val start = System.nanoTime()
        val ids = arrayOf<String>("VH-0001", "VH-0002", "VH-0003", "VH-0004", "VH-0005")

        for (i in 0..<count) {
            try {
                val id = ids[i % ids.size]
                when (i % 4) {
                    0 -> tracker.ping()
                    1 -> tracker.getVehicle(id)
                    2 -> tracker.getVehicleCount()
                    3 -> tracker.listVehicles()
                }
            } catch (e: Exception) {
                // Ignore for loop mode
            }
        }

        val elapsed = (System.nanoTime() - start) / 1000000.0
        System.out.printf(
            "  ← %d calls in %.1f ms (%.1f calls/sec)%n",
            count, elapsed, count / (elapsed / 1000.0)
        )
    }

    private fun statusStr(status: VehicleStatus): String {
        return when (status.value()) {
            VehicleStatus._MOVING -> "🚗 MOVING"
            VehicleStatus._IDLE -> "⏸  IDLE"
            VehicleStatus._PARKED -> "🅿  PARKED"
            VehicleStatus._MAINTENANCE -> "🔧 MAINT"
            else -> "? UNKNOWN"
        }
    }
}