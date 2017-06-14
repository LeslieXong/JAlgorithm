package leastsquare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class lsqTest
{
	public static void main(String[] args)
	{
		/*
		 * //≤‚ ‘«ÛƒÊæÿ’Û double[][] a={{1 ,2, 3, 4},{2, 5,9,10},{3,9,22,20},{4,10,20,37}}; Matrix W =new Matrix(a);
		 * W.printMatrix("W"); Matrix Winv=W.chol(new Matrix(4,1)); Winv.printMatrix("Winv");
		 */
		 testLSQ();
	}
	
	public static void testLSQ()
	{
		double[][] L1 = { { 5.835 }, { 3.782 }, { 9.640 }, { 7.384 }, { 2.270 } };
		Matrix L = new Matrix(L1);

		double[][] b = { { 1, 0, 0 }, { -1, 1, 0 }, { 0, 1, 0 }, { 0, 1, -1 }, { 0, 0, 1 } };
		Matrix B = new Matrix(b);
		B.printMatrix("B");

		double[][] p = { { 2.9, 0, 0, 0, 0 }, { 0, 3.7, 0, 0, 0 }, { 0, 0, 2.5, 0, 0 },
				{ 0, 0, 0, 3.3, 0 }, { 0, 0, 0, 0, 4.0 } };
		Matrix P = new Matrix(p);
		P.printMatrix("P");

		double[][] dd = { { -237.483 }, { 0 }, { -237.483 }, { 0 }, { -237.483 } };
		Matrix d = (new Matrix(dd));
		d.printMatrix("d");

		LeastSquare lsq = new LeastSquare(B, d);
		lsq.LSQ(L, P);
	}
	
}
