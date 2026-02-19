package tr.akguel.server

import FleetManagement.FleetManagement.GeoPosition
import FleetManagement.FleetManagement.VehicleInfo
import FleetManagement.FleetManagement.VehicleNotFound
import FleetManagement.FleetManagement.VehicleStatus
import FleetManagement.FleetManagement.VehicleTrackerPOA
import org.omg.CORBA.ORB
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of the VehicleTracker IDL interface.
 * Extends the generated POA skeleton.
 */
class VehicleTrackerImpl(private val orb: ORB) : VehicleTrackerPOA() {

    private val vehicles: MutableMap<String, VehicleInfo> = ConcurrentHashMap<String, VehicleInfo>()

    init {
        seedDemoData()
    }

    /**
     * Seed some demo vehicles.
     */
    private fun seedDemoData() {
        addVehicle(
            "VH-0001",
            "Ahmet Yilmaz",
            39.9208,
            32.8541,
            62.5f,
            90.toShort(),
            VehicleStatus.MOVING,
            78.5f,
            125430
        )
        addVehicle("VH-0002", "Mehmet Demir", 39.9334, 32.8597, 0.0f, 0.toShort(), VehicleStatus.PARKED, 45.2f, 89200)
        addVehicle("VH-0003", "Ayse Kaya", 39.9120, 32.8390, 45.0f, 180.toShort(), VehicleStatus.MOVING, 92.1f, 67890)
        addVehicle("VH-0004", "Fatma Ozturk", 39.9456, 32.8700, 0.0f, 0.toShort(), VehicleStatus.IDLE, 60.0f, 210340)
        addVehicle(
            "VH-0005",
            "Ali Celik",
            39.9050,
            32.8200,
            0.0f,
            0.toShort(),
            VehicleStatus.MAINTENANCE,
            15.3f,
            340500
        )
    }

    private fun addVehicle(
        id: String, driver: String?, lat: Double, lon: Double,
        speed: Float, heading: Short, status: VehicleStatus?,
        fuel: Float, odometer: Int
    ) {
        val pos: GeoPosition = GeoPosition(lat, lon, speed, heading)
        val info: VehicleInfo = VehicleInfo(id, driver, pos, status, fuel, odometer)
        vehicles[id] = info
    }

    @Throws(VehicleNotFound::class)
    public override fun getVehicle(vehicle_id: String): VehicleInfo {
        val info: VehicleInfo = vehicles.get(vehicle_id)
            ?: throw VehicleNotFound(vehicle_id, "Vehicle '$vehicle_id' not found")

        println("[Supplier] getVehicle(\"" + vehicle_id + "\") → " + info.driver_name)
        return info
    }

    @Throws(VehicleNotFound::class)
    public override fun updatePosition(vehicle_id: String, position: GeoPosition) {
        val existing: VehicleInfo = vehicles[vehicle_id]
            ?: throw VehicleNotFound(vehicle_id, "Vehicle '$vehicle_id' not found")

        val updated: VehicleInfo = VehicleInfo(
            existing.vehicle_id,
            existing.driver_name,
            position,
            if (position.speed_kmh > 0) VehicleStatus.MOVING else VehicleStatus.IDLE,
            existing.fuel_level_pct,
            existing.odometer_km
        )
        vehicles[vehicle_id] = updated

        System.out.printf(
            "[Supplier] updatePosition(\"%s\") → lat=%.4f lon=%.4f speed=%.1f%n",
            vehicle_id, position.latitude, position.longitude, position.speed_kmh
        )
    }

    public override fun listVehicles(): Array<VehicleInfo?> {
        val result: Array<VehicleInfo?> = vehicles.values.toTypedArray<VehicleInfo?>()
        println("[Supplier] listVehicles() → " + result.size + " vehicles")
        return result
    }

    override fun getVehicleCount(): Int {
        val count: Int = vehicles.size
        println("[Supplier] getVehicleCount() → $count")
        return count
    }

    public override fun ping(): String {
        println("[Supplier] ping()")
        return "pong from VehicleTracker at " + System.currentTimeMillis()
    }

    public override fun shutdown() {
        println("[Supplier] shutdown() requested — stopping ORB...")
        // Run in separate thread to avoid blocking the call
        Thread(Runnable {
            try {
                Thread.sleep(500)
            } catch (ignored: InterruptedException) {
            }
            orb.shutdown(false)
        }).start()
    }
}