package kalmanfilter;

import util.Matrix;

/**
 * Kalman could estimate more parameter like speed<br/>
 * While Least square use redundant observation to estimate less parameter<br/>
 * Model:<br/> 
 * X(n)=F*X(n-1)+ Q (P)   <br/>
 * Z   =H*X(n)  + R (W)
 * @author LeslieXong
 */
abstract class KalmanBasic
{
	protected int obsvNum,stateNum; //m为观测值Z个数  n为状态值X个数
	
	protected Matrix stateTransitModelF,obsvModelH,processNoiseCovQ,obsvNoiseCovR;
	protected Matrix prioriErrorCovP,posteriorErrorCovP;
	protected Matrix prioriState,posteriorState,processResidual;

	public KalmanBasic(int _obsvNum,int _stateNum)
	{
		this.obsvNum=_obsvNum;
		this.stateNum=_stateNum;
		
		setInitialState();
		setDefaultModel();
	}

	/**
	 * 
	 * Input new obsvNum dimension observations to filter state
	 * @param obsv：obsvNum*1 matrix
	 */
	public Matrix filter(Matrix obsv)
	{
		if(prioriState==null || prioriErrorCovP==null)
			throw new  NullPointerException("must iniatal state first");
		
		//predict
		Matrix state_ = stateTransitModelF.times(prioriState);  
		Matrix P_ = stateTransitModelF.times(prioriErrorCovP.times(stateTransitModelF.trans())).plus(processNoiseCovQ);   
		Matrix W = obsvModelH.times(P_.times(obsvModelH.trans())).plus(obsvNoiseCovR);        
		Matrix HtWinv = W.chol(obsvModelH).trans();
		
		//update
		Matrix kalmanGain=P_.times(HtWinv);                                          //Kalman gain
		processResidual = obsv.minus(obsvModelH.times(state_));                      			 
		posteriorState=(kalmanGain.times(processResidual)).plus(state_);            //update state
		posteriorErrorCovP = P_.minus((kalmanGain.times(obsvModelH)).times(P_));    //update p
		
		prioriState=posteriorState;
		prioriErrorCovP=posteriorErrorCovP;
		
		return  posteriorState;
	}
	
	/**
	 * Could initial all state as 0; <br>Priori error covariance as diagnoal matrix;<BR/>
	 */
	abstract void setInitialState();
	
	
	/**
	 * Initial state
	 * @param state initial state
	 * @param covariance could not be 0 otherwise it will not converge
	 * @param q process noise covariance
	 * @param r transit noise covariance
	 */
	public void setCurrentState(Matrix state,Matrix covariance)
	{
		prioriState=state;
		prioriErrorCovP=covariance;
	}
	
	/**
	 * set a default model's F and H.
	 */
	abstract void setDefaultModel();
	
	/**
	 *  Use for stateTransitModel Dim: stateNum*stateNum <BR/>
	 *  Big frocessNoiseCov(Compare to ObsvNoiseCov) lead fast converge but sensitive to observation<BR/>
	 *  If set liable initial state manually <BR/>
	 */
	public void setProcessNoiseCovQ(Matrix Q)
	{
		this.processNoiseCovQ=Q;
	}
	/**
	 *   Use for stateTransitModel Dim: stateNum*stateNum <BR/>
	 *  Big processNoiseCov (Compare to ObsvNoiseCov) lead fast converge but sensitive to observation<BR/>
	 *  If set liable initial state manually <BR/>
	 */
	public void setProcessNoiseCovQ(double q)
	{
		this.processNoiseCovQ=new Matrix(stateNum,q);
	}
	
	/**
	 * Use for ObsvModel Dim:obsvNum*obsvNum <br>
	 * Small obsvNoiseCov(Compare to ProcessNoiseCov) lead fast converge but sensitive to observation
	 */
	public void setObsvNoiseCovR(Matrix R)
	{
		this.obsvNoiseCovR=R;
	}
	/**
	 * Use for ObsvModel Dim:obsvNum*obsvNum <br>
	 * Small obsvNoiseCov(Compare to ProcessNoiseCov) lead fast converge but sensitive to observation
	 */
	public void setObsvNoiseCovR(double r)
	{
		this.obsvNoiseCovR=new Matrix(obsvNum,r);
	}
	
	/**
	 * Dim: stateNum*stateNum
	 */
	public void setStateTransitModelF(Matrix F)
	{
		this.stateTransitModelF=F;
	}
	
	/**
	 * Dim: obsvNum*stateNum 
	 */
	public void setObsvModelH(Matrix H)
	{
		this.obsvModelH=H;
	}
	
	
	/*******************print function*******************************/
	
	void printProcessNoiseCovQ()
	{
		processNoiseCovQ.printMatrix("processNoiseCovQ");
	}

	void printStateTransitModelF()
	{
		stateTransitModelF.printMatrix("stateTransitModelF");
	}

	void printObsvModelH()
	{
		obsvModelH.printMatrix("obsvModelH");
	}

	void printObsvNoiseCovR()
	{
		obsvNoiseCovR.printMatrix("obsvNoiseCovR");
	}

	void forwardPrint()
	{
		processResidual.printMatrix("eps");

		posteriorState.printMatrix("posteriorState");

		posteriorErrorCovP.printMatrix("posteriorErrorCovP");
	}

}
