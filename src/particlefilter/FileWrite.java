package particlefilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileWrite {
	private String filePath;
	private BufferedWriter bw;
	
	public FileWrite(String filePath)
	{
		this.filePath=filePath;
		initial();
	}
	
	private void initial()
	{
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
	}
	
	public void write(String content)
	{
		try {
			bw.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeStream()
	{
		try {
			bw.close();
			//System.out.print("file closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
