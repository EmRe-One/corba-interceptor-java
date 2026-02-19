package FleetManagement.FleetManagement;

/**
 * Generated from IDL exception "VehicleNotFound".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleNotFound
	extends org.omg.CORBA.UserException
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	public VehicleNotFound()
	{
		super(FleetManagement.FleetManagement.VehicleNotFoundHelper.id());
	}

	public java.lang.String vehicle_id = "";
	public java.lang.String message = "";
	public VehicleNotFound(java.lang.String _reason,java.lang.String vehicle_id, java.lang.String message)
	{
		super(_reason);
		this.vehicle_id = vehicle_id;
		this.message = message;
	}
	public VehicleNotFound(java.lang.String vehicle_id, java.lang.String message)
	{
		super(FleetManagement.FleetManagement.VehicleNotFoundHelper.id());
		this.vehicle_id = vehicle_id;
		this.message = message;
	}
}
