package particlefilter;

import java.util.Collection;

public interface Particle<StateType, MeasurementType> {
	public Particle<StateType, MeasurementType> ApplyFilter(Collection<MeasurementType> y);
}
