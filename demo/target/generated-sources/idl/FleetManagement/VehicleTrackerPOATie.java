package FleetManagement;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "VehicleTracker".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
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
	public FleetManagement.VehicleTracker _this()
	{
		org.omg.CORBA.Object __o = _this_object() ;
		FleetManagement.VehicleTracker __r = FleetManagement.VehicleTrackerHelper.narrow(__o);
		return __r;
	}
	public FleetManagement.VehicleTracker _this(org.omg.CORBA.ORB orb)
	{
		org.omg.CORBA.Object __o = _this_object(orb) ;
		FleetManagement.VehicleTracker __r = FleetManagement.VehicleTrackerHelper.narrow(__o);
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
	public FleetManagement.VehicleInfo getVehicle(java.lang.String vehicle_id) throws FleetManagement.VehicleNotFound
	{
		return _delegate.getVehicle(vehicle_id);
	}

	public java.lang.String ping()
	{
		return _delegate.ping();
	}

	public FleetManagement.VehicleInfo[] listVehicles()
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

	public void updatePosition(java.lang.String vehicle_id, FleetManagement.GeoPosition pos) throws FleetManagement.VehicleNotFound
	{
_delegate.updatePosition(vehicle_id,pos);
	}

}
