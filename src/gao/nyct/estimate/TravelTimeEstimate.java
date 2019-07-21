package gao.nyct.estimate;

import gao.nyct.defclass.Trip;

import java.util.List;

/**
 * 估计行车时间的主类
 * @author Administrator
 *
 */
public class TravelTimeEstimate {
	static List<Trip>[] myTrip = null; // 按时间分片并过滤后的Trip数据
	public static void main(String[] args) {
    	
		Algorithm algorithm = new Algorithm();
		String tripi = "trip11";
		algorithm.preprocess(tripi);
		// 迭代开始时间
		System.out.println("=========================================");
		long startTime = System.currentTimeMillis();
		System.out.println("迭代开始时间："+startTime);
		System.out.println("=========================================");
		
		algorithm.compute(tripi);
		
		// 迭代耗时
		System.out.println("=========================================");
		long endTime = System.currentTimeMillis();
		System.out.println("迭代耗时："+(endTime-startTime)/1000.0+"s");
		System.out.println("=========================================");
		
		algorithm.remain(11);
		
		// 总耗时
		System.out.println("=========================================");
		long theEndTime = System.currentTimeMillis();
		System.out.println("总耗时："+(theEndTime-startTime)/1000.0+"s");
		System.out.println("=========================================");
	}

}
