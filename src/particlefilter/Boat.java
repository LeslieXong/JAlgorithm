package particlefilter;
import java.util.Random;

public class Boat
{
	public static double ground_speed_std = 0.5;
	public static double pos_std = 2.6;
	public static double heading_std = 1;
	
	public static BoatState NoisBoatState (double _x, double _y, double _heading, double _dheading, double _ground_speed)
	{
		Random r = new  Random();
		return new BoatState(_x + r.nextGaussian()*pos_std, 
						 _y + r.nextGaussian()*pos_std,
						 _heading +r.nextGaussian()*heading_std,
						 _ground_speed + r.nextGaussian()*ground_speed_std);
	}
	
}

