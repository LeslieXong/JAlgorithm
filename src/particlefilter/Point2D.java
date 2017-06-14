package particlefilter;

public class Point2D {
	public double x;
	public double y;
	
	Point2D()
	{
		this.x=0.0;
		this.y=0.0;
	}
	
	
	public Point2D(double x,double y)
	{
		this.x=x;
		this.y=y;
	}
	
	public String toString()
	{
		return String.format("x:%.3f  y:%.3f", x,y);
	}
}
