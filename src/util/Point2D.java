package util;

/**
 *
 * @author LeslieXong
 */
public class Point2D {
    
    public float x, y;

    public Point2D() { }

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }
     
    public Point2D(double x,double y)
    {
    	this.x=(float)x;
    	this.y=(float)y;
    }
}