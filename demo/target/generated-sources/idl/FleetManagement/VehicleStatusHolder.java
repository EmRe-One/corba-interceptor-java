package FleetManagement;
/**
 * Generated from IDL enum "VehicleStatus".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
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
