package gao.nyct.defclass;
/**
 * 定义线段类
 * @author Administrator
 *
 */
public class Line{
	public Point[] p;//两定点
    public double length;//弧的长度
    public long sid;//弧的标号
    public long wayid; //地图上的道路id
    public double travelTime; //在该道路的行车时间
    
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
