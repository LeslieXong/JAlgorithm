package particlefilter;
import java.util.Collection;
import java.util.Random;

import particlefilter.ParticleFeature;

public class Particle {
	
	ParticleState state;
	double weight;
	double dt = 0.5; //sec

	public Particle(ParticleState s, double w)
	{
		this.state = s;
		this.weight = w;
	}
	
	//dt is time step
	ParticleState Propagate(double dt)
	{
		Random r = new Random();
		double x1,y1,dx,dy, heading1, speed1; 
		double d = state.ground_speed*dt; //distance traveled
        dy = -d * Math.sin(2*Math.PI-Math.PI*state.heading/180);
        dx = d * Math.cos(2*Math.PI-Math.PI*state.heading/180);
		y1 = state.y + (dy*180)/(Utils.R*Math.PI*Math.cos(state.x*Math.PI/180));
		x1 = state.x + (dx*180)/(Utils.R*Math.PI);
		heading1 = state.heading + r.nextGaussian()*0.034; //2 degree;
		speed1 = state.ground_speed + r.nextGaussian()*1;// 1 m/s
		//we assume ground speed and angular velocity are not changing (we do not measure acceleration and angular acceleration) 
		return new ParticleState(x1, y1, heading1, speed1);		
	}
	

	public Particle ApplyFilter(Collection<Point2D> m) {
		//propagate
		ParticleState state1 = Propagate(dt);
		double weight1 = 1.0;
		for (Point2D item : m)
		{
			weight1 *= this.Likelihood(item,ParticleFeature.pos_std*ParticleFeature.pos_std);
		}
		return new Particle(state1, weight1);
	}
	

	ParticleState Propagate(Point2D pdr)
	{
		Random r = new Random();
		double x1,y1, heading1, speed1; 
       
		x1 = state.x + pdr.x;
		y1 = state.y + pdr.y;
		state.x=x1; 	// update state is very important this decide the likelihood
		state.y=y1;		// The download version did not do this.
		heading1 = state.heading + r.nextGaussian()*0.034; //2 degree;
		speed1 = state.ground_speed + r.nextGaussian()*1;// 1 m/s
		//we assume ground speed and angular velocity are not changing (we do not measure acceleration and angular acceleration) 
		return new ParticleState(x1, y1, heading1, speed1);		
	}
	
	public Particle ApplyFilter(Point2D pos,Point2D pdr) {
		ParticleState state1 = Propagate(pdr);
		double weight1 = 1.0 ;
		
		weight1 *= this.Likelihood(pos, ParticleFeature.pos_std*ParticleFeature.pos_std);
		
		return new Particle(state1, weight1);
	}
	
	
	/**
	 * probability of this this measurement generate this particle state,likelihood
	 */
	public double Likelihood(Point2D m, double var)
	{
		double l = 1.0;
		l *= Utils.dnorm(state.x, m.x, var);  //naive bayes.
		l *= Utils.dnorm(state.y, m.y, var);
		return l;		
	}
	
}	