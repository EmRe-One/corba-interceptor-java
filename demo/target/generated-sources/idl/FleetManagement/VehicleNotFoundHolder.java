package FleetManagement;

/**
 * Generated from IDL exception "VehicleNotFound".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public final class VehicleNotFoundHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.VehicleNotFound value;

	public VehicleNotFoundHolder ()
	{
	}
	public VehicleNotFoundHolder(final FleetManagement.VehicleNotFound initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.VehicleNotFoundHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.VehicleNotFoundHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.VehicleNotFoundHelper.write(_out, value);
	}
}
