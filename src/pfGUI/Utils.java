package pfGUI;

/**
 *
 * @author LeslieXong
 */
public class Utils
{
	public static double distance(float x1, float y1, float x2, float y2)
	{
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public static double gaussianPdf(double mean, double std, double x)
	{
		return Math.exp(-(Math.pow(mean - x, 2)) / (2.0 * Math.pow(std, 2)))
				/ Math.sqrt(2.0 * Math.PI * Math.pow(std, 2));
	}
	
}
