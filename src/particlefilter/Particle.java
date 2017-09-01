package particlefilter;

import java.util.Random;

import simulator.Utils;
import util.Point2D;

/**
 *
 * @author  LeslieXong
 */
public class Particle extends Point2D{
    public float senseStd; //stand deviation
    public float direction;
    public float worldWidth;
    public float worldHeight;
    public double weight = 0;
    public Point2D[] landmarks;
    private Random random;
    
    /**
     * Default constructor for a particle
     * 
     * @param landmarks Point array of landmark points for the particle
     * @param width width of the particle's world in pixels
     * @param height height of the particle's world in pixels
     */
    public Particle(Point2D[] landmarks, float width, float height) {
        this.landmarks = landmarks;
        this.worldWidth = width;
        this.worldHeight = height;
        random = new Random();
        x = random.nextFloat() * width;
        y = random.nextFloat() * height;
        direction = random.nextFloat() * 2f * ((float)Math.PI);
        senseStd = 0f;        
    }

	/**
     * Sets the position of the particle and its relative probability
     * 
     * @param x new x position of the particle
     * @param y new y position of the particle
     * @param direction new orientation of the particle, in radians
     * @param prob new probability of the particle between 0 and 1
     * @throws Exception 
     */
    public void set(float x, float y, float direction, double prob) throws Exception {
        if(x < 0 || x >= worldWidth) {
            throw new Exception("X coordinate out of bounds");
        }
        if(y < 0 || y >= worldHeight) {
            throw new Exception("Y coordinate out of bounds");
        }
        if(direction < 0 || direction >= 2 * Math.PI) {
            throw new Exception("X coordinate out of bounds");
        }
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.weight = prob;
    }
    
    /**
     * Sets the noise of the particles measurements and movements
     * 
     * @param Fnoise noise of particle in forward movement
     * @param Onoise noise of particle in direction measurement
     * @param Snoise noise of particle in sensing position
     */
    public void setSenseNoise(float Snoise) {
        this.senseStd = Snoise;
    }
    
 
    /**
     * Moves the particle's position(propagate)
     * TODO INS error should consider system error other than random error
     * @param direction value, in radians
     * @param forward move value, must be >= 0
     */
    public void move(float direction, float forward) throws Exception {
        if(forward < 0) {
            throw new Exception("target cannot move backwards");
        }
        
        this.direction = circle(direction, 2f * (float)Math.PI);;
       
        x += Math.cos(direction) * forward;
        y += Math.sin(direction) * forward;
        x = circle(x, worldWidth);   //TODO in real application this should be removed
        y = circle(y, worldHeight);
    }
    
    /**
     * Calculates the probability of particle based on all measurement
     * 
     * @param measurement distance measurements 
     */
    public void likelihood(float[] measurement) {
        double likeli = 1.0;
        for(int i=0;i<landmarks.length;i++) {
            float dist = (float) Utils.distance(x, y, landmarks[i].x, landmarks[i].y);            
            likeli *= Utils.gaussianPdf(dist, senseStd, measurement[i]);            
        }      
        weight = likeli;
    }
    
    /**
     * just use the nearest on beacon as measurement
     * @param measurement
     */
    public void likelihood2(float[] measurement) {
    	float min=measurement[0];
    	int minIndex=0;
    	for (int i=0;i<measurement.length;i++)
		{
    		if (measurement[i]<min)
    		{
    			min=measurement[i];
    			minIndex=i;
    		}
		}
    	
    	float dist = (float) Utils.distance(x, y, landmarks[minIndex].x, landmarks[minIndex].y);   
        weight =  Utils.gaussianPdf(dist, senseStd, measurement[minIndex]);  ;
    }
    
    private float circle(float num, float length) {         
        while(num > length - 1) num -= length;
        while(num < 0) num += length;
        return num;       
    }
    
    @Override
    public String toString() {
        return "[x=" + x + " y=" + y + " orient=" + Math.toDegrees(direction) + " prob=" +weight +  "]";
    }
    
}