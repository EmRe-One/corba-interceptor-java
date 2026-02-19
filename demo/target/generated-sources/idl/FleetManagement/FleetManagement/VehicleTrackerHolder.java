package FleetManagement.FleetManagement;

/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleTrackerHolder	implements org.omg.CORBA.portable.Streamable{
	 public VehicleTracker value;
	public VehicleTrackerHolder()
	{
	}
	public VehicleTrackerHolder (final VehicleTracker initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return VehicleTrackerHelper.type();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = VehicleTrackerHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		VehicleTrackerHelper.write (_out,value);
	}
}
