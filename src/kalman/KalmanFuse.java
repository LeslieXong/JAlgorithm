package kalman;


public class KalmanFuse extends KalmanBasic
{
	
	public KalmanFuse(int _obsvNum, int _stateNum)
	{
		super(_obsvNum, _stateNum);
	}

	void setStateTransitModelF()
	{
		Matrix stateTransitModelF= new Matrix(stateNum, 0);
		stateTransitModelF.setValue(0, 0, 1);
		stateTransitModelF.setValue(1, 1, 1);
		super.setStateTransitModelF(stateTransitModelF);
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
	void setObsvModelH()
	{
		Matrix obsvModelH = new Matrix(obsvNum, stateNum,0);
		obsvModelH.setValue(0, 0, 1);
		obsvModelH.setValue(1, 1, 1);
		setObsvModelH(obsvModelH);
	}

	@Override
	void initialDefaultState()
	{
		Matrix prioriState=new Matrix(stateNum,1,0);
		Matrix prioriErrorCovP=new Matrix(stateNum,1);
	
		setState(prioriState, prioriErrorCovP);
		setProcessNoiseCovQ(0.1);
		setObsvNoiseCovR(3);
	}

}
