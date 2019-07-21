package gao.nyct.estimate;

import gao.nyct.defclass.Point;
import gao.nyct.defclass.Trip;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

/**
 * ��ʱ����ҳ�����Ҫ���Trip
 * @author Administrator
 *
 */
public class FindTrip {
	public static final double ITHRESHOLD = 0.1; // �����³��ص㶨λ�����Χ�ڵ�·��,��λkm
	public static final double PI = 3.141592653589793238462643383279502884;
	public static final int TIME_SLICE = 24; //��һ���Ϊ��ʱ����
//	static List<Trip>[] allTrip = null; // ��ʱ�ηֺõ�	
	
	public double Rad(double d) {
	    return d * PI / 180.0;
	}
	
	// ���������ľ���
	public double distance(double lat1, double lng1, double lat2,double lng2) {
		double radLat1 = Rad(lat1);
		double radLat2 = Rad(lat2);
		double a = radLat1 - radLat2;
		double b = Rad(lng1) - Rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378137.0;
		//s = Math.round(s * 10000) / 10000;
		return s;
	}
	
	// Ѱ�����е�Trip
	@SuppressWarnings("unchecked")
	public List<Trip>[] findAll() {
		List<Trip>[] allTrip = new ArrayList[TIME_SLICE];
		for(int i=0;i<TIME_SLICE;i++){
			allTrip[i] = new ArrayList<Trip>();
		}
		Mongo connection = null;
		DB db = null;
		DBCollection dbcoll = null;
		DBCursor dbcsor = null;
		try {
			connection = new MongoClient( "127.0.0.1" , 27017 );
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		db = connection.getDB("MapLocNew");
		dbcoll = db.getCollection("mapCar");
		dbcsor = dbcoll.find();
		// ����ÿһ�����⳵��¼
		while(dbcsor.hasNext()){
			DBObject cObject = dbcsor.next();
			String pickuptime = (String) cObject.get("pickuptime");
			String dorpofftime = (String) cObject.get("dorpofftime");
			double distance = (double) cObject.get("distance");
			Map<String, Double> pickup = (Map<String, Double>) cObject
					.get("pickup");
			Map<String, Double> dropoff = (Map<String, Double>) cObject
					.get("dropoff");
			double pickup_lat = pickup.get("lat");
			double pickup_lng = pickup.get("lon");
			double dropoff_lat = dropoff.get("lat");
			double dropoff_lng = dropoff.get("lon");
			Point o = findIntersection(pickup_lat, pickup_lng);
			if(o == null) // ���û�ҵ�
				continue;
			Point d = findIntersection(dropoff_lat, dropoff_lng);
			if(d == null) // ���û�ҵ�
				continue;
			if(o.id == d.id) // ����ǻ�·,ȥ����·
				continue;
			double time = (toMillionSeconds(dorpofftime) - toMillionSeconds(pickuptime))/1000.0;
			Trip trip = new Trip(o, d, pickuptime, time);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINESE);
			try {
				@SuppressWarnings("deprecation")
				int hour = sdf.parse(pickuptime).getHours();
				allTrip[hour].add(trip);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		connection.close();
		return allTrip;
	}
	
	// ����һ���㣬Ѱ�����������·��
	public Point findIntersection(double lat, double lng) {
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
		// Ѱ����100�׷�Χ�ڵ�����·��
		DBObject object = new BasicDBObject("gis", new BasicDBObject("$within",
				new BasicDBObject("$center", Arrays.asList(
						Arrays.asList(lat, lng), ITHRESHOLD / 111.12))));
		dbcsor = dbcoll.find(object);
		double min = Double.MAX_VALUE;
		long res_id = 0;
		double res_lat = 0;
		double res_lng = 0;
		// �����ҳ������
		while(dbcsor.hasNext()){
			DBObject cObject = dbcsor.next();
			long id = (long) cObject.get("_id");
			@SuppressWarnings("unchecked")
			Map<String, Double> gis = (Map<String, Double>) cObject
					.get("gis");
			double latt = gis.get("lat");
			double lngg = gis.get("lon");
			double dis = distance(lat, lng, latt, lngg);
			if(dis<min){
				min = dis;
				res_id = id;
				res_lat = latt;
				res_lng = lngg;
			}
		}
		connection.close();  // �ر����ݿ�����
		// ���û���������ڵ�·��
		if(min==Double.MAX_VALUE)
			return null;
		Point point = new Point(res_lat, res_lng, res_id);
		return point;
	}
	
	// ������ת��Ϊ����
	public long toMillionSeconds(String str){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINESE);
        long millionSeconds = 0;
		try {
			millionSeconds = sdf.parse(str).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//����
		return millionSeconds;
	}

	// �ϲ��ȼ�Trip,ͬʱȥ��̫�̻�̫����·��
	public List<Trip>[] mergeEquivalent(){
//		if(allTrip==null)
//			this.findAll();
		List<Trip>[] allTrip = findAll();
		@SuppressWarnings("unchecked")
		List<Trip>[] res = new List[TIME_SLICE];
		
		Mongo connection = null;
		DB db = null;
		DBCollection dbcoll = null;
		try {
			connection = new MongoClient( "127.0.0.1" , 27017 );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = connection.getDB("MapLocNew");
		
		for(int i=0;i<TIME_SLICE;i++){
//			List<Trip> tripList = new ArrayList<>();
			Map<String, List<Trip>> map = new HashMap<>(); // ���ڴ�ŵȼ�Trip
			for(Trip trip:allTrip[i]){
				String key = trip.getO().id+"_"+trip.getD().id;
				if(map.containsKey(key))
					map.get(key).add(trip);
				else{
					List<Trip> list = new ArrayList<>();
					list.add(trip);
					map.put(key, list);
				}
			}
			
			String collName = "trip"+i;
			dbcoll = db.getCollection(collName);
			if(db.collectionExists(collName)){
				dbcoll.drop();
			}
			
			// ����map�е�ÿһ���ȼ��࣬���кϲ�
			for(List<Trip> list:map.values()){
				Point o = list.get(0).getO();
				Point d = list.get(0).getD();
				int n = list.size();
				double travelTime = 0;
				for(Trip trip:list){
					travelTime += trip.getTravelTime();
				}
				travelTime /= n; // ʱ��ȡƽ��ֵ
				//ȥ��̫�̻�̫����Trip,t<2min��t>1h
				if(travelTime<120||travelTime>3600)
					continue;
				Trip myTrip = new Trip(o, d, travelTime);
				
				DBObject object = new BasicDBObject();
				object.put("sid", o.id);
				object.put("eid", d.id);
				object.put("travelTime", travelTime);
				dbcoll.insert(object);
				
//				tripList.add(myTrip);
			}
//			res[i] = tripList;
		}
		connection.close();
		return res;
	}
	
//	public static void main(String[] args) {
//		FindTrip findTrip = new FindTrip();
//		List<Trip>[] allTrip = findTrip.findAll();
//		List<Trip>[] mergeTrip = findTrip.mergeEquivalent();
//		System.out.println(allTrip[11].size()+":"+mergeTrip[11].size());
//		for(int i=0;i<24;i++){
//			System.out.println("=============================="+i+"==============================");
//			for(Trip trip:allTrip[i]){
//				System.out.println(trip.getStartTime()+", "+trip.getTravelTime());
//				trip.getO().print();
//				trip.getD().print();
//			}
//			System.out.println("=================================================================");
//			for(Trip trip:mergeTrip[i]){
//				System.out.println(trip.getStartTime()+", "+trip.getTravelTime());
//				trip.getO().print();
//				trip.getD().print();
//			}
//			System.out.println("=============================="+i+"==============================");
//		}
//	}

}
