package FleetManagement.FleetManagement;

/**
 * Generated from IDL alias "VehicleInfoList".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleInfoListHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.FleetManagement.VehicleInfo[] value;

	public VehicleInfoListHolder ()
	{
	}
	public VehicleInfoListHolder (final FleetManagement.FleetManagement.VehicleInfo[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return VehicleInfoListHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = VehicleInfoListHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		VehicleInfoListHelper.write (out,value);
	}
}
