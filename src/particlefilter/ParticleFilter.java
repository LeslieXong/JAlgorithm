package particlefilter;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.Point2D;

public class ParticleFilter {

    public Particle[] particles;
    int particlesNum = 0;
    Random random = new Random();

    public ParticleFilter(int numParticles, Point2D[] landmarks, int width, int height) {
        this.particlesNum = numParticles;

        particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++) {
            particles[i] = new Particle(landmarks, width, height);
        }
    }

    public void setNoise(float fNoise, float oNoise, float sNoise) {
        for (int i = 0; i < particlesNum; i++) {
            particles[i].setNoise(fNoise, oNoise, sNoise);
        }
    }

    /**
     * Moves the particle's position(propagate)
     * TODO INS error should consider system error other than random error
     * @param orient value, in radians
     * @param forward move value, must be >= 0
     */
    public void move(float orient, float forward) throws Exception {
        for (int i = 0; i < particlesNum; i++) {
            particles[i].move(orient, forward);
        }
    }

    public void reSample(float[] measurement) throws Exception {
        Particle[] new_particles = new Particle[particlesNum];

        for (int i = 0; i < particlesNum; i++) {
            particles[i].likelihood(measurement);
        }
        
        float beta = 0f;
        Particle best = getMapPosition();
        int index = (int) random.nextFloat() * particlesNum;
        for (int i = 0; i < particlesNum; i++) {
            beta += random.nextFloat() * 2f * best.probability;
            while (beta > particles[index].probability) {
                beta -= particles[index].probability;
                index = circle(index + 1, particlesNum);
            }
            new_particles[i] = new Particle(particles[index].landmarks, particles[index].worldWidth, particles[index].worldHeight);
            new_particles[i].set(particles[index].x, particles[index].y, particles[index].orientation, particles[index].probability);
            new_particles[i].setNoise(particles[index].forwardNoise, particles[index].orientationNoise, particles[index].senseNoise);
        }

        particles = new_particles;        
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
    public Particle getMapPosition() {
        Particle particle = particles[0];
        for (int i = 0; i < particlesNum; i++) {
            if (particles[i].probability > particle.probability) {
                particle = particles[i];
            }
        }
        return particle;
    }
    
    /**
     * EAP estimation:expected a posterior
     * @return
     */
    public Particle getEapPosition() {
        Particle p = new Particle(particles[0].landmarks, particles[0].worldWidth, particles[0].worldHeight);
        float x = 0, y = 0, orient = 0, prob = 0;
        for(int i=0;i<particlesNum;i++) {
            x += particles[i].x;
            y += particles[i].y;
            orient += particles[i].orientation;
            prob += particles[i].probability;
        }
        x /= particlesNum;
        y /= particlesNum;
        orient /= particlesNum;
        prob /= particlesNum;
        try {
            p.set(x, y, orient, prob);
        } catch (Exception ex) {
            Logger.getLogger(ParticleFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        p.setNoise(particles[0].forwardNoise, particles[0].orientationNoise, particles[0].senseNoise);
        
        return p;
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