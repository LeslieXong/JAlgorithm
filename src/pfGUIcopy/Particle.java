package pfGUIcopy;

import java.util.Random;

/**
 *
 * @author Michael
 */
public class Particle {
    
    public float forwardNoise, turnNoise, senseNoise;
    public float x, y, orientation;
    public int worldWidth;
    public int worldHeight;
    public double probability = 0;
    public Point[] landmarks;
    Random random;
    
    /**
     * Default constructor for a particle
     * 
     * @param landmarks Point array of landmark points for the particle
     * @param width width of the particle's world in pixels
     * @param height height of the particle's world in pixels
     */
    public Particle(Point[] landmarks, int width, int height) {
        this.landmarks = landmarks;
        this.worldWidth = width;
        this.worldHeight = height;
        random = new Random();
        x = random.nextFloat() * width;
        y = random.nextFloat() * height;
        orientation = random.nextFloat() * 2f * ((float)Math.PI);
        forwardNoise = 0f;
        turnNoise = 0f;
        senseNoise = 0f;        
    }
    
    /**
     * Sets the position of the particle and its relative probability
     * 
     * @param x new x position of the particle
     * @param y new y position of the particle
     * @param orientation new orientation of the particle, in radians
     * @param prob new probability of the particle between 0 and 1
     * @throws Exception 
     */
    public void set(float x, float y, float orientation, double prob) throws Exception {
        if(x < 0 || x >= worldWidth) {
            throw new Exception("X coordinate out of bounds");
        }
        if(y < 0 || y >= worldHeight) {
            throw new Exception("Y coordinate out of bounds");
        }
        if(orientation < 0 || orientation >= 2 * Math.PI) {
            throw new Exception("X coordinate out of bounds");
        }
        this.x = x;
        this.y = y;
        this.orientation = orientation;
        this.probability = prob;
    }
    
    /**
     * Sets the noise of the particles measurements and movements
     * 
     * @param Fnoise noise of particle in forward movement
     * @param Tnoise noise of particle in turning
     * @param Snoise noise of particle in sensing position
     */
    public void setNoise(float Fnoise, float Tnoise, float Snoise) {
        this.forwardNoise = Fnoise;
        this.turnNoise = Tnoise;
        this.senseNoise = Snoise;
    }
    
    /**
     * Senses the distance of the particle to each of its landmarks
     * 
     * @return a float array of distances to landmarks
     */
    public float[] sense() {
        float[] ret = new float[landmarks.length];
        
        for(int i=0;i<landmarks.length;i++){
            float dist = (float) Utils.distance(x, y, landmarks[i].x, landmarks[i].y);
            ret[i] = dist + (float)random.nextGaussian() * senseNoise;
        }       
        return ret;
    }    
    
    /**
     * Moves the particle's position(propagate)
     * 
     * @param turn turn value, in degrees
     * @param forward move value, must be >= 0
     */
    public void move(float turn, float forward) throws Exception {
        if(forward < 0) {
            throw new Exception("target cannot move backwards");
        }
        orientation = orientation + turn + (float)random.nextGaussian() * turnNoise;
        orientation = circle(orientation, 2f * (float)Math.PI);
        
        double dist = forward + random.nextGaussian() * forwardNoise;
        
        x += Math.cos(orientation) * dist;
        y += Math.sin(orientation) * dist;
        x = circle(x, worldWidth);
        y = circle(y, worldHeight); 
    }
    
    /**
     * Calculates the probability of particle based on another particle's sense()
     * 
     * @param measurement distance measurements from another particle's sense()
     * @return the probability of the particle being correct, between 0 and 1
     */
    public double measurementProb(float[] measurement) {
        double prob = 1.0;
        for(int i=0;i<landmarks.length;i++) {
            float dist = (float) Utils.distance(x, y, landmarks[i].x, landmarks[i].y);            
            prob *= Utils.gaussianPdf(dist, senseNoise, measurement[i]);            
        }      
        
        probability = prob;
        return prob;
    }

    private float circle(float num, float length) {         
        while(num > length - 1) num -= length;
        while(num < 0) num += length;
        return num;       
    }
    
    @Override
    public String toString() {
        return "[x=" + x + " y=" + y + " orient=" + Math.toDegrees(orientation) + " prob=" +probability +  "]";
    }
    
}