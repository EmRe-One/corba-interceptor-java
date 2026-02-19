package FleetManagement.FleetManagement;


/**
 * Generated from IDL struct "GeoPosition".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class GeoPositionHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(GeoPositionHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_struct_tc(FleetManagement.FleetManagement.GeoPositionHelper.id(),"GeoPosition",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("latitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("longitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("speed_kmh", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)), null),new org.omg.CORBA.StructMember("heading", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null)});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final FleetManagement.FleetManagement.GeoPosition s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static FleetManagement.FleetManagement.GeoPosition extract (final org.omg.CORBA.Any any)
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
		return "IDL:FleetManagement/GeoPosition:1.0";
	}
	public static FleetManagement.FleetManagement.GeoPosition read (final org.omg.CORBA.portable.InputStream in)
	{
		FleetManagement.FleetManagement.GeoPosition result = new FleetManagement.FleetManagement.GeoPosition();
		result.latitude=in.read_double();
		result.longitude=in.read_double();
		result.speed_kmh=in.read_float();
		result.heading=in.read_short();
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final FleetManagement.FleetManagement.GeoPosition s)
	{
		out.write_double(s.latitude);
		out.write_double(s.longitude);
		out.write_float(s.speed_kmh);
		out.write_short(s.heading);
	}
}
