package leastsquare;

/**
 * L=BX+d X的协因数阵为P，L为m个，X为n个 m>=n<BR/>
 * v=Bx-l 其中l=L-(BX0+d)<BR/>
 * 最小二乘则 x=!(BtPB)*BtPl <BR/>
 * 
 * @author LeslieXong
 */
public class LeastSquare
{
	Matrix L,B, X,X0,P,d; // 参数X
	Matrix Dx; 		//参数估计值方差
	int nL, nX; // m为观测值Z个数 n为状态值X个数
	double sigma;   //单位权方差

	LeastSquare(Matrix B,Matrix d)
	{
		this.B=B;
		this.d=d;
		this.nL = B.GetNrow();
		this.nX = B.GetNcol();
		SetX0();
	}

	/**
	 * 最小二乘
	 * @param L
	 */
	void LSQ(Matrix L,Matrix P)
	{
		this.L=L;
		this.P=P;
		Matrix l = L.minus(B.times(X0).plus(d));
		l.printMatrix("l");

		Matrix T = B.trans().times(P).times(B).chol(new Matrix(nX, 1)); // 参数估计值的协因数阵
		T.printMatrix("!Nbb");

		Matrix x = T.times(B.trans()).times(P).times(l);
		x.printMatrix("x");

		X = X0.plus(x); // 因为X0取0 ，所以X=x
		X.printMatrix("X");

		Matrix v = B.times(x).minus(l);
		v.printMatrix("v");

		L = L.plus(v);
		L.printMatrix("L");

		int r = nL - nX; // 自由度
		Matrix VtRV = v.trans().times(P).times(v);
		if (r >= 0)
		{
			sigma = VtRV.value(0, 0) / ((double) r);
		} else
		{
			sigma = 1.0;
		}

		System.out.printf("%8.5f",sigma);
		Dx = T.times(sigma);
	}

	/**
	 * m*n观测矩阵，若是变化的则利用SetH(matrix H)
	 */
	void SetB(Matrix B)
	{
		this.B = B;
	}

	/**
	 * 设置默认m*m观测误差协方差矩阵 ，若是变化的则利用SetR(matrix R)
	 */
	void SetP(Matrix P)
	{
		this.P = P;
	}

	void Setd(Matrix d)
	{
		this.d=d;
	}
	
	/**
	 * 设置系统值初始值X的默认量为0
	 */
	void SetX0()
	{
		X0 = (new Matrix(nX,1,0));
		X0.printMatrix("X0");
	}

	/**
	 * 系统值初始值X的设置
	 */
	void SetX0(Matrix x0)
	{
		this.X0=x0;
	}
	
	
	Matrix GetDxx()
	{
		return this.Dx;
	}
	
	double GetSigma()
	{
		return sigma;
	}
	
	
	void printB()
	{
		B.printMatrix("B");
	}

	void printP()
	{
		P.printMatrix("P");
	}

}
