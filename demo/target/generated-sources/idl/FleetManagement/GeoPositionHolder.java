package FleetManagement;

/**
 * Generated from IDL struct "GeoPosition".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public final class GeoPositionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public FleetManagement.GeoPosition value;

	public GeoPositionHolder ()
	{
	}
	public GeoPositionHolder(final FleetManagement.GeoPosition initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return FleetManagement.GeoPositionHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = FleetManagement.GeoPositionHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		FleetManagement.GeoPositionHelper.write(_out, value);
	}
}
