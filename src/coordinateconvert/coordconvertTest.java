package coordinateconvert;

public class coordconvertTest
{
	public static void main(String[] args)
	{
		/*
		 * //≤‚ ‘«ÛƒÊæÿ’Û double[][] a={{1 ,2, 3, 4},{2, 5,9,10},{3,9,22,20},{4,10,20,37}}; Matrix W =new Matrix(a);
		 * W.printMatrix("W"); Matrix Winv=W.chol(new Matrix(4,1)); Winv.printMatrix("Winv");
		 */
	
		testCoordinateConver();
	}

	private static void testCoordinateConver()
	{
		double[][] a = { { 1.0, 1.0 }, { 6.0, 5.0 } };
		double[][] ac = { { 100, 100 }, { 350.0, 300 } };

		double[][] realCoor = { { 0, 18 }, { -5, 55.5 } };
		double[][] pixCoor = { { 791, 513 }, { 2461, 733 } };

		CoordConversion cv = new CoordConversion(pixCoor, realCoor);
		double[] x = cv.convertReal2Pix(5, 55);

		System.out.println(x[0]);
		System.out.println(x[1]);
	}
	
}
