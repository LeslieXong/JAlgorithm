package kalmanfilter;

import util.Matrix;

public class KalmanFilter extends KalmanBasic
{
	
	public KalmanFilter(int _obsvNum, int _stateNum)
	{
		super(_obsvNum, _stateNum);
	}

	@Override
	public void setStateTransitModelF(Matrix pdr)
	{
		Matrix stateTransitModelF= new Matrix(stateNum, 0);
		stateTransitModelF.setValue(0, 0, 1+(pdr.value(0, 0)/this.prioriState.value(0, 0)));
		stateTransitModelF.setValue(1, 1, 1+(pdr.value(1, 0)/this.prioriState.value(1, 0)));
	
		this.stateTransitModelF=stateTransitModelF;
	}

	@Override
	void setDefaultModel()
	{
		double [][] observe={{1,0},{0,1}};
		setObsvModelH(new Matrix(observe));
		
		double [][] transit={{1,0},{0,1}};
		super.setStateTransitModelF(new Matrix(transit));
		
		setProcessNoiseCovQ(1);
		setObsvNoiseCovR(1);
	}
	
	@Override
	void setInitialState()
	{
		Matrix prioriState=new Matrix(stateNum,1,0);
		Matrix prioriErrorCovP=new Matrix(stateNum,1);
	
		setCurrentState(prioriState, prioriErrorCovP);
	}

}
