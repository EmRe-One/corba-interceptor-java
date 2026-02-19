package FleetManagement.FleetManagement;
/**
 * Generated from IDL enum "VehicleStatus".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at Feb 17, 2026, 10:26:44 PM
 */

public final class VehicleStatus
	implements org.omg.CORBA.portable.IDLEntity
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	private int value = -1;
	public static final int _MOVING = 0;
	public static final VehicleStatus MOVING = new VehicleStatus(_MOVING);
	public static final int _IDLE = 1;
	public static final VehicleStatus IDLE = new VehicleStatus(_IDLE);
	public static final int _PARKED = 2;
	public static final VehicleStatus PARKED = new VehicleStatus(_PARKED);
	public static final int _MAINTENANCE = 3;
	public static final VehicleStatus MAINTENANCE = new VehicleStatus(_MAINTENANCE);
	public int value()
	{
		return value;
	}
	public static VehicleStatus from_int(int value)
	{
		switch (value) {
			case _MOVING: return MOVING;
			case _IDLE: return IDLE;
			case _PARKED: return PARKED;
			case _MAINTENANCE: return MAINTENANCE;
			default: throw new org.omg.CORBA.BAD_PARAM();
		}
	}
	public String toString()
	{
		switch (value) {
			case _MOVING: return "MOVING";
			case _IDLE: return "IDLE";
			case _PARKED: return "PARKED";
			case _MAINTENANCE: return "MAINTENANCE";
			default: throw new org.omg.CORBA.BAD_PARAM();
		}
	}
	protected VehicleStatus(int i)
	{
		value = i;
	}
	/**
	 * Designate replacement object when deserialized from stream. See
	 * http://www.omg.org/docs/ptc/02-01-03.htm#Issue4271
	 *
	 * @throws java.io.ObjectStreamException
	 */
	java.lang.Object readResolve()
	throws java.io.ObjectStreamException
	{
		return from_int(value());
	}
}
