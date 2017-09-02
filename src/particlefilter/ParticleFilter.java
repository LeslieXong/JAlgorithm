package particlefilter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import util.Point2D;

public class ParticleFilter
{

	public Particle[] particles;
	int particlesNum = 0;
	private Random random = new Random();

	public ParticleFilter(int numParticles, Point2D[] landmarks, float width, float height)
	{
		this.particlesNum = numParticles;

		particles = new Particle[numParticles];
		for (int i = 0; i < numParticles; i++)
		{
			particles[i] = new Particle(landmarks, width, height);
		}
	}

	public void setSenseNoise(float senseNoise)
	{
		for (int i = 0; i < particlesNum; i++)
		{
			particles[i].setSenseNoise(senseNoise);
		}
	}

	/**
	 * Moves the particle's position(propagate)
	 * 
	 * @param orient
	 *            value in radian
	 * @param forward
	 *            move value, must be >= 0
	 * @throws Exception
	 */
	public void move(float direction, float forward) throws Exception
	{
		for (Particle p : particles)
		{
			p.move(direction, forward);
		}
	}

	//Return the index where to insert item x in list a, assuming a is sorted.
	public int BisectLeft(LinkedList<Double> list, double num)
	{
		int high = list.size();
		int low = 0;
		while (low < high)
		{
			int mid = (low + high) / 2;
			if (list.get(mid) < num)
				low = mid + 1;
			else
				high = mid;
		}
		return low;
	}

	/**
	 * MSV (minimum sampling variance) resampling method: T. Li, G. Villarrubia, S. Sun, J. M. Corchado, J. Bajo.
	 * Resampling methods for particle filtering: identical distribution, a new method and comparable study, Frontiers
	 * of Information Technology & Electronic Engineering, DOI:10.1631/FITEE.1500199
	 * 
	 * @throws Exception
	 */
	public void reSample3() throws Exception
	{
		double sum = 0.0;
		for (Particle particle : particles)
		{
			sum += particle.weight;
		}

		for (Particle particle : particles)
		{
			particle.weight /= sum;
		}

		int[] Ns = new int[particlesNum];
		int i = 0,sums=0;
		for (Particle p : particles)
		{
			Ns[i] = (int) (particlesNum * p.weight);
			sums+=Ns[i];
			i++;
		}
        
		System.out.println("sums"+sums);
		//int[] index1 = sortAndOriginalIndex(weight);
		int[] index = new int[particlesNum];
		int m = 0, n = 0;
		while (n < particlesNum)
		{	
			int count = 1;
			while (count <= Ns[n])
			{
				index[m] = n;
				m++;
				count++;
			}
			n++;
		}

		for(;m<particlesNum;m++)
		{
			index[m]=random.nextInt(particlesNum);
		}
		
		Particle[] new_particles = new Particle[particlesNum];
		for (i = 0; i < particlesNum; i++)
		{
			Particle particle = particles[index[i]];  //!can not use new_particles[i]=particle
			new_particles[i] = new Particle(particle.landmarks, particle.worldWidth, particle.worldHeight);
			new_particles[i].set(particle.x, particle.y, particle.direction, particle.weight);
			new_particles[i].setSenseNoise(particle.senseStd);
		}
		particles = new_particles;
	}

	public int[] sortAndOriginalIndex(double[] arr)
	{
		int[] sortedIndex = new int[arr.length];
		TreeMap<Double, Integer> map = new TreeMap<Double, Integer>();
		for (int i = 0; i < arr.length; i++)
		{
			map.put(arr[i], i);
		}

		//当用Iterator 遍历TreeMap时，得到的记录是排过序的
		int n = 0;
		Iterator<Map.Entry<Double, Integer>> it = map.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<Double, Integer> me = it.next();
			sortedIndex[n++] = me.getValue();
		}
		return sortedIndex;
	}

	/**
	 * SIR resample method
	 * @throws Exception
	 */
	public void reSample2() throws Exception
	{
		LinkedList<Particle> state = new LinkedList<>();
		for (Particle particle : particles)
		{
			if (particle.weight > 0)
			{
				state.add(particle);
			}
		}

		double accum = 0.0;
		LinkedList<Double> weightDistribution = new LinkedList<>();
		for (Particle particle : state)
		{
			accum += particle.weight;
			weightDistribution.add(accum);
		}

		Particle[] new_particles = new Particle[particlesNum];
		for (int i = 0; i < particlesNum; i++)
		{
			int index = BisectLeft(weightDistribution, random.nextDouble());
			Particle particle = state.get(index);  //!can not use new_particles[i]=particle
			new_particles[i] = new Particle(particle.landmarks, particle.worldWidth, particle.worldHeight);
			new_particles[i].set(particle.x, particle.y, particle.direction, particle.weight);
			new_particles[i].setSenseNoise(particle.senseStd);
		}

		particles = new_particles;
	}

	public void reSample() throws Exception
	{
		Particle[] new_particles = new Particle[particlesNum];

		float beta = 0f;
		Particle best = getMapPosition();
		int index = (int) random.nextFloat() * particlesNum;
		for (int i = 0; i < particlesNum; i++)
		{
			beta += random.nextFloat() * 2f * best.weight;
			while (beta > particles[index].weight)
			{
				beta -= particles[index].weight;
				index = circle(index + 1, particlesNum);
			}
			Particle particle = particles[index];
			new_particles[i] = new Particle(particle.landmarks, particle.worldWidth, particle.worldHeight);
			new_particles[i].set(particle.x, particle.y, particle.direction, particle.weight);
			new_particles[i].setSenseNoise(particle.senseStd);
		}

		particles = new_particles;
	}

	/**
	 * EAP estimation:expected a posterior
	 * 
	 * @param beaconUse
	 *            0 use nearest beacon to achieve PF, 1 use all beacon measurement
	 * @return
	 */
	public Point2D getEapPosition(float[] measurement, int beaconUse)
	{
		double sumWeight = 0;
		for (Particle p : particles)
		{
			if (beaconUse == 0)
				p.likelihood2(measurement);
			else
				p.likelihood(measurement);
			sumWeight += p.weight;
		}

		//normalization
		for (Particle p : particles)
		{
			p.weight /= sumWeight;
		}

		Point2D pos = new Point2D();
		for (Particle p : particles)
		{
			pos.x += p.x * p.weight;
			pos.y += p.y * p.weight;
		}

		return pos;
	}

	private int circle(int num, int length)
	{
		while (num > length - 1)
		{
			num -= length;
		}
		while (num < 0)
		{
			num += length;
		}
		return num;
	}

	/**
	 * MAP estimation:Max a Posterior
	 * 
	 * @return
	 */
	private Particle getMapPosition()
	{
		Particle particle = particles[0];
		for (int i = 0; i < particlesNum; i++)
		{
			if (particles[i].weight > particle.weight)
			{
				particle = particles[i];
			}
		}
		return particle;
	}

	@Override
	public String toString()
	{
		String res = "";
		for (int i = 0; i < particlesNum; i++)
		{
			res += particles[i].toString() + "\n";
		}
		return res;
	}

}