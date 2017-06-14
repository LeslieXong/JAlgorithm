package particlefilter;

import java.util.ArrayList;
import java.util.List;

public class Bootstrap<P,M> {
	public static<P,M> List<Particle<P,M>> RunBootstrapStep(List<Particle<P,M>> priors, List<M> y)
	{
		List<Particle<P,M>> result = new ArrayList<Particle<P,M>>();
		for (int i = 0; i<priors.size(); i++)
		{
			result.add(priors.get(i).ApplyFilter(y));
		}
		return result;
	}	
}
