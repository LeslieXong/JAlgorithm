package simulator;

import java.util.Random;

import util.Point2D;

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
	
	
	  /**
     * Use senseNoise to simulate the distance measurement of the particle to each of its landmarks
     * 
     * @return a float array of distances to landmarks
     */
    public static float[] simulateSense(Point2D[] landmarks,float senseNoise,Point2D currentPosition) {
        float[] ret = new float[landmarks.length];
        Random random =new Random();
        
        for(int i=0;i<landmarks.length;i++){
            float dist = (float) Utils.distance(currentPosition.x, currentPosition.y, landmarks[i].x, landmarks[i].y);
            ret[i] = dist + (float)random.nextGaussian() * senseNoise;
        }       
        return ret;
    }    
    
}
