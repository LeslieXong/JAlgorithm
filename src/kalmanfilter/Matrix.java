package kalmanfilter;

class Matrix
{
	// number of rows and columns
	private int nr, nc;
	// a double array to store the data
	private double[][] X;

	// overloaded constructor:double[] to a matrix[1][n]
	Matrix(double[] a)
	{
		nr = 1;
		nc = a.length;
		X = new double[nr][nc];
		for (int i = 0; i < nc; i++)
			X[0][i] = a[i];
	}

	// constructor: double array to a matrix
	Matrix(double[][] A)
	{
		nr = A.length;
		nc = A[0].length;
		X = new double[nr][nc];
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				X[i][j] = A[i][j];
		}
	}

	// copy constructor: matrix to a matrix
	Matrix(Matrix A)
	{
		nr = A.GetNrow();
		nc = A.GetNcol();
		X = new double[nr][nc];
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				X[i][j] = A.value(i, j);
		}
	}

	/**
	 * overloaded constructor: diagonal matrix
	 */
	Matrix(int Nr, double a)
	{
		nr = Nr;
		nc = Nr;
		X = new double[nr][nc];
		for (int i = 0; i < nr; i++)
			for (int j = 0; j < nr; j++)
			{
				if (i == j)
					X[i][j] = a;
				else
					X[i][j] = 0;
			}
	}

	/**
	 * overloaded constructor: constant matrix
	 * @param Nr
	 * @param Nc
	 * @param a
	 */
	Matrix(int Nr, int Nc, double a)
	{
		nr = Nr;
		nc = Nc;
		X = new double[nr][nc];
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				X[i][j] = a;
		}
	}

	// overloaded constructor:scalar to a matrix
	Matrix(double a)
	{
		nr = 1;
		nc = 1;
		X = new double[1][1];
		X[0][0] = a;
	}

	//
	//
	// basic matrix operations
	//
	//

	// matrix addition
	Matrix plus(Matrix Y)
	{
		if (nr != Y.GetNrow())
		{
			System.out.println("Dimensions not compatible");
			System.exit(1);
		}
		if (nc != Y.GetNcol())
		{
			System.out.println("Dimensions not compatible");
			System.exit(1);
		}
		Matrix XplusY = new Matrix(nr, nc, 0);
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				XplusY.setValue(i, j, X[i][j] + Y.value(i, j));
		}
		return XplusY;
	}

	// matrix subtraction
	Matrix minus(Matrix Y)
	{
		if (nr != Y.GetNrow())
		{
			System.out.println("Dimensions not compatible");
			System.exit(1);
		}
		if (nc != Y.GetNcol())
		{
			System.out.println("Dimensions not compatible");
			System.exit(1);
		}
		Matrix XminusY = new Matrix(nr, nc, 0);
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				XminusY.setValue(i, j, X[i][j] - Y.value(i, j));
		}
		return XminusY;
	}

	// matrix multiplication
	Matrix times(Matrix Y)
	{
		Matrix XtimesY;
		if ((nr == 1) && (nc == 1))
		{
			XtimesY = Y.times(X[0][0]);
		} else
		{
			if (nc != Y.GetNrow())
			{
				System.out.println("Dimensions not compatible");
				System.exit(1);
			}
			double temp;
			XtimesY = new Matrix(nr, Y.GetNcol(), 0);
			for (int i = 0; i < nr; i++)
			{
				for (int j = 0; j < Y.GetNcol(); j++)
				{
					temp = 0;
					for (int k = 0; k < nc; k++)
					{
						temp += X[i][k] * Y.value(k, j);
					}
					XtimesY.setValue(i, j, temp);
				}
			}
		}
		return XtimesY;
	}

	// scalar multiplication
	Matrix times(double Y)
	{
		Matrix XtimesY = new Matrix(nr, nc, 0);
		for (int i = 0; i < nr; i++)
			for (int j = 0; j < nc; j++)
				XtimesY.setValue(i, j, X[i][j] * Y);
		return XtimesY;
	}

	// transposition
	Matrix trans()
	{
		Matrix B = new Matrix(nc, nr, 0);
		for (int i = 0; i < nc; i++)
			for (int j = 0; j < nr; j++)
				B.setValue(i, j, X[j][i]);
		return B;
	}

	//
	// access to a member elements
	//

	double value(int r, int c)
	{
		return X[r][c];
	}

	int GetNrow()
	{
		return nr;
	}

	int GetNcol()
	{
		return nc;
	}

	void setValue(int i, int j, double a)
	{
		X[i][j] = a;
	}

	//
	// utilities
	//

	void printMatrix(String matrixName)
	{
		System.out.println(matrixName+":");
		for (int i = 0; i < nr; i++)
		{
			for (int j = 0; j < nc; j++)
				System.out.printf("%8.5f ",X[i][j]);
			System.out.println(" ");
		}
		System.out.println(" " );
	}

	//
	// Cholesky decomposition 乔里斯基分解
	//

	Matrix backward(Matrix RHS)
	{
		int nrhs = RHS.GetNcol();
		double temp;
		Matrix B = new Matrix(nr, nrhs, 0);
		if (X[nr - 1][nr - 1] != 0)
		{
			for (int j = 0; j < nrhs; j++)
				B.setValue(nr - 1, j, RHS.value(nr - 1, j) / X[nr - 1][nr - 1]);
		} else
		{
			System.out.println("Singular system!");
			System.exit(1);
		}
		for (int i = nr - 2; i >= 0; i--)
			if (X[i][i] != 0)
			{
				for (int j = 0; j < nrhs; j++)
				{
					temp = RHS.value(i, j);
					for (int k = i + 1; k < nr; k++)
						temp -= X[i][k] * B.value(k, j);
					B.setValue(i, j, temp / X[i][i]);
				}
			} else
			{
				System.out.println("Singular system!");
				System.exit(1);
			}
		return B;
	}

	Matrix forward(Matrix RHS)
	{
		int nrhs = RHS.GetNcol();
		double temp;
		Matrix B = new Matrix(nr, nrhs, 0);
		if (X[0][0] != 0)
		{
			for (int j = 0; j < nrhs; j++)
				B.setValue(0, j, RHS.value(0, j) / X[0][0]);
		} else
		{
			System.out.println("Singular system!");
			System.exit(1);
		}
		for (int i = 1; i < nr; i++)
			if (X[i][i] != 0)
			{
				for (int j = 0; j < nrhs; j++)
				{
					temp = RHS.value(i, j);
					for (int k = 0; k < i; k++)
						temp -= X[i][k] * B.value(k, j);
					B.setValue(i, j, temp / X[i][i]);
				}
			} else
			{
				System.out.println("Singular system!");
				System.exit(1);
			}
		return B;
	}

	/**
	 * Cholesky decomposition 求逆就是W.chol(E)E为单位阵
	 * W.chol(H)=Winv*H    必须是正定阵（对称） <BR/>
	 * 因为(HtWinv)t=(WinvH)t 则HtWinv=W.chol(H).trans()
	 * @return
	 */
	Matrix chol(Matrix RHS)
	{
		Matrix B;
		if (nr == 1)
		{
			B = RHS.times(1. / X[0][0]);
		} else
		{
			double temp = 0;
			if (nr != nc)
			{
				System.out.println("Not a square matrix!");
				System.exit(1);
			}
			//G是分解出来的下三角阵
			Matrix G = new Matrix(nr, 0);// square matrix of all zero
			// lots of wasted storage here!

			for (int j = 0; j < nc; j++)
			{// proceed by columns
				if (X[j][j] == 0)
				{
					System.out.println("Singular matrix!");
					System.exit(1);
				}
				temp = X[j][j];// Compute l_{jj} first
				for (int k = 0; k < j; k++)
					temp -= G.value(j, k) * G.value(j, k);
				G.setValue(j, j, Math.sqrt(temp));

				for (int i = j + 1; i < nr; i++)
				{// now do the rest of the column
					temp = X[i][j];
					for (int k = 0; k < j; k++)
						temp -= G.value(j, k) * G.value(i, k);
					G.setValue(i, j, temp / G.value(j, j));
				}
			}		
			//now forward solve Lz=rhs
			Matrix h = G.forward(RHS);
			// now back solve the system
			B = G.trans().backward(h);
		}
		return B;
	}
}
