package FleetManagement;

/**
 * Generated from IDL struct "GeoPosition".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 17.02.2026, 22:16:47
 */

public final class GeoPosition
	implements org.omg.CORBA.portable.IDLEntity
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	public GeoPosition(){}
	public double latitude;
	public double longitude;
	public float speed_kmh;
	public short heading;
	public GeoPosition(double latitude, double longitude, float speed_kmh, short heading)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed_kmh = speed_kmh;
		this.heading = heading;
	}
}
