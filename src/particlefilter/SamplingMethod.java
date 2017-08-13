package particlefilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SamplingMethod {
	
	public static ArrayList<Integer> Sample(List<Double> weights)
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		int N = weights.size();
		int index = (int)(Math.random() * N);
		double beta = 0.0;
		double mw = Collections.max(weights);
		for (int i = 0; i<N; i++)
		{
			beta += Math.random() * 2.0 * mw;
			while (beta > weights.get(index))
			{
				beta -= weights.get(index);
				index = (index+1) % N;				
			}
			res.add(index);
		}
		return res;
	}
}
