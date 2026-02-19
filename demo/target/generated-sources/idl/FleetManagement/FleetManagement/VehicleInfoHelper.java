package FleetManagement.FleetManagement;


/**
 * Generated from IDL struct "VehicleInfo".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class VehicleInfoHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(VehicleInfoHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_struct_tc(FleetManagement.FleetManagement.VehicleInfoHelper.id(),"VehicleInfo",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("vehicle_id", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("driver_name", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("position", org.omg.CORBA.ORB.init().create_struct_tc(FleetManagement.FleetManagement.GeoPositionHelper.id(),"GeoPosition",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("latitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("longitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("speed_kmh", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)), null),new org.omg.CORBA.StructMember("heading", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null)}), null),new org.omg.CORBA.StructMember("status", org.omg.CORBA.ORB.init().create_enum_tc(FleetManagement.FleetManagement.VehicleStatusHelper.id(),"VehicleStatus",new String[]{"MOVING","IDLE","PARKED","MAINTENANCE"}), null),new org.omg.CORBA.StructMember("fuel_level_pct", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)), null),new org.omg.CORBA.StructMember("odometer_km", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(5)), null)});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final FleetManagement.FleetManagement.VehicleInfo s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static FleetManagement.FleetManagement.VehicleInfo extract (final org.omg.CORBA.Any any)
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
		return "IDL:FleetManagement/VehicleInfo:1.0";
	}
	public static FleetManagement.FleetManagement.VehicleInfo read (final org.omg.CORBA.portable.InputStream in)
	{
		FleetManagement.FleetManagement.VehicleInfo result = new FleetManagement.FleetManagement.VehicleInfo();
		result.vehicle_id=in.read_string();
		result.driver_name=in.read_string();
		result.position=FleetManagement.FleetManagement.GeoPositionHelper.read(in);
		result.status=FleetManagement.FleetManagement.VehicleStatusHelper.read(in);
		result.fuel_level_pct=in.read_float();
		result.odometer_km=in.read_ulong();
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final FleetManagement.FleetManagement.VehicleInfo s)
	{
		java.lang.String tmpResult0 = s.vehicle_id;
out.write_string( tmpResult0 );
		java.lang.String tmpResult1 = s.driver_name;
out.write_string( tmpResult1 );
		FleetManagement.FleetManagement.GeoPositionHelper.write(out,s.position);
		FleetManagement.FleetManagement.VehicleStatusHelper.write(out,s.status);
		out.write_float(s.fuel_level_pct);
		out.write_ulong(s.odometer_km);
	}
}
