package gao.nyct.defclass;
/**
 * 上下车地点定位到最近的路口所形成的数据
 * @author Administrator
 *
 */
public class Trip {	

	private Point o; //起始路口
	private Point d; //结束路口
	public long sid; //起始路口id
	public long eid; //终点路口id
	public double et; //估计的行车时间
	private String startTime; //起始时间
	private double travelTime; //行车时间
	public double length; // 总长度

	public Trip(){
		
	}
	// 构造方法
	public Trip(Point o, Point d, String startTime, double travelTime) {
		super();
		this.o = o;
		this.d = d;
		this.startTime = startTime;
		this.travelTime = travelTime;
	}

	public Trip(Point o, Point d, double travelTime) {
		super();
		this.o = o;
		this.d = d;
		this.travelTime = travelTime;
	}
	
	public Point getO() {
		return o;
	}

	public void setO(Point o) {
		this.o = o;
	}

	public Point getD() {
		return d;
	}

	public void setD(Point d) {
		this.d = d;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public double getTravelTime() {
		return travelTime;
	}

	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}
}
