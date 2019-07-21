package gao.nyct.defclass;
/**
 * �����߶���
 * @author Administrator
 *
 */
public class Line{
	public Point[] p;//������
    public double length;//���ĳ���
    public long sid;//���ı��
    public long wayid; //��ͼ�ϵĵ�·id
    public double travelTime; //�ڸõ�·���г�ʱ��
    
    public Line(){
    	this.p=new Point[2];
    }
    
    public Line(Line other){
    	this.p=new Point[2];
    	this.p[0]=new Point(other.p[0]);
    	this.p[1]=new Point(other.p[1]);
    }
    
    public Line(Point p1,Point p2, long sid, long wayid, double len){
        this.p=new Point[2];
        this.p[0]=p1;
        this.p[1]=p2;
        this.sid=sid;
        this.wayid = wayid;
        this.length=len;
    }
    
    public Line(Point p1,Point p2, long sid, double len){
        this.p=new Point[2];
        this.p[0]=p1;
        this.p[1]=p2;
        this.sid=sid;
        this.length=len;
    }
    
    public void print(){
    	System.out.println(sid+" "+p[0].id+" "+p[1].id+" "+length);
    }
}
