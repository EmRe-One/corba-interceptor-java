package tr.akguel

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.Volatile
import kotlin.system.exitProcess


/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  CORBA Demo — NamingService + Supplier + Consumer           ║
 * ║                                                              ║
 * ║  IDL: FleetManagement::VehicleTracker                        ║
 * ║  Protocol: Java Serialization over TCP (simulates GIOP/IIOP) ║
 * ║                                                              ║
 * ║  Usage: java CORBADemo.java                                  ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
object CORBADemo {
    // ═══════════════════════════════════════════════════════════
    //  MAIN — Start all three components
    // ═══════════════════════════════════════════════════════════
    const val RESET: String = "\u001b[0m"
    val COLORS: MutableMap<String, String> = mutableMapOf(
        "NS" to "\u001b[36m",  // Cyan
        "SUP" to "\u001b[32m",  // Green
        "CON" to "\u001b[33m" // Yellow
    )

    @Synchronized
    fun log(component: String?, msg: String?) {
        val color = COLORS.getOrDefault(component, RESET)
        System.out.printf("%s[%-3s]%s %s%n", color, component, RESET, msg)
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val nsPort = 2809
        val nsHost = "127.0.0.1"

        println()
        println("\u001b[1m  CORBA Demo — VehicleTracker\u001b[0m")
        println("  IDL: FleetManagement::VehicleTracker")
        println("  Architecture: NamingService + Supplier + Consumer")
        println()

        // ── 1. Start Naming Service ──
        val ns = NamingService(nsPort)
        Thread.startVirtualThread(ns)
        while (!ns.ready) Thread.sleep(50)
        println()

        // ── 2. Start Supplier ──
        val supplier = SupplierService(nsHost, nsPort)
        Thread.startVirtualThread(supplier)
        while (!supplier.ready) Thread.sleep(50)
        println()

        // Small delay for clean output
        Thread.sleep(200)

        // ── 3. Run Consumer ──
        val consumer = ConsumerClient()
        consumer.start(nsHost, nsPort)

        println()
        exitProcess(0)
    }

    // ═══════════════════════════════════════════════════════════
    //  IDL Types (normally generated from VehicleTracker.idl)
    // ═══════════════════════════════════════════════════════════
    // FleetManagement::GeoPosition
    @JvmRecord
    internal data class GeoPosition(
        val latitude: Double,
        val longitude: Double,
        val speed_kmh: Float,
        val heading: Short
    ) : Serializable {
        override fun toString(): String {
            return String.format(
                "lat=%.4f lon=%.4f speed=%.1f heading=%d°",
                latitude, longitude, speed_kmh, heading
            )
        }
    }

    // FleetManagement::VehicleStatus
    internal enum class VehicleStatus {
        MOVING, IDLE, PARKED, MAINTENANCE
    }

    // FleetManagement::VehicleInfo
    @JvmRecord
    internal data class VehicleInfo(
        val vehicle_id: String?, val driver_name: String?, val position: GeoPosition?,
        val status: VehicleStatus?, val fuel_level_pct: Float, val odometer_km: Int
    ) : Serializable {
        override fun toString(): String {
            val st = when (status) {
                VehicleStatus.MOVING -> "MOVING "
                VehicleStatus.IDLE -> "IDLE   "
                VehicleStatus.PARKED -> "PARKED "
                VehicleStatus.MAINTENANCE -> "MAINT  "
                else -> {
                    "UNKNOWN"
                }
            }
            return String.format(
                "[%s] %-14s %s  %s  fuel=%.0f%%  odo=%,dkm",
                vehicle_id, driver_name, st, position, fuel_level_pct, odometer_km
            )
        }
    }

    // FleetManagement::VehicleNotFound exception
    internal class VehicleNotFound(val vehicle_id: String?, msg: String?) : Exception(msg)

    // ═══════════════════════════════════════════════════════════
    //  GIOP Messages (Request / Response)
    // ═══════════════════════════════════════════════════════════
    @JvmRecord
    internal data class CORBARequest(val operation: String?, val args: Any) : Serializable

    @JvmRecord
    internal data class CORBAResponse(
        val success: Boolean, val result: Any?,
        val errorType: String?, val errorMessage: String?
    ) : Serializable {
        companion object {
            fun ok(r: Any?): CORBAResponse {
                return CORBAResponse(true, r, null, null)
            }

            fun error(type: String?, msg: String?): CORBAResponse {
                return CORBAResponse(false, null, type, msg)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Naming Service Messages
    // ═══════════════════════════════════════════════════════════
    @JvmRecord
    internal data class NamingRequest(val command: String?, val name: String?, val host: String?, val port: Int) :
        Serializable {
        constructor(cmd: String?, name: String?) : this(cmd, name, null, 0)
    }

    @JvmRecord
    internal data class NamingResponse(
        val success: Boolean, val host: String?, val port: Int,
        val error: String?, val entries: MutableList<String?>?
    ) : Serializable {
        companion object {
            fun ok(h: String?, p: Int): NamingResponse {
                return NamingResponse(true, h, p, null, null)
            }

            fun list(e: MutableList<String?>?): NamingResponse {
                return NamingResponse(true, null, 0, null, e)
            }

            fun error(e: String?): NamingResponse {
                return NamingResponse(false, null, 0, e, null)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  1. NAMING SERVICE
    // ═══════════════════════════════════════════════════════════
    internal class NamingService(val port: Int) : Runnable {
        val hostMap: MutableMap<String?, String?> = ConcurrentHashMap<String?, String?>()
        val portMap: MutableMap<String?, Int> = ConcurrentHashMap<String?, Int>()

        @Volatile
        var ready: Boolean = false

        override fun run() {
            try {
                ServerSocket(port).use { server ->
                    log("NS", "╔══════════════════════════════════════════════╗")
                    log("NS", "║   CORBA Naming Service — Port " + port + "            ║")
                    log("NS", "╚══════════════════════════════════════════════╝")
                    ready = true
                    while (true) {
                        val client: Socket = server.accept()
                        Thread.startVirtualThread(Runnable { handleNS(client) })
                    }
                }
            } catch (e: Exception) {
                log("NS", "ERROR: " + e.message)
            }
        }

        fun handleNS(client: Socket) {
            try {
                client.use {
                    ObjectOutputStream(client.getOutputStream()).use { out ->
                        ObjectInputStream(client.getInputStream()).use { `in` ->
                            val req = `in`.readObject() as NamingRequest
                            val resp = when (req.command) {
                                "rebind" -> {
                                    hostMap[req.name] = req.host
                                    portMap[req.name] = req.port
                                    log("NS", "  BIND    " + req.name + " → " + req.host + ":" + req.port)
                                    NamingResponse.Companion.ok(req.host, req.port)
                                }

                                "resolve" -> {
                                    val h = hostMap[req.name]
                                    val p: Int = portMap[req.name]!!
                                    if (h == null) {
                                        log("NS", "  RESOLVE " + req.name + " → NOT FOUND")
                                        NamingResponse.Companion.error("NotFound: " + req.name)
                                    } else {
                                        log("NS", "  RESOLVE " + req.name + " → " + h + ":" + p)
                                        NamingResponse.Companion.ok(h, p)
                                    }
                                }

                                "list" -> {
                                    val entries = ArrayList<String?>()
                                    hostMap.forEach { (n: String?, h: String?) ->
                                        entries.add(
                                            n + " → " + h + ":" + portMap.get(
                                                n
                                            )
                                        )
                                    }
                                    log("NS", "  LIST    " + entries.size + " entries")
                                    NamingResponse.Companion.list(entries)
                                }

                                else -> NamingResponse.Companion.error("Unknown: " + req.command)
                            }

                            out.writeObject(resp)
                            out.flush()
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  2. SUPPLIER (VehicleTracker Implementation)
    // ═══════════════════════════════════════════════════════════
    internal class SupplierService(val nsHost: String?, val nsPort: Int) : Runnable {
        val vehicles: MutableMap<String?, VehicleInfo> = ConcurrentHashMap<String?, VehicleInfo>()

        @Volatile
        var ready: Boolean = false
        var myPort: Int = 0

        init {
            seedData()
        }

        override fun run() {
            try {
                ServerSocket(0).use { server ->
                    myPort = server.getLocalPort()
                    val myHost = InetAddress.getLoopbackAddress().getHostAddress()

                    log("SUP", "╔══════════════════════════════════════════════╗")
                    log("SUP", "║   VehicleTracker — Supplier                  ║")
                    log("SUP", "║   Port: " + myPort + "                                  ║")
                    log("SUP", "╚══════════════════════════════════════════════╝")
                    log("SUP", "  " + vehicles.size + " vehicles loaded")

                    // Register with Naming Service
                    registerWithNS(myHost, myPort)
                    ready = true

                    log("SUP", "  Waiting for incoming CORBA requests...")
                    log("SUP", "")
                    while (true) {
                        val client: Socket = server.accept()
                        Thread.startVirtualThread(Runnable { handleCall(client) })
                    }
                }
            } catch (e: Exception) {
                log("SUP", "ERROR: " + e.message)
            }
        }

        @Throws(Exception::class)
        fun registerWithNS(myHost: String?, myPort: Int) {
            Socket(nsHost, nsPort).use { ns ->
                ObjectOutputStream(ns.getOutputStream()).use { out ->
                    ObjectInputStream(ns.getInputStream()).use { `in` ->
                        out.writeObject(
                            NamingRequest(
                                "rebind",
                                "FleetManagement/VehicleTracker", myHost, myPort
                            )
                        )
                        out.flush()

                        val resp = `in`.readObject() as NamingResponse
                        if (resp.success) {
                            log("SUP", "  ✓ Registered: FleetManagement/VehicleTracker")
                        } else {
                            throw RuntimeException(resp.error)
                        }
                    }
                }
            }
        }

        fun handleCall(client: Socket) {
            try {
                client.use {
                    ObjectInputStream(client.getInputStream()).use { `in` ->
                        ObjectOutputStream(client.getOutputStream()).use { out ->
                            val req = `in`.readObject() as CORBARequest
                            val t0 = System.nanoTime()

                            val resp = dispatch(req)

                            val ms = (System.nanoTime() - t0) / 1000000.0
                            val detail = if (resp.success)
                                summarize(resp.result!!)
                            else
                                resp.errorType + ": " + resp.errorMessage
                            log(
                                "SUP", String.format(
                                    "  %-20s → %s  (%.2fms)",
                                    req.operation + "()", detail, ms
                                )
                            )

                            out.writeObject(resp)
                            out.flush()
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }

        fun dispatch(req: CORBARequest): CORBAResponse {
            try {
                return when (req.operation) {
                    "ping" -> CORBAResponse.Companion.ok("pong @ " + System.currentTimeMillis())
                    "getVehicleCount" -> CORBAResponse.Companion.ok(vehicles.size)
                    "listVehicles" -> CORBAResponse.Companion.ok(vehicles.values.toTypedArray<VehicleInfo>())
                    "getVehicle" -> {
                        val id = (req.args as List<*>)[0] as String?
                        val v: VehicleInfo = vehicles[id] ?: throw VehicleNotFound(id, "'$id' not found")
                        CORBAResponse.Companion.ok(v)
                    }

                    "updatePosition" -> {
                        val id = (req.args as List<*>)[0] as String?
                        val pos = (req.args as List<*>)[1] as GeoPosition
                        val old: VehicleInfo = vehicles[id] ?: throw VehicleNotFound(id, "'$id' not found")
                        vehicles[id] = VehicleInfo(
                            id, old.driver_name, pos,
                            if (pos.speed_kmh > 0) VehicleStatus.MOVING else VehicleStatus.IDLE,
                            old.fuel_level_pct, old.odometer_km
                        )
                        CORBAResponse.Companion.ok("OK")
                    }

                    else -> CORBAResponse.Companion.error("BAD_OPERATION", req.operation)
                }
            } catch (e: VehicleNotFound) {
                return CORBAResponse.Companion.error("VehicleNotFound", e.message)
            }
        }

        fun seedData() {
            v("VH-0001", "Ahmet Yilmaz", 39.9208, 32.8541, 62.5f, 90.toShort(), VehicleStatus.MOVING, 78.5f, 125430)
            v("VH-0002", "Mehmet Demir", 39.9334, 32.8597, 0.0f, 0.toShort(), VehicleStatus.PARKED, 45.2f, 89200)
            v("VH-0003", "Ayse Kaya", 39.9120, 32.8390, 45.0f, 180.toShort(), VehicleStatus.MOVING, 92.1f, 67890)
            v("VH-0004", "Fatma Ozturk", 39.9456, 32.8700, 0.0f, 0.toShort(), VehicleStatus.IDLE, 60.0f, 210340)
            v("VH-0005", "Ali Celik", 39.9050, 32.8200, 0.0f, 0.toShort(), VehicleStatus.MAINTENANCE, 15.3f, 340500)
        }

        fun v(
            id: String?,
            d: String?,
            lat: Double,
            lon: Double,
            spd: Float,
            h: Short,
            s: VehicleStatus?,
            f: Float,
            o: Int
        ) {
            vehicles[id] = VehicleInfo(id, d, GeoPosition(lat, lon, spd, h), s, f, o)
        }

        fun summarize(r: Any): String {
            if (r is VehicleInfo) return r.vehicle_id + " " + r.driver_name
            if (r is Array<*> && r.isArrayOf<VehicleInfo>()) return r.size.toString() + " vehicles"
            return r.toString()
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  3. CONSUMER (Client)
    // ═══════════════════════════════════════════════════════════
    internal class ConsumerClient {
        var supplierHost: String? = null
        var supplierPort: Int = 0

        @Throws(Exception::class)
        fun start(nsHost: String?, nsPort: Int) {
            log("CON", "╔══════════════════════════════════════════════╗")
            log("CON", "║   VehicleTracker — Consumer                  ║")
            log("CON", "╚══════════════════════════════════════════════╝")

            // ── Resolve from Naming Service ──
            resolve(nsHost, nsPort)

            log("CON", "")
            log("CON", "  ═══ Calling Supplier Operations ═══════════")
            log("CON", "")

            // ── ping() ──
            call("ping")

            // ── getVehicleCount() ──
            call("getVehicleCount")

            // ── listVehicles() ──
            val listResp = call("listVehicles")

            // ── getVehicle("VH-0001") ──
            call("getVehicle", "VH-0001")

            // ── getVehicle("VH-0003") ──
            call("getVehicle", "VH-0003")

            // ── updatePosition("VH-0002", ...) ──
            call(
                "updatePosition", "VH-0002",
                GeoPosition(39.94, 32.87, 55.0f, 45.toShort())
            )

            // ── getVehicle("VH-0002") — verify update ──
            call("getVehicle", "VH-0002")

            // ── getVehicle("VH-9999") — expect VehicleNotFound ──
            call("getVehicle", "VH-9999")

            // ── Rapid-fire test ──
            log("CON", "")
            log("CON", "  ═══ Rapid-Fire Test (30 calls) ════════════")
            rapidFire(30)

            log("CON", "")
            log("CON", "  ════════════════════════════════════════════")
            log("CON", "  All operations completed successfully!")
            log("CON", "  ════════════════════════════════════════════")
        }

        @Throws(Exception::class)
        fun resolve(nsHost: String?, nsPort: Int) {
            val name = "FleetManagement/VehicleTracker"
            log("CON", "  Resolving: $name from $nsHost:$nsPort")

            Socket(nsHost, nsPort).use { ns ->
                ObjectOutputStream(ns.getOutputStream()).use { out ->
                    ObjectInputStream(ns.getInputStream()).use { `in` ->
                        out.writeObject(NamingRequest("resolve", name))
                        out.flush()

                        val resp = `in`.readObject() as NamingResponse
                        if (!resp.success) throw RuntimeException(resp.error)

                        supplierHost = resp.host
                        supplierPort = resp.port
                        log("CON", "  ✓ Resolved: $supplierHost:$supplierPort")
                    }
                }
            }
        }

        fun invoke(op: String?, vararg args: Any?): CORBAResponse {
            try {
                Socket(supplierHost, supplierPort).use { sock ->
                    ObjectOutputStream(sock.getOutputStream()).use { out ->
                        ObjectInputStream(sock.getInputStream()).use { `in` ->
                            out.writeObject(CORBARequest(op, args))
                            out.flush()
                            return `in`.readObject() as CORBAResponse
                        }
                    }
                }
            } catch (e: Exception) {
                return CORBAResponse.Companion.error("COMM_FAILURE", e.message)
            }
        }

        fun call(op: String, vararg args: Any?): CORBAResponse {
            // Build args string for display
            var argStr = ""
            if (args.isNotEmpty()) {
                val parts = ArrayList<String?>()
                for (a in args) {
                    if (a is String) parts.add("\"" + a + "\"")
                    else if (a is GeoPosition) parts.add(
                        String.format(
                            "GeoPosition{%.2f, %.2f, %.1f}",
                            a.latitude,
                            a.longitude,
                            a.speed_kmh
                        )
                    )
                    else parts.add(a.toString())
                }
                argStr = parts.joinToString(", ")
            }

            log("CON", "  → $op($argStr)")

            val t0 = System.nanoTime()
            val resp = invoke(op, *args)
            val ms = (System.nanoTime() - t0) / 1000000.0

            if (resp.success) {
                formatResult(resp.result, ms)
            } else {
                log(
                    "CON", kotlin.String.format(
                        "    ✗ %s: %s  (%.1fms)",
                        resp.errorType, resp.errorMessage, ms
                    )
                )
            }
            return resp
        }

        fun formatResult(result: Any?, ms: Double) {
            if (result is VehicleInfo) {
                log("CON", kotlin.String.format("    ← %s  (%.1fms)", result, ms))
            } else if (result is Array<*> && result.isArrayOf<VehicleInfo>()) {
                log("CON", kotlin.String.format("    ← %d vehicles:  (%.1fms)", result.size, ms))
                for (v in result) {
                    log("CON", "      $v")
                }
            } else {
                log("CON", kotlin.String.format("    ← %s  (%.1fms)", result, ms))
            }
        }

        fun rapidFire(count: Int) {
            val ids = arrayOf<kotlin.String?>("VH-0001", "VH-0002", "VH-0003", "VH-0004", "VH-0005")
            val t0 = System.nanoTime()
            var ok = 0

            for (i in 0..<count) {
                val r = when (i % 4) {
                    0 -> invoke("ping")
                    1 -> invoke("getVehicle", ids[i % ids.size])
                    2 -> invoke("getVehicleCount")
                    3 -> invoke("listVehicles")
                    else -> null
                }
                if (r != null && r.success) ok++
            }

            val elapsed = (System.nanoTime() - t0) / 1000000.0
            log(
                "CON", kotlin.String.format(
                    "  %d/%d calls OK in %.0fms (%.0f calls/sec)",
                    ok, count, elapsed, count / (elapsed / 1000.0)
                )
            )
        }
    }
}