package FleetManagement.FleetManagement;

/**
 * Generated from IDL exception "VehicleNotFound".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleNotFoundHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.FleetManagement.VehicleNotFound value;

	public VehicleNotFoundHolder ()
	{
	}
	public VehicleNotFoundHolder(final FleetManagement.FleetManagement.VehicleNotFound initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.FleetManagement.VehicleNotFoundHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.FleetManagement.VehicleNotFoundHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.FleetManagement.VehicleNotFoundHelper.write(_out, value);
	}
}
