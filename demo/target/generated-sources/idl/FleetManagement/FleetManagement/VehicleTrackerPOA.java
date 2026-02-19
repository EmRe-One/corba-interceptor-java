package FleetManagement.FleetManagement;


/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public abstract class VehicleTrackerPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, FleetManagement.FleetManagement.VehicleTrackerOperations
{
	static private final java.util.HashMap<String,Integer> m_opsHash = new java.util.HashMap<String,Integer>();
	static
	{
		m_opsHash.put ( "getVehicle", Integer.valueOf(0));
		m_opsHash.put ( "ping", Integer.valueOf(1));
		m_opsHash.put ( "updatePosition", Integer.valueOf(2));
		m_opsHash.put ( "listVehicles", Integer.valueOf(3));
		m_opsHash.put ( "shutdown", Integer.valueOf(4));
		m_opsHash.put ( "getVehicleCount", Integer.valueOf(5));
	}
	private String[] ids = {"IDL:FleetManagement/VehicleTracker:1.0"};
	public FleetManagement.FleetManagement.VehicleTracker _this()
	{
		org.omg.CORBA.Object __o = _this_object() ;
		FleetManagement.FleetManagement.VehicleTracker __r = FleetManagement.FleetManagement.VehicleTrackerHelper.narrow(__o);
		return __r;
	}
	public FleetManagement.FleetManagement.VehicleTracker _this(org.omg.CORBA.ORB orb)
	{
		org.omg.CORBA.Object __o = _this_object(orb) ;
		FleetManagement.FleetManagement.VehicleTracker __r = FleetManagement.FleetManagement.VehicleTrackerHelper.narrow(__o);
		return __r;
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // getVehicle
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				_out = handler.createReply();
				FleetManagement.FleetManagement.VehicleInfoHelper.write(_out,getVehicle(_arg0));
			}
			catch(FleetManagement.FleetManagement.VehicleNotFound _ex0)
			{
				_out = handler.createExceptionReply();
				FleetManagement.FleetManagement.VehicleNotFoundHelper.write(_out, _ex0);
			}
				break;
			}
			case 1: // ping
			{
				_out = handler.createReply();
				java.lang.String tmpResult6 = ping();
_out.write_string( tmpResult6 );
				break;
			}
			case 2: // updatePosition
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				FleetManagement.FleetManagement.GeoPosition _arg1=FleetManagement.FleetManagement.GeoPositionHelper.read(_input);
				_out = handler.createReply();
				updatePosition(_arg0,_arg1);
			}
			catch(FleetManagement.FleetManagement.VehicleNotFound _ex0)
			{
				_out = handler.createExceptionReply();
				FleetManagement.FleetManagement.VehicleNotFoundHelper.write(_out, _ex0);
			}
				break;
			}
			case 3: // listVehicles
			{
				_out = handler.createReply();
				FleetManagement.FleetManagement.VehicleInfoListHelper.write(_out,listVehicles());
				break;
			}
			case 4: // shutdown
			{
				_out = handler.createReply();
				shutdown();
				break;
			}
			case 5: // getVehicleCount
			{
				_out = handler.createReply();
				_out.write_long(getVehicleCount());
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
