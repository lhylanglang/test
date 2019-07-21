package gao.nyct.defclass;

import java.util.Vector;
/**
 * 定义点类
 * @author Administrator
 *
 */
public class Point {
	public long id;
	public double x;// 点的两个坐标
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

	public Vector<Long> line_set;// 表示该点所属的弧集合，可能有多条
}
