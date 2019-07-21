package gao.nyct.estimate;

import gao.nyct.defclass.Trip;

import java.util.List;

/**
 * �����г�ʱ�������
 * @author Administrator
 *
 */
public class TravelTimeEstimate {
	static List<Trip>[] myTrip = null; // ��ʱ���Ƭ�����˺��Trip����
	public static void main(String[] args) {
    	
		Algorithm algorithm = new Algorithm();
		String tripi = "trip11";
		algorithm.preprocess(tripi);
		// ������ʼʱ��
		System.out.println("=========================================");
		long startTime = System.currentTimeMillis();
		System.out.println("������ʼʱ�䣺"+startTime);
		System.out.println("=========================================");
		
		algorithm.compute(tripi);
		
		// ������ʱ
		System.out.println("=========================================");
		long endTime = System.currentTimeMillis();
		System.out.println("������ʱ��"+(endTime-startTime)/1000.0+"s");
		System.out.println("=========================================");
		
		algorithm.remain(11);
		
		// �ܺ�ʱ
		System.out.println("=========================================");
		long theEndTime = System.currentTimeMillis();
		System.out.println("�ܺ�ʱ��"+(theEndTime-startTime)/1000.0+"s");
		System.out.println("=========================================");
	}

}
