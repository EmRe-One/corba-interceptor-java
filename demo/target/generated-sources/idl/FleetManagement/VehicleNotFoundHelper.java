package FleetManagement;


/**
 * Generated from IDL exception "VehicleNotFound".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public abstract class VehicleNotFoundHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(VehicleNotFoundHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_exception_tc(FleetManagement.VehicleNotFoundHelper.id(),"VehicleNotFound",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("vehicle_id", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("message", org.omg.CORBA.ORB.init().create_string_tc(0), null)});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final FleetManagement.VehicleNotFound s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static FleetManagement.VehicleNotFound extract (final org.omg.CORBA.Any any)
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
		return "IDL:FleetManagement/VehicleNotFound:1.0";
	}
	public static FleetManagement.VehicleNotFound read (final org.omg.CORBA.portable.InputStream in)
	{
		String id = in.read_string();
		if (!id.equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id: " + id);
		java.lang.String x0;
		x0=in.read_string();
		java.lang.String x1;
		x1=in.read_string();
		final FleetManagement.VehicleNotFound result = new FleetManagement.VehicleNotFound(id, x0, x1);
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final FleetManagement.VehicleNotFound s)
	{
		out.write_string(id());
		java.lang.String tmpResult2 = s.vehicle_id;
out.write_string( tmpResult2 );
		java.lang.String tmpResult3 = s.message;
out.write_string( tmpResult3 );
	}
}
