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
	public double vinit = 15; // ��·��ʼ�ٶȣ���λm/s
	private HashMap<Long,Line> lineSet = null; // ��ͼ����������·�ڵĵ�·����
	private HashMap<Long,Point> pointSet = null; // ��ͼ������·�ڵ�ļ���
	// ��ȡ����·�ڵĵ�·����
	@SuppressWarnings("unchecked")
	public HashMap<Long,Line> getStreet(){
		pointSet=new HashMap<Long,Point>(); // ��ʼ��·�ڵ�ļ���
    	lineSet=new HashMap<Long,Line>();  //��ʼ������·�ڵ�·����
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
		// ����ÿһ��·��
		while(dbcsor.hasNext()){
			DBObject dbObject = dbcsor.next();
			long id = (long) dbObject.get("_id");
			Map<String, Double> pgis = (Map<String, Double>) dbObject
					.get("gis");
			double lat = pgis.get("lat");
			double lon = pgis.get("lon");
			Point point = new Point(lat, lon, id);
			pointSet.put(id, point);
			List<Long> list = (List<Long>) dbObject.get("edge"); // ��ȡ��·����б�
			DBCursor cursor = null;
			DBCollection coll = db.getCollection("mapArc");
			DBCursor pcursor = null;
			for(long sid:list){
				if(lineSet.containsKey(sid)) // ����õ�·�Ѿ���ӹ�
					continue;
				cursor = coll.find(new BasicDBObject("_id", sid));
				if(cursor.hasNext()){ // ����ҵ���Ӧ�ĵ�·
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
					//���ҵ�һ������
					pcursor = dbcoll.find(new BasicDBObject("_id", x));
					if(!pcursor.hasNext()) //���û�ҵ�
						continue;
					pObject = pcursor.next();
					p1.id = x;
					Map<String, Double> gis1 = (Map<String, Double>) pObject
							.get("gis");
					p1.x = gis1.get("lat");
					p1.y = gis1.get("lon");
					//���ҵڶ�������
					pcursor = dbcoll.find(new BasicDBObject("_id", y));
					if(!pcursor.hasNext()) //���û�ҵ�
						continue;
					pObject = pcursor.next();
					p2.id = y;
					Map<String, Double> gis2 = (Map<String, Double>) pObject
							.get("gis");
					p2.x = gis2.get("lat");
					p2.y = gis2.get("lon");
					Line line = new Line(p1, p2, sid, wayid, length);
					// ��ʼ����·��ʻʱ��
					line.travelTime = length/vinit;
					lineSet.put(sid, line);
				}
			}			
		}
		connection.close();
		return lineSet;
	}
	
	// ��ȡ��ͼ�㼯
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
