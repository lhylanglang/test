package gao.nyct.estimate;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class Test1 {

	private static double Rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double Distance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = Rad(lat1);
		double radLat2 = Rad(lat2);
		double a = radLat1 - radLat2;
		double b = Rad(lng1) - Rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378137.0;
		s = Math.round(s * 10000) / 10000;
		return s;
	}
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Mongo connection = null;
		DB db = null;
		DBCollection dbcoll = null;
		DBCursor dbcursor = null;
		try {
			connection = new MongoClient( "127.0.0.1" , 27017 );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = connection.getDB("test");
		String collName = "persons";
		dbcoll = db.getCollection(collName);
		if(db.collectionExists(collName));
			db.getCollection(collName).drop();
    	connection.close();
    	
	}

}
