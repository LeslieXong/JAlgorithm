package particlefilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import util.*;

/**
 * Load data from csv format
 * @author LeslieXong
 *
 */
public class LoadData {
	private String filePath;
	private BufferedReader br;
	
	private List<Point2D> measurePDRList;
	private List<Point2D> measurePosList;
	private List<Point2D> truePosList;
	
	public LoadData(String filePath)
	{
		this.filePath=filePath;
		measurePDRList=new LinkedList<>();
		measurePosList=new LinkedList<>();
		truePosList=new LinkedList<>();
		initial();
	}
	
	private void initial()
	{
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.print("data file not existed\n");
			//throw new RuntimeException();
		}
		FileReader fr = null;
		try {
			fr = new FileReader(file.getAbsoluteFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br = new BufferedReader(fr);
		read();
	}
	
	public List<Point2D> getMeasurePDRList() {
		return measurePDRList;
	}
	
	public List<Point2D> getTruePosList() {
		return truePosList;
	}
	
	public List<Point2D> getMeasurePosList() {
		return measurePosList;
	}
	
	public void read()
	{
		try { 
            br.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null; 
            while((line=br.readLine())!=null){ 
                String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                
                double x=Double.parseDouble(item[0]);
                double y=Double.parseDouble(item[1]);
                measurePDRList.add(new Point2D(x,y));
               
                x=Double.parseDouble(item[3]);
                y=Double.parseDouble(item[4]);
                measurePosList.add(new Point2D(x,y));
                
                x=Double.parseDouble(item[6]);
                y=Double.parseDouble(item[7]);
                truePosList.add(new Point2D(x,y));
                
               // System.out.println(truePosList.toString()); 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
	}
	
	public void closeStream()
	{
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
