package particlefilter;
import java.util.ArrayList;
import java.util.List;

import particlefilter.Boat;

public class pfTest {

	public static void TrackBoat() {
		LoadData ld=new LoadData("src/particlefilter/test2.csv");
		List<Point2D> measurePDRList=ld.getMeasurePDRList();
		List<Point2D> measurePosList=ld.getMeasurePosList();
		List<Point2D> truePosList=ld.getTruePosList();
			
		int N = 500;  //particle number
		BoatParticle[] particles = new BoatParticle[N];
		
		FileWrite fw = new FileWrite("src/particlefilter/pf-out.txt");	
		
		BoatState s;
		// create initial particles
		for (int i = 0; i < N; i++) {
			s= Boat.NoisBoatState(measurePosList.get(0).x, measurePosList.get(0).y, 0, 0, 0);
			particles[i] = new BoatParticle(s, 1);
			//System.out.println("particle state(x,y,heading):" + s.x + "," + s.y + "," + s.heading);
		}

		BoatParticle[] posterior_particles = new BoatParticle[N];
		BoatParticle[] posterior_particles1 = new BoatParticle[N];

		int length = measurePDRList.size();
		int current_ind=0;
		while (current_ind <length) {
			Point2D measurePos=measurePosList.get(current_ind);
			Point2D measurePDR=measurePDRList.get(current_ind);
			Point2D truePos=truePosList.get(current_ind);
			
			for (int i = 0; i < N; i++) {
				posterior_particles1[i] = (BoatParticle) particles[i].ApplyFilter(measurePos,measurePDR);
			}
			
			// reSample // get weights
			ArrayList<Double> w = new ArrayList<Double>(N);
			for (int j = 0; j < N; j++) {
				w.add(posterior_particles1[j].weight);
			}
			
			ArrayList<Integer> rr = SamplingWheel.Sample(w);
			for (int j = 0; j < N; j++) {
				posterior_particles[j] = posterior_particles1[rr.get(j)];
			}
			//TODO normalisation weight then use weight to get position 
			//TODO estimate before reSample?
			
			double meanx = 0;
			double meany = 0;
			
			for (int i = 0; i < N; i++) {
				meanx += posterior_particles[i].state.x;
				meany += posterior_particles[i].state.y;
			}
			
			double estX=meanx/N;
			double estY=meany/N;
			double deltax=truePos.x-estX;
			double deltay=truePos.y-estY;
			fw.write(current_ind + "," + estX + "," + estY +","+deltax+","+deltay+ "\n");
			System.out.printf("%8.3f %8.3f %8.3f %8.3f\n",estX,estY,deltax,deltay);
			particles = posterior_particles;
			current_ind++;
		}
		fw.closeStream();
	}

	public static void main(String[] args) {
		pfTest.TrackBoat();
	}

}
