package originkf;

//This class provides specific examples of the abstract
//methods in class Kalman that arise from smoothing
//spline considerations.
//The x process corresponds to m-fold integrated Brownian 
//motion.
import java.math.*;


class ss extends ssKalman
{
	// members
	vector tau;  // vector of "time" ordinates
	int m;       // order of derivative in penalty(导数阶)
	double lam;  // smoothing parameter

	// constructor
	ss(int T, vector Tau, int M, double Lambda)
	{
		t = T;
		tau = new vector(Tau);
		m = M;
		lam = Lambda;
	}

	matrix Q(int k)
	{
		double den = 0, num = 0;
		double delta = 0;

		if (k == 0)
			delta = tau.value(0);
		if (k != 0)
			delta = tau.value(k) - tau.value(k - 1);

		matrix A = new matrix(m, 0);
		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
			{
				den = fac(m - 1 - i) * fac(m - 1 - j);
				den *= (double) (2 * m - 1 - i - j);
				num = Math.pow(delta, 2 * m - 1 - i - j) / lam;
				A.setValue(i, j, num / den);
			}

		return A;
	}

	matrix F(int k)
	{
		matrix A;
		double delta = 0;
		if (k == tau.GetNrow())
		{
			A = new matrix(m, 0);
		}

		else
		{
			if (k == 0)
				delta = tau.value(0);

			if (k != 0)
				delta = tau.value(k) - tau.value(k - 1);

			A = new matrix(m, 1);// m by m identity
			if (m != 1)
			{
				for (int i = 0; i < (m - 1); i++)
					for (int j = i + 1; j < m; j++)
					{
						A.setValue(i, j, Math.pow(delta, j - i) / fac(j - i));
					}
			}
		}
		return A;
	}

	matrix H(int k)
	{
		matrix A = new matrix(1, m, 0);
		A.setValue(0, 0, 1);
		return A;
	}

	matrix R(int k)
	{
		matrix A = new matrix(1, 1, 1);
		return A;
	}

	/**
	 * 阶乘函数
	 */
	static double fac(int k)
	{
		double f = 1;
		if (k == 0)
			f = 1;
		if (k != 0)
		{
			for (int i = 1; i <= k; i++)
				f += f * (double) i;   //原无+号
		}
		return f;
	}

	vector Gettau()
	{
		return tau;
	}

	int Getm()
	{
		return m;
	}

}
