package gao.nyct.estimate;

import gao.nyct.defclass.Line;
import gao.nyct.defclass.Point;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class FindStreet {
	public double vinit = 15; // 道路初始速度，单位m/s
	private HashMap<Long,Line> lineSet = null; // 地图中连接所有路口的道路集合
	private HashMap<Long,Point> pointSet = null; // 地图中所有路口点的集合
	// 获取连接路口的道路集合
	@SuppressWarnings("unchecked")
	public HashMap<Long,Line> getStreet(){
		pointSet=new HashMap<Long,Point>(); // 初始化路口点的集合
    	lineSet=new HashMap<Long,Line>();  //初始化连接路口道路集合
		Mongo connection = null;
		DB db = null;
		DBCollection dbcoll = null;
		DBCursor dbcsor = null;
		try {
			connection = new MongoClient( "127.0.0.1" , 27017 );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = connection.getDB("MapLocNew");
		dbcoll = db.getCollection("mapIntersection");
		dbcsor = dbcoll.find();
		// 遍历每一个路口
		while(dbcsor.hasNext()){
			DBObject dbObject = dbcsor.next();
			long id = (long) dbObject.get("_id");
			Map<String, Double> pgis = (Map<String, Double>) dbObject
					.get("gis");
			double lat = pgis.get("lat");
			double lon = pgis.get("lon");
			Point point = new Point(lat, lon, id);
			pointSet.put(id, point);
			List<Long> list = (List<Long>) dbObject.get("edge"); // 获取道路标号列表
			DBCursor cursor = null;
			DBCollection coll = db.getCollection("mapArc");
			DBCursor pcursor = null;
			for(long sid:list){
				if(lineSet.containsKey(sid)) // 如果该道路已经添加过
					continue;
				cursor = coll.find(new BasicDBObject("_id", sid));
				if(cursor.hasNext()){ // 如果找到对应的道路
					DBObject sObject = cursor.next();
					double length = (double) sObject.get("length");
					long wayid = (long) sObject.get("wayid");
					Map<String, Long> gis = (Map<String, Long>) sObject
							.get("gis");
					long x = gis.get("x");
					long y = gis.get("y");
					Point p1 = new Point();
					Point p2 = new Point();
					DBObject pObject = null;
					//查找第一个顶点
					pcursor = dbcoll.find(new BasicDBObject("_id", x));
					if(!pcursor.hasNext()) //如果没找到
						continue;
					pObject = pcursor.next();
					p1.id = x;
					Map<String, Double> gis1 = (Map<String, Double>) pObject
							.get("gis");
					p1.x = gis1.get("lat");
					p1.y = gis1.get("lon");
					//查找第二个顶点
					pcursor = dbcoll.find(new BasicDBObject("_id", y));
					if(!pcursor.hasNext()) //如果没找到
						continue;
					pObject = pcursor.next();
					p2.id = y;
					Map<String, Double> gis2 = (Map<String, Double>) pObject
							.get("gis");
					p2.x = gis2.get("lat");
					p2.y = gis2.get("lon");
					Line line = new Line(p1, p2, sid, wayid, length);
					// 初始化道路行驶时间
					line.travelTime = length/vinit;
					lineSet.put(sid, line);
				}
			}			
		}
		connection.close();
		return lineSet;
	}
	
	// 获取地图点集
	public HashMap<Long,Point> getPointSet(){
		if(pointSet==null)
			getStreet();
		return pointSet;
	}
	
//	public static void main(String[] args){
//		FindStreet findStreet = new FindStreet();
//		List<Line> street = findStreet.findStreet();
//		for(Line line:street){
//			line.print();
//		}
//	}
}
