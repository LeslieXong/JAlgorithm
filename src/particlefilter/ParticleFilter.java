package particlefilter;

import java.util.LinkedList;
import java.util.Random;
import util.Point2D;

public class ParticleFilter {

    public Particle[] particles;
    int particlesNum = 0;
    private Random random = new Random();

    public ParticleFilter(int numParticles, Point2D[] landmarks, float width, float height) {
        this.particlesNum = numParticles;

        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++) {
            particles[i] = new Particle(landmarks, width, height);
        }
    }

    public void setSenseNoise(float senseNoise) {
        for (int i = 0; i < particlesNum; i++) {
            particles[i].setSenseNoise(senseNoise);
        }
    }

    /**
     * Moves the particle's position(propagate)
     * @param orient value in radian
     * @param forward move value, must be >= 0
     * @throws Exception 
     */
    public void move(float direction, float forward) throws Exception {
        for (Particle p:particles) {
            p.move(direction, forward);
        }
    }
    
    //Return the index where to insert item x in list a, assuming a is sorted.
    public int BisectLeft(LinkedList<Double> list,double num)
    {
    	int high=list.size();
    	int low=0;
    	while (low<high)
    	{
    		int mid=(low+high)/2;
    		if (list.get(mid)<num)
    			low=mid+1;
    		else
    			high=mid;
    	}
    	return low;
    }
    
    public void reSample2() throws Exception
    {
    	double sum = 0.0;
    	LinkedList<Particle> state=new LinkedList<>();
        for (Particle particle : particles)
		{
        	if (particle.weight>0)
			{
				state.add(particle);
				sum+=particle.weight;
			}
		}
        
        if(sum==0) //the should re initial particles
        {
//        	particles = new Particle[numParticles];
//        	for (int i = 0; i < numParticles; i++) {
//        		particles[i] = new Particle(landmarks, width, height);
//        	}
        }
        
        double accum = 0.0;
        LinkedList<Double> weightDistribution=new LinkedList<>();
        for (Particle particle : state)
		{
			accum += particle.weight/sum;
			weightDistribution.add(accum);
		}
        
        Particle[] new_particles = new Particle[particlesNum];
        for (int i = 0; i < particlesNum; i++) {
        	int index=BisectLeft(weightDistribution, random.nextDouble());
        	Particle particle=state.get(index);  //!can not use new_particles[i]=particle
        	new_particles[i] =new Particle(particle.landmarks, particle.worldWidth, particle.worldHeight);
            new_particles[i].set(particle.x, particle.y, particle.direction,particle.weight);
            new_particles[i].setSenseNoise(particle.senseStd);
        }
        
        particles=new_particles ;
    }
   
    public void reSample() throws Exception {
        Particle[] new_particles = new Particle[particlesNum];
        
        float beta = 0f;
        Particle best = getMapPosition();
        int index = (int) random.nextFloat() * particlesNum;
        for (int i = 0; i < particlesNum; i++) {
            beta += random.nextFloat() * 2f * best.weight;
            while (beta > particles[index].weight) {
                beta -= particles[index].weight;
                index = circle(index + 1, particlesNum);
            }
            Particle particle=particles[index];
            new_particles[i] = new Particle(particle.landmarks, particle.worldWidth, particle.worldHeight);
            new_particles[i].set(particle.x, particle.y, particle.direction, particle.weight);
            new_particles[i].setSenseNoise(particle.senseStd);
        }

        particles = new_particles;        
    }
    
    /**
     * EAP estimation:expected a posterior
     * @return
     */
    public Point2D getEapPosition(float[] measurement) {
    	double sumWeight=0; 
    	for (Particle p:particles) {
             p.likelihood(measurement);
             sumWeight+=p.weight;
         }
    	
    	//normalization
    	for (Particle p:particles) {
            p.weight/=sumWeight;
        }
    	 
        Point2D pos=new Point2D();
        for(Particle p:particles) {
            pos.x += p.x*p.weight;
            pos.y += p.y*p.weight;
        }
        
        return pos;
    }

    private int circle(int num, int length) {
        while (num > length - 1) {
            num -= length;
        }
        while (num < 0) {
            num += length;
        }
        return num;
    }
    
    /**
     * MAP estimation:Max a Posterior
     * @return
     */
    private Particle getMapPosition() {
        Particle particle = particles[0];
        for (int i = 0; i < particlesNum; i++) {
            if (particles[i].weight>particle.weight){
                particle = particles[i];
            }
        }
        return particle;
    }
    
    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < particlesNum; i++) {
            res += particles[i].toString() + "\n";
        }
        return res;
    }
   
}