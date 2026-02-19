package FleetManagement;


/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public interface VehicleTrackerOperations
{
	/* constants */
	/* operations  */
	FleetManagement.VehicleInfo getVehicle(java.lang.String vehicle_id) throws FleetManagement.VehicleNotFound;
	void updatePosition(java.lang.String vehicle_id, FleetManagement.GeoPosition pos) throws FleetManagement.VehicleNotFound;
	FleetManagement.VehicleInfo[] listVehicles();
	int getVehicleCount();
	java.lang.String ping();
	void shutdown();
}
