package kalman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import particlefilter.LoadData;
import particlefilter.Point2D;

public class kfTest
{
	public static void main(String[] args)
	{
		testKalman();
	}

	public static void testKalman()
	{
		LoadData ld=new LoadData("src/kalman/test2.csv");
		List<Point2D> measurePDRList=ld.getMeasurePDRList();
		List<Point2D> measurePosList=ld.getMeasurePosList();
		List<Point2D> truePosList=ld.getTruePosList();

		int num = measurePDRList.size();
		
		KalmanFuse kf = new KalmanFuse(2, 2);
		
		double[] xy={measurePosList.get(0).x,measurePosList.get(0).y};
		Matrix zz = new Matrix(xy).trans();
		kf.setState(zz,new Matrix(2,5));

		System.out.printf("%8.3f %8.3f %8.3f %8.3f\n", xy[0],xy[1],0f,0f);
		// forward recursion
		for (int i = 1; i < num; i++)
		{
			Point2D trueP=truePosList.get(i);
			double[] xy1={measurePosList.get(i).x,measurePosList.get(i).y};
			Matrix z = new Matrix(xy1).trans();
			
			Matrix pdr=new Matrix(2,1,0); 
			pdr.setValue(0, 0, measurePDRList.get(i).x);
			pdr.setValue(1, 0, measurePDRList.get(i).y);
			
			kf.setStateTransitModelF(pdr);
			Matrix state=kf.updateState(z);
			System.out.printf("%8.3f %8.3f %8.3f %8.3f\n", state.value(0, 0),state.value(1, 0),trueP.x-state.value(0, 0),trueP.y-state.value(1, 0));
		}
	}

}
