package FleetManagement.FleetManagement;


/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class VehicleTrackerHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(VehicleTrackerHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_interface_tc("IDL:FleetManagement/VehicleTracker:1.0", "VehicleTracker");
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final FleetManagement.FleetManagement.VehicleTracker s)
	{
			any.insert_Object(s);
	}
	public static FleetManagement.FleetManagement.VehicleTracker extract(final org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object()) ;
	}
	public static String id()
	{
		return "IDL:FleetManagement/VehicleTracker:1.0";
	}
	public static VehicleTracker read(final org.omg.CORBA.portable.InputStream in)
	{
		return narrow(in.read_Object(FleetManagement.FleetManagement._VehicleTrackerStub.class));
	}
	public static void write(final org.omg.CORBA.portable.OutputStream _out, final FleetManagement.FleetManagement.VehicleTracker s)
	{
		_out.write_Object(s);
	}
	public static FleetManagement.FleetManagement.VehicleTracker narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof FleetManagement.FleetManagement.VehicleTracker)
		{
			return (FleetManagement.FleetManagement.VehicleTracker)obj;
		}
		else if (obj._is_a("IDL:FleetManagement/VehicleTracker:1.0"))
		{
			FleetManagement.FleetManagement._VehicleTrackerStub stub;
			stub = new FleetManagement.FleetManagement._VehicleTrackerStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
		else
		{
			throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
		}
	}
	public static FleetManagement.FleetManagement.VehicleTracker unchecked_narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof FleetManagement.FleetManagement.VehicleTracker)
		{
			return (FleetManagement.FleetManagement.VehicleTracker)obj;
		}
		else
		{
			FleetManagement.FleetManagement._VehicleTrackerStub stub;
			stub = new FleetManagement.FleetManagement._VehicleTrackerStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
	}
}
