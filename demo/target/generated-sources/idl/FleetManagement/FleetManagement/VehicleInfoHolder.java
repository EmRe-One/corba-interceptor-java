package FleetManagement.FleetManagement;

/**
 * Generated from IDL struct "VehicleInfo".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleInfoHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.FleetManagement.VehicleInfo value;

	public VehicleInfoHolder ()
	{
	}
	public VehicleInfoHolder(final FleetManagement.FleetManagement.VehicleInfo initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.FleetManagement.VehicleInfoHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.FleetManagement.VehicleInfoHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.FleetManagement.VehicleInfoHelper.write(_out, value);
	}
}
