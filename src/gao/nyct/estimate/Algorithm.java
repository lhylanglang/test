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
 * 估计行车时间的主要算法类
 * @author Administrator
 *
 */
public class Algorithm {	
	
    public HashMap<Long,Line> lineSet; // 道路集合
    public Set<Long> tripStreet; // 旅途经过的所有道路的id
    
	public Algorithm(){
		// 过滤trip，并将过滤后的trip存入数据库，只需执行一次
//    	FindTrip findTrip = new FindTrip();
//    	findTrip.mergeEquivalent(); // 按时间分片并过滤后的Trip数据
	}
	
	/**
	 * 初始化路径计算，同时对trip进行第二次的过滤
	 */
	@SuppressWarnings("unchecked")
	public void preprocess(String tripi){
    	FindStreet findStreet = new FindStreet();
    	lineSet = findStreet.getStreet();
    	HashMap<Long,Point> pointSet = findStreet.getPointSet(); // 道路交叉点集
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
		//查询是否计算过
		String collName = "shortestPath";
		DBCollection coll = db.getCollection(collName);
		DBCursor cursor = null;
		
//int j=0; // 用于跳出循环
		
		while(dbcsor.hasNext()){
//			if(j==15)
//				break;
//			j++;
			DBObject cObject = dbcsor.next();
			long sid = (long) cObject.get("sid");
			long eid = (long) cObject.get("eid");
			double travelTime = (double) cObject.get("travelTime");
			cursor = coll.find(new BasicDBObject("_id", sid+"-"+eid));
			if(cursor.hasNext()){ // 如果已经算过
				DBObject dbObject = cursor.next();
				double length = (double) dbObject.get("length");
				double averageSpeed = length/travelTime; // 计算平均速度
				if(averageSpeed<0.5||averageSpeed>30){ // 移除速度太慢或太快的旅途
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
			if(cursor.hasNext()){ // 如果已经算过
				DBObject dbObject = cursor.next();
				double length = (double) dbObject.get("length");
				double averageSpeed = length/travelTime; // 计算平均速度
				if(averageSpeed<0.5||averageSpeed>30){ // 移除速度太慢或太快的旅途
					dbcoll.remove(cObject);
					continue;
				}
				List<Long> list = (List<Long>) dbObject.get("path");
				List<Long> res = new ArrayList<>();
				int n = list.size();
				for(int i=n-1;i>=0;i--){ // 反转
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
//        		System.out.println("图连通");
//        	else
//        		System.out.println("图不连通");
        	double length = dij.solve(sid, eid);
        	double averageSpeed = length/travelTime; // 计算平均速度
			if(averageSpeed<0.5||averageSpeed>30){ // 移除速度太慢或太快的旅途
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
	 * 算法的计算迭代
	 * @param tripi 要计算的时间片
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
//int j=0; // 用于跳出循环
		// 查找所有的trip
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
		System.out.println("Trip的个数为："+tripList.size());
		// 查询计算出的最短路
		String collName = "shortestPath";
		DBCollection coll = db.getCollection(collName);
		DBCursor cursor = null;
		while(flag){
			flag = false;
			Map<Long, List<Trip>> ts = new HashMap<>(); // 用于存放道路和所有经过它的Trip的集合的映射关系
			double relErr = 0; // 相对误差
			for(Trip trip:tripList){
				cursor = coll.find(new BasicDBObject("_id", trip.sid+"-"+trip.eid));
//				if(!cursor.hasNext()) // 如果没找到最短路径
//					continue;
				DBObject dbObject = cursor.next();
				List<Long> path = (List<Long>) dbObject.get("path");
				double et = 0; // 估计的旅途行车时间
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
				trip.length = (double) dbObject.get("length"); // 最短路径长度
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
				double newRelErr = 0; // 新的相对误差
				List<Double> newetList = new ArrayList<>(); // 用于存放新估计的旅途行车时间，要和trip保持同样的顺序
				for(Trip trip:tripList){
					cursor = coll.find(new BasicDBObject("_id", trip.sid+"-"+trip.eid));
//					if(!cursor.hasNext()) // 如果没找到最短路径
//						continue;
					DBObject dbObject = cursor.next();
					List<Long> path = (List<Long>) dbObject.get("path");
					double newet = 0; // 新估计的旅途行车时间
					for(long id:path){
						newet += lineSet.get(id).travelTime;
					}
					newetList.add(newet);
					newRelErr += Math.abs(newet-trip.getTravelTime())/trip.getTravelTime();
				}
				if(newRelErr<relErr){ // 新的估计比之前的好
					int n = tripList.size();
					for(int i=0;i<n;i++){
						Trip trip = tripList.get(i);
						trip.et = newetList.get(i); // 更新旅途行车时间的估计
					}
					relErr = newRelErr;
//					flag = true;
//					break;
				}else{ // 新的估计比之前的坏
					k = 1+(k-1)*0.75; // 减少道路行车时间的增加/减少步长
					if(k<1.0001) // k太小，跳出 内部循环
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

		public long id; // nTripStreet中道路id
		public List<Long> list; // 存放nTripStreet中道路与在tripStreet中具有共同交叉路口的道路集合
		
		public NStreet(){}
		public NStreet(long id, List<Long> list){
			this.id = id;
			this.list = list;
		}
		
//		// 从大到小排序
//		@Override
//		public int compareTo(NStreet o) {
//			if(this.list.size()<o.list.size())
//				return 1;
//			if(this.list.size()>o.list.size())
//				return -1;
//			return 0;
//		}
		
		// 从小到大排序
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
	 * 计算余下道路的行车时间
	 * @param i 第几个时间段
	 */
	public void remain(int i){
		List<Long> nTripStreet = new ArrayList<>(); // 旅途没有经过的其他道路
		for(long id:lineSet.keySet()){
			if(!tripStreet.contains(id)){
				nTripStreet.add(id);
			}
		}
		
		// 第一次计算
		List<NStreet> nmap = new ArrayList<>(); // 存放nTripStreet中道路与在tripStreet中具有共同交叉路口的道路集合的映射关系
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
			// 如果该点与已计算过的点没有任何联系,由于是最大的,所以后面的点都属于其他联通子图,计算结束
			if(nStreet.list.size()==0){
				break;
			}
			double v = 0.0; // 道路速度
			for(long id:nStreet.list){
				if(lineSet.get(id).length == 0.0 && lineSet.get(id).travelTime == 0.0) // 避免NaN的出现
					continue;
				v += lineSet.get(id).length/lineSet.get(id).travelTime;
			}
			
			if(v == 0.0){ // 不再计算保留原来的值
				nmap.remove(nStreet);
				continue;
			}
				
			v /= nStreet.list.size();
			lineSet.get(nStreet.id).travelTime = lineSet.get(nStreet.id).length/v;
//			nmap.remove(0); // 移除已经计算的
			nmap.remove(nStreet); // 移除已经计算的
			// 更新nmap中已经计算过list中的道路id
			long sid = lineSet.get(nStreet.id).p[0].id;
			long eid = lineSet.get(nStreet.id).p[1].id;
			for(NStreet nst:nmap){
				long sid1 = lineSet.get(nst.id).p[0].id;
				long eid1 = lineSet.get(nst.id).p[1].id;
				if(sid1==sid||sid1==eid||eid1==sid||eid1==eid)
					nst.list.add(nStreet.id);
			}
		}
		
		// 将结果存入mapArc数据库中
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
