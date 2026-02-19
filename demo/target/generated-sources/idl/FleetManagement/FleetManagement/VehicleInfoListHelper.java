package FleetManagement.FleetManagement;

/**
 * Generated from IDL alias "VehicleInfoList".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class VehicleInfoListHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;

	public static void insert (org.omg.CORBA.Any any, FleetManagement.FleetManagement.VehicleInfo[] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static FleetManagement.FleetManagement.VehicleInfo[] extract (final org.omg.CORBA.Any any)
	{
		if ( any.type().kind() == org.omg.CORBA.TCKind.tk_null)
		{
			throw new org.omg.CORBA.BAD_OPERATION ("Can't extract from Any with null type.");
		}
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(VehicleInfoListHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_alias_tc(FleetManagement.FleetManagement.VehicleInfoListHelper.id(), "VehicleInfoList",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().create_struct_tc(FleetManagement.FleetManagement.VehicleInfoHelper.id(),"VehicleInfo",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("vehicle_id", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("driver_name", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("position", org.omg.CORBA.ORB.init().create_struct_tc(FleetManagement.FleetManagement.GeoPositionHelper.id(),"GeoPosition",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("latitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("longitude", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)), null),new org.omg.CORBA.StructMember("speed_kmh", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)), null),new org.omg.CORBA.StructMember("heading", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null)}), null),new org.omg.CORBA.StructMember("status", org.omg.CORBA.ORB.init().create_enum_tc(FleetManagement.FleetManagement.VehicleStatusHelper.id(),"VehicleStatus",new String[]{"MOVING","IDLE","PARKED","MAINTENANCE"}), null),new org.omg.CORBA.StructMember("fuel_level_pct", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)), null),new org.omg.CORBA.StructMember("odometer_km", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(5)), null)})));
				}
			}
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:FleetManagement/VehicleInfoList:1.0";
	}
	public static FleetManagement.FleetManagement.VehicleInfo[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		FleetManagement.FleetManagement.VehicleInfo[] _result;
		int _l_result0 = _in.read_long();
		try
		{
			 int x = _in.available();
			 if ( x > 0 && _l_result0 > x )
				{
					throw new org.omg.CORBA.MARSHAL("Sequence length too large. Only " + x + " available and trying to assign " + _l_result0);
				}
		}
		catch (java.io.IOException e)
		{
		}
		_result = new FleetManagement.FleetManagement.VehicleInfo[_l_result0];
		for (int i=0;i<_result.length;i++)
		{
			_result[i]=FleetManagement.FleetManagement.VehicleInfoHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, FleetManagement.FleetManagement.VehicleInfo[] _s)
	{
		
		_out.write_long(_s.length);
		for (int i=0; i<_s.length;i++)
		{
			FleetManagement.FleetManagement.VehicleInfoHelper.write(_out,_s[i]);
		}

	}
}
