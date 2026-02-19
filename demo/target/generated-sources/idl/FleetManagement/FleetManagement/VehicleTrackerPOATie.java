package FleetManagement.FleetManagement;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public class VehicleTrackerPOATie
	extends VehicleTrackerPOA
{
	private VehicleTrackerOperations _delegate;

	private POA _poa;
	public VehicleTrackerPOATie(VehicleTrackerOperations delegate)
	{
		_delegate = delegate;
	}
	public VehicleTrackerPOATie(VehicleTrackerOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
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
	public VehicleTrackerOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(VehicleTrackerOperations delegate)
	{
		_delegate = delegate;
	}
	public POA _default_POA()
	{
		if (_poa != null)
		{
			return _poa;
		}
		return super._default_POA();
	}
	public FleetManagement.FleetManagement.VehicleInfo getVehicle(java.lang.String vehicle_id) throws FleetManagement.FleetManagement.VehicleNotFound
	{
		return _delegate.getVehicle(vehicle_id);
	}

	public java.lang.String ping()
	{
		return _delegate.ping();
	}

	public void updatePosition(java.lang.String vehicle_id, FleetManagement.FleetManagement.GeoPosition pos) throws FleetManagement.FleetManagement.VehicleNotFound
	{
_delegate.updatePosition(vehicle_id,pos);
	}

	public FleetManagement.FleetManagement.VehicleInfo[] listVehicles()
	{
		return _delegate.listVehicles();
	}

	public void shutdown()
	{
_delegate.shutdown();
	}

	public int getVehicleCount()
	{
		return _delegate.getVehicleCount();
	}

}
