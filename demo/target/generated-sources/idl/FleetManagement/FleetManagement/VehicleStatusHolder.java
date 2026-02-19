package FleetManagement.FleetManagement;
/**
 * Generated from IDL enum "VehicleStatus".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleStatusHolder
	implements org.omg.CORBA.portable.Streamable
{
	public VehicleStatus value;

	public VehicleStatusHolder ()
	{
	}
	public VehicleStatusHolder (final VehicleStatus initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return VehicleStatusHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = VehicleStatusHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		VehicleStatusHelper.write (out,value);
	}
}
