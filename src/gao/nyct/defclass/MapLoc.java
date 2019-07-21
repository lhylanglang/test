package gao.nyct.defclass;

import java.util.HashMap;
/**
 * 定义地图类
 * @author Administrator
 *
 */
public class MapLoc {
	
    public HashMap<Long,Point> PointSet;
    public HashMap<Long,Line> LineSet;
    public int PointNum,LineNum; 
	public MapLoc(){}
	
    public MapLoc(int pnum,int lnum){
    	PointNum=pnum;
    	LineNum=lnum;
    	PointSet=new HashMap<Long,Point>();
    	LineSet=new HashMap<Long,Line>();
    }
    
    public MapLoc(HashMap<Long, Point> pointSet, HashMap<Long, Line> lineSet){
    	this.PointSet = pointSet;
    	this.LineSet = lineSet;
    	this.PointNum = pointSet.size();
    	this.LineNum = lineSet.size();
    }
    
    public void addPoint(long id,double lat,double lng){
    	PointSet.put(id, new Point(lat,lng));
    }
    
    public void addLine(long id,long xid,long yid,double len,long stid) {
    	LineSet.put(id,new Line(PointSet.get(xid),PointSet.get(yid),id,stid,len));
    }
    
    public void print(){
    	System.out.println(PointNum);
    	
    	for(Long id:PointSet.keySet()){
    		System.out.print(id+" "); 
    		PointSet.get(id).print();
    	}
    	
    	System.out.println(LineNum);
    	for(Long id:LineSet.keySet()){
    		LineSet.get(id).print();
    	}
    }

}
