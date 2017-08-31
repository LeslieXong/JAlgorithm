package coordinateconvert;
import util.*;

public class CoordConversion
{ 
	int rowNum;     //点数
	//Matrix fourPara; //参数dx dy k,afa
	Matrix para;    // 参数dx dy k*cos（afa） k*sin（afa）
	//Matrix error;   //残差矩阵（2*iRow）*1 
	//double sigma;
	
	/**
	 * 必须同为右手系，即可通过旋转达到不能翻折</br>
	 * 至少需要两个点，不能同在坐标轴上（分别不出90度旋转）
	 * @param pixCoor
	 * @param realCoor
	 */
	public CoordConversion( double[][] pixCoor, double[][] realCoor)
	{
		//pixCoor、 realCoor为rowNum*2
		if(pixCoor.length!=realCoor.length)
		{
			System.out.println("Dimensions not compatible");
			System.exit(1);
		}else
		{
			calculatePara(pixCoor,realCoor);
		}
	}	
	private void calculatePara( double[][] pixCoor, double[][] realCoor)
	{
		//TODO 数据合法性检查
        rowNum = pixCoor.length;
        //System.out.println(rowNum);
        
        double[][] aB = new double[2*rowNum][4];
        double[][] aL = new double[2*rowNum][1];
      
        //B矩阵
        for (int i = 0; i < rowNum; i++)
        {
            aB[2 * i][0] = 1;
            aB[2 * i][1] = 0;
            aB[2 * i][2] = realCoor[i][0];
            aB[2 * i][3] = realCoor[i][1];

            aB[2 * i + 1][ 0] = 0;
            aB[2 * i + 1][ 1] = 1;
            aB[2 * i + 1][ 2] = realCoor[i][1];
            aB[2 * i + 1][ 3] = -realCoor[i][0];

        }

        //l矩阵
        for (int i = 0; i < rowNum; i++)
        {
            aL[2 * i][ 0] = pixCoor[i][ 0];
            aL[2 * i + 1][0] = pixCoor[i][ 1];
        }

        Matrix B=new Matrix(aB);
        B.printMatrix("B");
        Matrix l=new Matrix(aL);
        //l.printMatrix("l:");
        Matrix T = B.trans().times(B).chol(new Matrix(4, 1)); // 参数估计值的协因数阵
		T.printMatrix("Nbb-");
		
		para = T.times(B.trans()).times(l);
		para.printMatrix("para");
	};
	
	public double[] convertReal2Pix(double x,double y)
	{
		double []pix=new double[2];
		pix[0]=para.value(0, 0)+x*para.value(2, 0)+y*para.value(3, 0);
		pix[1]=para.value(1, 0)-x*para.value(3, 0)+y*para.value(2, 0);
		return pix;
	}
}
