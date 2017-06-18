package particlefilter;

//some of the code is borrowed from here
//http://www.movable-type.co.uk/scripts/latlong.html

public final class Utils {
	//helper classes
    //static double R = 6371; //Mean radius of earth in km
	//semi-minor axis (Earth's center to a pole distance)
	static double earth_b =  1000*6356.7523142; 
	//semi-major axis (Earth's center to equator distance)
	static double earth_a =  1000*6378.1370; 
	//HARDCODED for Chicago
	static double R = Utils.geocentric_radius(41.88194); 
	
	//helper class for storing lat and lon
	public static final class GeoPoint {
		public double lat;
		public double lon;
		public GeoPoint(double _lat, double _lon)
		{
			this.lat = _lat;
			this.lon = _lon;
		}
	}
	
	public static GeoPoint proj_to_world_mercator(double lat, double lon) {
		double x,y;
		lat = Utils.to_rad(lat);
		lon = Utils.to_rad(lon);
		double lon0 = Utils.to_rad(-87.627778);
		
		x = Utils.earth_a*(lon - lon0);
		y = Utils.earth_a*(Math.log(Math.tan(lat/2 + Math.PI/4)));
		return new GeoPoint(x,y);
		
	}
	
	//Calculates geocentric radius of the earth at a given latitude
	//http://en.wikipedia.org/wiki/Earth_radius
	public static double geocentric_radius(double lat){
		lat = Utils.to_rad(lat);
		
		
		return Math.sqrt((Math.pow(earth_a*earth_a*Math.cos(lat),2) +Math.pow(earth_b*earth_b*Math.sin(lat),2))/
				(Math.pow(earth_a*Math.cos(lat),2) +Math.pow(earth_b*Math.sin(lat),2)) );
	}
	//Converts degrees to radian
	public static double to_rad(double angle)
	{
		return ((angle%360)*Math.PI / 180.0);
	}
//	converts radian to degree
	public static double to_deg(double rad)
	{
		return rad*180.0 / Math.PI;
	}
	//returns the value of Gaussian pdf with mu=mean and std^2=varience ec=valuated at x
	public static double dnorm(double x, double mean, double varience)
	{
		return Math.exp(-Math.pow(x-mean,2)/(2*varience))/(Math.sqrt(varience)*Math.sqrt(2*Math.PI));
	}
	
	//Updates the lat and lon by moving the point by d meters in the direction given by bearing
	public static void move_lat_lon(GeoPoint pt, double d, double bearing)
	{
		double lat1, lat2, lon1,lon2, theta, delta;
 		lat1 = Utils.to_rad(pt.lat);
		lon1 = Utils.to_rad(pt.lon);
		theta = Utils.to_rad(bearing);
		delta = d/(R);
		
		lat2 = Math.asin(Math.sin(lat1)*Math.cos(delta) + 
				Math.cos(lat1)*Math.sin(delta)*Math.cos(theta));
		lon2 = lon1 + Math.atan2(Math.sin(theta)*Math.sin(delta)*Math.cos(lat1), Math.cos(delta) - Math.sin(lat1)*Math.sin(lat2));
		lon2 = (lon2 + 3*Math.PI) % (2*Math.PI) - Math.PI; // normalize to -180..180
		pt.lat = Utils.to_deg(lat2);
		pt.lon = Utils.to_deg(lon2);
	}
	
	//Converts degrees, minutes and seconds to decomal form
	public static double to_decimal_degrees(int degree, int minute, int second)
	{
		return degree + minute/60.0 + second/3600.0;
	}
	//returns distance between 2 points
	public static double distance(double lat1, double lon1, double lat2, double lon2)
	{
		lat1 = Utils.to_rad(lat1);
		lat2 = Utils.to_rad(lat2);
		lon1 = Utils.to_rad(lon1);
		lon2 = Utils.to_rad(lon2);
		double dlat = lat2-lat1;
		double dlon = lon2-lon1;
		double a = Math.sin(dlat/2) * Math.sin(dlat/2) + 
				Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlon/2)*Math.sin(dlon/2);
		double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
		return R*c;		
	}
	
	//calculates the resulting bearing when going from pt1 to pt2
	public static double bearing(double lat1, double lon1, double lat2, double lon2)
	{
		double theta2 = Utils.to_rad(lat1);
		double theta1 = Utils.to_rad(lat2);
		double dlon = Utils.to_rad(lon1-lon2);
		double y = Math.sin(dlon)*Math.cos(theta2);
		double x = Math.cos(theta1)*Math.sin(theta2) - 
				Math.sin(theta1)*Math.cos(theta2)*Math.cos(dlon);
		double theta = Math.atan2(y, x);
		return (Utils.to_deg(theta)+180) % 360;
	}
}
