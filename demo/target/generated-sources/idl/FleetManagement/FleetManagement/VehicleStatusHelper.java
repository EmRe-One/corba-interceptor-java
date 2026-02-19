package FleetManagement.FleetManagement;
/**
 * Generated from IDL enum "VehicleStatus".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class VehicleStatusHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(VehicleStatusHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_enum_tc(FleetManagement.FleetManagement.VehicleStatusHelper.id(),"VehicleStatus",new String[]{"MOVING","IDLE","PARKED","MAINTENANCE"});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final FleetManagement.FleetManagement.VehicleStatus s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static FleetManagement.FleetManagement.VehicleStatus extract (final org.omg.CORBA.Any any)
	{
		org.omg.CORBA.portable.InputStream in = any.create_input_stream();
		try
		{
			return read (in);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (java.io.IOException e)
			{
			throw new RuntimeException("Unexpected exception " + e.toString() );
			}
		}
	}

	public static String id()
	{
		return "IDL:FleetManagement/VehicleStatus:1.0";
	}
	public static VehicleStatus read (final org.omg.CORBA.portable.InputStream in)
	{
		return VehicleStatus.from_int(in.read_long());
	}

	public static void write (final org.omg.CORBA.portable.OutputStream out, final VehicleStatus s)
	{
		out.write_long(s.value());
	}
}
