package gao.nyct.estimate;

import gao.nyct.defclass.Line;
import gao.nyct.defclass.MapLoc;
import gao.nyct.defclass.Point;
import gao.nyct.defclass.Trip;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

/**
 * �����г�ʱ�����Ҫ�㷨��
 * @author Administrator
 *
 */
public class Algorithm {	
	
    public HashMap<Long,Line> lineSet; // ��·����
    public Set<Long> tripStreet; // ��;���������е�·��id
    
	public Algorithm(){
		// ����trip���������˺��trip�������ݿ⣬ֻ��ִ��һ��
//    	FindTrip findTrip = new FindTrip();
//    	findTrip.mergeEquivalent(); // ��ʱ���Ƭ�����˺��Trip����
	}
	
	/**
	 * ��ʼ��·�����㣬ͬʱ��trip���еڶ��εĹ���
	 */
	@SuppressWarnings("unchecked")
	public void preprocess(String tripi){
    	FindStreet findStreet = new FindStreet();
    	lineSet = findStreet.getStreet();
    	HashMap<Long,Point> pointSet = findStreet.getPointSet(); // ��·����㼯
    	MapLoc mymap = new MapLoc(pointSet, lineSet);
 
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
		dbcoll = db.getCollection(tripi);
		dbcsor = dbcoll.find();
		//��ѯ�Ƿ�����
		String collName = "shortestPath";
		DBCollection coll = db.getCollection(collName);
		DBCursor cursor = null;
		
//int j=0; // ��������ѭ��
		
		while(dbcsor.hasNext()){
//			if(j==15)
//				break;
//			j++;
			DBObject cObject = dbcsor.next();
			long sid = (long) cObject.get("sid");
			long eid = (long) cObject.get("eid");
			double travelTime = (double) cObject.get("travelTime");
			cursor = coll.find(new BasicDBObject("_id", sid+"-"+eid));
			if(cursor.hasNext()){ // ����Ѿ����
				DBObject dbObject = cursor.next();
				double length = (double) dbObject.get("length");
				double averageSpeed = length/travelTime; // ����ƽ���ٶ�
				if(averageSpeed<0.5||averageSpeed>30){ // �Ƴ��ٶ�̫����̫�����;
					dbcoll.remove(cObject);
					continue;
				}
				Dijkstra dij = new Dijkstra();
				System.out.println("=============================================");
				dij.printPathInfo(sid, eid);
				System.out.println("=============================================");
				continue;
			} 
			cursor = coll.find(new BasicDBObject("_id", eid+"-"+sid));
			if(cursor.hasNext()){ // ����Ѿ����
				DBObject dbObject = cursor.next();
				double length = (double) dbObject.get("length");
				double averageSpeed = length/travelTime; // ����ƽ���ٶ�
				if(averageSpeed<0.5||averageSpeed>30){ // �Ƴ��ٶ�̫����̫�����;
					dbcoll.remove(cObject);
					continue;
				}
				List<Long> list = (List<Long>) dbObject.get("path");
				List<Long> res = new ArrayList<>();
				int n = list.size();
				for(int i=n-1;i>=0;i--){ // ��ת
					res.add(list.get(i));
				}
				DBObject object = new BasicDBObject();
				object.put("_id", sid+"-"+eid);
				object.put("path", res);
				object.put("length", length);
				coll.insert(object);
				System.out.println("=============================================");
				System.out.println(res+": "+length);
				System.out.println("=============================================");
				continue;
			}
						
        	Dijkstra dij = new Dijkstra(mymap);
//        	if(dij.isConnected(mymap, trip.getO().id, trip.getD().id))
//        		System.out.println("ͼ��ͨ");
//        	else
//        		System.out.println("ͼ����ͨ");
        	double length = dij.solve(sid, eid);
        	double averageSpeed = length/travelTime; // ����ƽ���ٶ�
			if(averageSpeed<0.5||averageSpeed>30){ // �Ƴ��ٶ�̫����̫�����;
				dbcoll.remove(cObject);
				continue;
			}
        	System.out.println("=============================================");
        	dij.printPathInfo(sid, eid);
        	System.out.println("=============================================");
		}
		
		connection.close();
	}
	
	/**
	 * �㷨�ļ������
	 * @param tripi Ҫ�����ʱ��Ƭ
	 */
	@SuppressWarnings("unchecked")
	public void compute(String tripi){
		
		tripStreet = new HashSet<>();
		boolean flag = true;
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
		dbcoll = db.getCollection(tripi);
		dbcsor = dbcoll.find();
		List<Trip> tripList = new ArrayList<>();
//int j=0; // ��������ѭ��
		// �������е�trip
		while(dbcsor.hasNext()){
//			if(j==15)
//				break;
//			j++;
			
			Trip trip = new Trip();
			DBObject cObject = dbcsor.next();
			trip.sid = (long) cObject.get("sid");
			trip.eid = (long) cObject.get("eid");
			trip.setTravelTime((double) cObject.get("travelTime"));
			tripList.add(trip);
		}
		System.out.println("Trip�ĸ���Ϊ��"+tripList.size());
		// ��ѯ����������·
		String collName = "shortestPath";
		DBCollection coll = db.getCollection(collName);
		DBCursor cursor = null;
		while(flag){
			flag = false;
			Map<Long, List<Trip>> ts = new HashMap<>(); // ���ڴ�ŵ�·�����о�������Trip�ļ��ϵ�ӳ���ϵ
			double relErr = 0; // ������
			for(Trip trip:tripList){
				cursor = coll.find(new BasicDBObject("_id", trip.sid+"-"+trip.eid));
//				if(!cursor.hasNext()) // ���û�ҵ����·��
//					continue;
				DBObject dbObject = cursor.next();
				List<Long> path = (List<Long>) dbObject.get("path");
				double et = 0; // ���Ƶ���;�г�ʱ��
				for(long id:path){
					et += lineSet.get(id).travelTime;
					if(ts.containsKey(id)){
						ts.get(id).add(trip);
					}else{
						List<Trip> list = new ArrayList<>();
						list.add(trip);
						ts.put(id, list);
					}
				}
				trip.et = et;
				trip.length = (double) dbObject.get("length"); // ���·������
				relErr += Math.abs(trip.et-trip.getTravelTime())/trip.getTravelTime();
			}
			tripStreet = ts.keySet();
			double k = 1.2;
			while(true){
				for(long id:ts.keySet()){
					double os = 0;
					for(Trip trip:ts.get(id)){
						os += (trip.et-trip.getTravelTime())*trip.length;
					}
					if(os<0)
						lineSet.get(id).travelTime = k*lineSet.get(id).travelTime;
					else
						lineSet.get(id).travelTime = lineSet.get(id).travelTime/k;
				}
				double newRelErr = 0; // �µ�������
				List<Double> newetList = new ArrayList<>(); // ���ڴ���¹��Ƶ���;�г�ʱ�䣬Ҫ��trip����ͬ����˳��
				for(Trip trip:tripList){
					cursor = coll.find(new BasicDBObject("_id", trip.sid+"-"+trip.eid));
//					if(!cursor.hasNext()) // ���û�ҵ����·��
//						continue;
					DBObject dbObject = cursor.next();
					List<Long> path = (List<Long>) dbObject.get("path");
					double newet = 0; // �¹��Ƶ���;�г�ʱ��
					for(long id:path){
						newet += lineSet.get(id).travelTime;
					}
					newetList.add(newet);
					newRelErr += Math.abs(newet-trip.getTravelTime())/trip.getTravelTime();
				}
				if(newRelErr<relErr){ // �µĹ��Ʊ�֮ǰ�ĺ�
					int n = tripList.size();
					for(int i=0;i<n;i++){
						Trip trip = tripList.get(i);
						trip.et = newetList.get(i); // ������;�г�ʱ��Ĺ���
					}
					relErr = newRelErr;
//					flag = true;
//					break;
				}else{ // �µĹ��Ʊ�֮ǰ�Ļ�
					k = 1+(k-1)*0.75; // ���ٵ�·�г�ʱ�������/���ٲ���
					if(k<1.0001) // k̫С������ �ڲ�ѭ��
						break;
				}
			}
		}
		
for(Trip trip:tripList){
	System.out.println(trip.sid +"->"+trip.eid+": "+trip.getTravelTime()+"s et: "+trip.et+"s");
}
//for(long sid:tripStreet){
//	System.out.println(sid+": "+lineSet.get(sid).travelTime+" s");
//}
		connection.close();
	}
	
	public class NStreet implements Comparable<NStreet>{

		public long id; // nTripStreet�е�·id
		public List<Long> list; // ���nTripStreet�е�·����tripStreet�о��й�ͬ����·�ڵĵ�·����
		
		public NStreet(){}
		public NStreet(long id, List<Long> list){
			this.id = id;
			this.list = list;
		}
		
//		// �Ӵ�С����
//		@Override
//		public int compareTo(NStreet o) {
//			if(this.list.size()<o.list.size())
//				return 1;
//			if(this.list.size()>o.list.size())
//				return -1;
//			return 0;
//		}
		
		// ��С��������
		@Override
		public int compareTo(NStreet o) {
			if(this.list.size()<o.list.size())
				return -1;
			if(this.list.size()>o.list.size())
				return 1;
			return 0;
		}
		
	}
	/**
	 * �������µ�·���г�ʱ��
	 * @param i �ڼ���ʱ���
	 */
	public void remain(int i){
		List<Long> nTripStreet = new ArrayList<>(); // ��;û�о�����������·
		for(long id:lineSet.keySet()){
			if(!tripStreet.contains(id)){
				nTripStreet.add(id);
			}
		}
		
		// ��һ�μ���
		List<NStreet> nmap = new ArrayList<>(); // ���nTripStreet�е�·����tripStreet�о��й�ͬ����·�ڵĵ�·���ϵ�ӳ���ϵ
		for(long id:nTripStreet){
			long sid = lineSet.get(id).p[0].id;
			long eid = lineSet.get(id).p[1].id;
			List<Long> list = new ArrayList<>();
			for(long streetId:tripStreet){
				long sid1 = lineSet.get(streetId).p[0].id;
				long eid1 = lineSet.get(streetId).p[1].id;
				if(sid1==sid||sid1==eid||eid1==sid||eid1==eid)
					list.add(streetId);
			}
			NStreet nStreet = new NStreet(id, list);
			nmap.add(nStreet);
		}
		
		while(!nmap.isEmpty()){
//			Collections.sort(nmap);
//			NStreet nStreet = nmap.get(0);
			NStreet nStreet = Collections.max(nmap);
			// ����õ����Ѽ�����ĵ�û���κ���ϵ,����������,���Ժ���ĵ㶼����������ͨ��ͼ,�������
			if(nStreet.list.size()==0){
				break;
			}
			double v = 0.0; // ��·�ٶ�
			for(long id:nStreet.list){
				if(lineSet.get(id).length == 0.0 && lineSet.get(id).travelTime == 0.0) // ����NaN�ĳ���
					continue;
				v += lineSet.get(id).length/lineSet.get(id).travelTime;
			}
			
			if(v == 0.0){ // ���ټ��㱣��ԭ����ֵ
				nmap.remove(nStreet);
				continue;
			}
				
			v /= nStreet.list.size();
			lineSet.get(nStreet.id).travelTime = lineSet.get(nStreet.id).length/v;
//			nmap.remove(0); // �Ƴ��Ѿ������
			nmap.remove(nStreet); // �Ƴ��Ѿ������
			// ����nmap���Ѿ������list�еĵ�·id
			long sid = lineSet.get(nStreet.id).p[0].id;
			long eid = lineSet.get(nStreet.id).p[1].id;
			for(NStreet nst:nmap){
				long sid1 = lineSet.get(nst.id).p[0].id;
				long eid1 = lineSet.get(nst.id).p[1].id;
				if(sid1==sid||sid1==eid||eid1==sid||eid1==eid)
					nst.list.add(nStreet.id);
			}
		}
		
		// ���������mapArc���ݿ���
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
		dbcoll = db.getCollection("mapArc");
		String timei = "time"+i;
		for(long sid:lineSet.keySet()){
			dbcsor = dbcoll.find(new BasicDBObject("_id", sid));
			DBObject object = dbcsor.next();			
			if(object.get(timei)==null){
				object.put(timei, lineSet.get(sid).travelTime);
				dbcoll.save(object);
			}else{
				DBObject updateCondition = new BasicDBObject();
				updateCondition.put("_id", sid);
				DBObject updateValue = new BasicDBObject();
				updateValue.put(timei, lineSet.get(sid).travelTime);
				DBObject updateSetValue = new BasicDBObject("$set", updateValue);
				dbcoll.update(updateCondition, updateSetValue);
			}
			System.out.println(sid+": "+lineSet.get(sid).travelTime+" s");
		}
		connection.close();
	}
	
	
    public static void main(String[] args) {  
//    	List<NStreet> list = new ArrayList<>();
//        for(int i=1;i<=3;i++){
//        	List<Long> list1 = new ArrayList<>();
//        	for(int j=0;j<i;j++){
//        		list1.add((long) j);
//        	}
//        	NStreet nStreet = new NStreet((long)i, list1);
//        	list.add(nStreet);
//        }
////        Collections.sort(list);
////        list.remove(Collections.max(list));
//        System.out.println(Collections.max(list).list);
    }  
}
