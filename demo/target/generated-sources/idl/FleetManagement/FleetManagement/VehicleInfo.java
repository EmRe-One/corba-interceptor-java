package FleetManagement.FleetManagement;

/**
 * Generated from IDL struct "VehicleInfo".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleInfo
	implements org.omg.CORBA.portable.IDLEntity
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	public VehicleInfo(){}
	public java.lang.String vehicle_id = "";
	public java.lang.String driver_name = "";
	public FleetManagement.FleetManagement.GeoPosition position;
	public FleetManagement.FleetManagement.VehicleStatus status;
	public float fuel_level_pct;
	public int odometer_km;
	public VehicleInfo(java.lang.String vehicle_id, java.lang.String driver_name, FleetManagement.FleetManagement.GeoPosition position, FleetManagement.FleetManagement.VehicleStatus status, float fuel_level_pct, int odometer_km)
	{
		this.vehicle_id = vehicle_id;
		this.driver_name = driver_name;
		this.position = position;
		this.status = status;
		this.fuel_level_pct = fuel_level_pct;
		this.odometer_km = odometer_km;
	}
}
