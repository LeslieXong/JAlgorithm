package kalmanfilter;

import java.util.List;

import particlefilter.LoadData;
import util.*;

public class kfTest
{
	public static void main(String[] args)
	{
		testKalman();
	}

	public static void testKalman()
	{
		LoadData ld=new LoadData("src/kalmanfilter/test2.csv");
		List<Point2D> measurePDRList=ld.getMeasurePDRList();
		List<Point2D> measurePosList=ld.getMeasurePosList();
		List<Point2D> truePosList=ld.getTruePosList();

		int num = measurePDRList.size();
		
		KalmanFilter kf = new KalmanFilter(2, 2);
		
		double[] xy={measurePosList.get(0).x,measurePosList.get(0).y};
		Matrix zz = new Matrix(xy).trans();
		kf.setCurrentState(zz,new Matrix(2,5));

		System.out.printf("%8.3f %8.3f %8.3f %8.3f\n", xy[0],xy[1],0f,0f);
		// forward recursion
		for (int i = 1; i < num; i++)
		{
			Point2D trueP=truePosList.get(i);
			double[] xy1={measurePosList.get(i).x,measurePosList.get(i).y};
			Matrix z = new Matrix(xy1).trans();
			
			double [][] pdr={{ measurePDRList.get(i).x,0},{0, measurePDRList.get(i).y}};
			kf.setStateTransitModelF(new Matrix(pdr));
			Matrix state=kf.filter(z);
			System.out.printf("%8.3f %8.3f %8.3f %8.3f\n", state.value(0, 0),state.value(1, 0),trueP.x-state.value(0, 0),trueP.y-state.value(1, 0));
		}
	}

}
