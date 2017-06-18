package particlefilter;

//The implementation is partially based on this paper
public class ParticleState {
	
	public ParticleState(double _x, double _y, double _heading, double _ground_speed)
	{
		x = _x;
		y = _y;
		heading = _heading;
		ground_speed = _ground_speed;
		length = 10.9;
	}
	
	public double x;
	public double y;
	public double heading; // orientation of the boat
	public double length;
	public double ground_speed;
}
