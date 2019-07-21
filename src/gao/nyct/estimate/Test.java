package gao.nyct.estimate;

import gao.nyct.defclass.Line;
import gao.nyct.defclass.MapLoc;
import gao.nyct.defclass.Point;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class Test {
	public static void main(String[] args){
		
		HashMap<Long,Point> PointSet;
	    HashMap<Long,Line> LineSet;
	    PointSet=new HashMap<Long,Point>();
    	LineSet=new HashMap<Long,Line>();
	    for(long i=1;i<=8;i++){
	    	Point p = new Point();
	    	p.id = i;
	    	PointSet.put(i, p);
	    }
	    
	    LineSet.put((long) 12, new Line(PointSet.get((long)1), PointSet.get((long)2), 12, 12, 1));
	    LineSet.put((long) 13, new Line(PointSet.get((long)1), PointSet.get((long)3), 13, 13, 1));
	    LineSet.put((long) 14, new Line(PointSet.get((long)1), PointSet.get((long)4), 14, 14, 4));
	    LineSet.put((long) 16, new Line(PointSet.get((long)1), PointSet.get((long)6), 16, 16, 2));
	    LineSet.put((long) 17, new Line(PointSet.get((long)1), PointSet.get((long)7), 17, 17, 5));
	    LineSet.put((long) 26, new Line(PointSet.get((long)2), PointSet.get((long)6), 26, 26, 2));
	    LineSet.put((long) 28, new Line(PointSet.get((long)2), PointSet.get((long)8), 28, 28, 4));
	    LineSet.put((long) 37, new Line(PointSet.get((long)3), PointSet.get((long)7), 37, 37, 3));
	    LineSet.put((long) 45, new Line(PointSet.get((long)4), PointSet.get((long)5), 45, 45, 1));
	    LineSet.put((long) 56, new Line(PointSet.get((long)5), PointSet.get((long)6), 56, 56, 1));
	    LineSet.put((long) 78, new Line(PointSet.get((long)7), PointSet.get((long)8), 78, 78, 1));
	    MapLoc mymap = new MapLoc(PointSet, LineSet);
	    Dijkstra dij = new Dijkstra(mymap);

//	    if(dij.isConnected(mymap, 1, 8))
//	    	System.out.println("1到8图连通");
//	    else
//	    	System.out.println("1到8图不连通");
    	dij.solve(1, 1);
    	dij.printPathInfo(1);
	    
//	    dij.printPathInfo(42436126);
	    
	    
//	    Mongo connection = null;
//		DB db = null;
//		DBCollection dbcoll = null;
//		DBCursor dbcursor = null;
//		try {
//			connection = new MongoClient( "127.0.0.1" , 27017 );
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		db = connection.getDB("MapLoc1");
//		String collName = "shortestPath";
//		dbcoll = db.getCollection(collName);
//		DBObject query = new BasicDBObject();
//		// 删除指定的条目
//		Pattern pattern = Pattern.compile(42427374+"-.*$");
//		query.put("_id", new BasicDBObject("$regex", pattern));
//		dbcursor = dbcoll.find(query);
//		while(dbcursor.hasNext()){
//			DBObject dbObject = dbcursor.next();
//	    	dbcoll.remove(dbObject);
//		}    	
//    	connection.close();
	}

}
