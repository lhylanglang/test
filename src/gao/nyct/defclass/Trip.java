package gao.nyct.defclass;
/**
 * ���³��ص㶨λ�������·�����γɵ�����
 * @author Administrator
 *
 */
public class Trip {	

	private Point o; //��ʼ·��
	private Point d; //����·��
	public long sid; //��ʼ·��id
	public long eid; //�յ�·��id
	public double et; //���Ƶ��г�ʱ��
	private String startTime; //��ʼʱ��
	private double travelTime; //�г�ʱ��
	public double length; // �ܳ���

	public Trip(){
		
	}
	// ���췽��
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
