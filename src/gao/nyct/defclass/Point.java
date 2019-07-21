package gao.nyct.defclass;

import java.util.Vector;
/**
 * �������
 * @author Administrator
 *
 */
public class Point {
	public long id;
	public double x;// �����������
	public double y;

	public Point() {
	}

	public Point(double lat, double lng) {
		this.x = lat;
		this.y = lng;
	}
	
	public Point(double lat, double lng, long id){
		this.x = lat;
		this.y = lng;
		this.id = id;
	}

	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
	}

	public void print() {
		System.out.println(x + " " + y);
	}

	public Vector<Long> line_set;// ��ʾ�õ������Ļ����ϣ������ж���
}
