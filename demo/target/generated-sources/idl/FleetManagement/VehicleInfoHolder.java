package FleetManagement;

/**
 * Generated from IDL struct "VehicleInfo".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public final class VehicleInfoHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.VehicleInfo value;

	public VehicleInfoHolder ()
	{
	}
	public VehicleInfoHolder(final FleetManagement.VehicleInfo initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.VehicleInfoHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.VehicleInfoHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.VehicleInfoHelper.write(_out, value);
	}
}
