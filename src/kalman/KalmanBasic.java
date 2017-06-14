package kalman;

/**
 * Kalman could estimate more parameter like speed<br/>
 * While Least square use redundant observation to estimate less parameter<br/>
 * Model:<br/> 
 * X(n)=F*X(n-1)+Q    (P)<br/>
 * Z=H*X(n)+R         (W)
 * @author sir-h
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
		
		initialDefaultState();
		setStateTransitModelF();
		setObsvModelH();
	}

	/**
	 * 
	 * Input new obsvNum dimension observations to update state
	 * @param obsv：obsvNum*1 matrix
	 */
	public Matrix updateState(Matrix obsv)
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
	 * Could initial all state as 0; priori error covariance to diagnoal matrix;<BR/>
	 * Call:<BR/>1.setState(Matrix state,Matrix covariance) <BR/>
	 * 2.setProcessNoiseCovQ(Matrix Q) / setProcessNoiseCovQ(double q) <BR/>
	 * 3.setObsvNoiseCovR(Matrix R) / setObsvNoiseCovR(double r)<BR/>
	 */
	abstract void initialDefaultState();
	
	
	/**
	 * Initial state
	 * @param state initial state
	 * @param covariance could not be 0 otherwise it will not converge
	 * @param q process noise covariance
	 * @param r transit noise covariance
	 */
	public void setState(Matrix state,Matrix covariance)
	{
		prioriState=state;
		prioriErrorCovP=covariance;
	}
	
	/**
	 *  Dim: stateNum*stateNum<BR/>
	 *  Big frocessNoiseCov(Compare to ObsvNoiseCov) lead fast converge but sensitive to observation<BR/>
	 *  If set liable initial state manually <BR/>
	 */
	public void setProcessNoiseCovQ(Matrix Q)
	{
		this.processNoiseCovQ=Q;
	}
	/**
	 *  Dim: stateNum*stateNum<BR/>
	 *  Big frocessNoiseCov(Compare to ObsvNoiseCov) lead fast converge but sensitive to observation<BR/>
	 *  If set liable initial state manually <BR/>
	 */
	public void setProcessNoiseCovQ(double q)
	{
		this.processNoiseCovQ=new Matrix(stateNum,q);
	}
	
	/**
	 * Dim:obsvNum*obsvNum <br>
	 * Small obsvNoiseCov(Compare to ProcessNoiseCov) lead fast converge but sensitive to observation
	 */
	public void setObsvNoiseCovR(Matrix R)
	{
		this.obsvNoiseCovR=R;
	}
	/**
	 * Dim:obsvNum*obsvNum <br>
	 * Small obsvNoiseCov(Compare to ProcessNoiseCov) lead fast converge but sensitive to observation
	 */
	public void setObsvNoiseCovR(double r)
	{
		this.obsvNoiseCovR=new Matrix(obsvNum,r);
	}
	
	
	abstract void setStateTransitModelF();
	/**
	 * Dim: stateNum*stateNum
	 */
	public void setStateTransitModelF(Matrix F)
	{
		this.stateTransitModelF=F;
	}
	
	
	abstract void setObsvModelH();
	
	/**
	 * Dim: obsvNum*stateNum 
	 */
	public void setObsvModelH(Matrix H)
	{
		this.obsvModelH=H;
	}
	
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
