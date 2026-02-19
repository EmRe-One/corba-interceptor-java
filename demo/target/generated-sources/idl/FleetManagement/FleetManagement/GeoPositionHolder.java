package FleetManagement.FleetManagement;

/**
 * Generated from IDL struct "GeoPosition".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class GeoPositionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.FleetManagement.GeoPosition value;

	public GeoPositionHolder ()
	{
	}
	public GeoPositionHolder(final FleetManagement.FleetManagement.GeoPosition initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.FleetManagement.GeoPositionHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.FleetManagement.GeoPositionHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.FleetManagement.GeoPositionHelper.write(_out, value);
	}
}
