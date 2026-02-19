package FleetManagement.FleetManagement;


/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public interface VehicleTrackerOperations
{
	/* constants */
	/* operations  */
	FleetManagement.FleetManagement.VehicleInfo getVehicle(java.lang.String vehicle_id) throws FleetManagement.FleetManagement.VehicleNotFound;
	void updatePosition(java.lang.String vehicle_id, FleetManagement.FleetManagement.GeoPosition pos) throws FleetManagement.FleetManagement.VehicleNotFound;
	FleetManagement.FleetManagement.VehicleInfo[] listVehicles();
	int getVehicleCount();
	java.lang.String ping();
	void shutdown();
}
