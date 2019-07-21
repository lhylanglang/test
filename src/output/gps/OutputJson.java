package output.gps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
/**
 * 用于将经纬度输出为json的方法类
 * @author Administrator
 *
 */
public class OutputJson implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4122450888781274530L;
	//输出文件路径
	private String filePath;
	FileOutputStream fos = null;
	OutputStreamWriter osw = null;
	private BufferedWriter bw = null;
	
	public OutputJson(String filePath){
		this.filePath = filePath;
	}
	
	public void init(){
		try {
			fos = new FileOutputStream(new File(filePath));
			osw = new OutputStreamWriter(fos); 
			bw = new BufferedWriter(osw);
			bw.write("[\r\n");
			bw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(double lon, double lat){
		try {
			bw.write("{\"longitude\": "+lon+",\"latitude\": "+lat+"},\r\n");
			System.out.println("{\"longitude\": "+lon+",\"latitude\": "+lat+"}");
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			bw.write("]\r\n");
			bw.flush();
			if(bw!=null)
				bw.close();
			if(osw!=null)
				osw.close();
			if(fos!=null)
				fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("输出连接关闭！");
	}
}
