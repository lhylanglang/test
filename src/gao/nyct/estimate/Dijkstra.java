package gao.nyct.estimate;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import gao.nyct.defclass.Line;
import gao.nyct.defclass.MapLoc;
import gao.nyct.defclass.MongoManager;

public class Dijkstra {
	public static final double EPS = 1e-8; // 判断两点是否在同一地点的精度，差值小于此值认为相等
	public static final double INF = 2100000000.0; //设置无穷取值
	public static final double SPEED = 6.1388889; // 道路平均时速，单位为m/s  22.1km/h
	// 边
	public class Edge {
		public long to, id;
		public int next;
		public double len;
	}

	// 节点
	public class Node implements Comparable<Node> {
		public long to;
		public double len;

		public Node() {
		}

		public Node(long To, double Len) {
			this.to = To;
			this.len = Len;
		}

		public int compareTo(Node other) {
			if (this.len < other.len)
				return -1;
			else if(this.len == other.len)
				return 0;
			return 1;
		}
	}

	public Random rand; // 用以产生随机数
	public Edge[] e;// 边数组
	public HashMap<Long, Integer> box;
	public HashMap<Long, Boolean> vis;
	public HashMap<Long, Double> dis; // 存放到初始点的距离
	public HashMap<Long, Long> flag;
	public MapLoc mymp = null;
	public int cnt;// 边数量
	
	// 无参构造方法
	public Dijkstra() {
		
	}
	
	// 有参构造方法
	public Dijkstra(MapLoc mymp) {
		this.mymp = mymp;
		rand = new Random();
		int lnum = mymp.LineNum * 2 + 10;
		e = new Edge[lnum];
		for (int i = 0; i < lnum; i++) {
			e[i] = new Edge();
		}
		box = new HashMap<Long, Integer>();
		vis = new HashMap<Long, Boolean>();
		dis = new HashMap<Long, Double>();
		flag = new HashMap<Long, Long>();
		cnt = 0;
		buildMap();
	}

	// 构建地图
	public void buildMap() {
		// 初始化地图信息
		for (Long id : mymp.PointSet.keySet()) {// 遍历点集合中的点数据
			box.put(id, -1);
			vis.put(id, false);
			dis.put(id, INF);
//			px.put(id, new Path(-1, -1));
			flag.put(id, (long) -1);
		}
		cnt = 0;
		// 构建地图
		for (Long id : mymp.LineSet.keySet()) {
			Line line = mymp.LineSet.get(id);
			long from = line.p[0].id;
			long to = line.p[1].id;
			double len = line.length;
			Add(from, to, len, id);
			Add(to, from, len, id);
		}
		// System.out.println("Pointnum = "+mymp.PointNum);
		// System.out.println("连通分量数"+connectNum(mymp));
		// System.out.println(cnt);
	}


	// 添加边信息
	public void Add(long from, long to, double len, long id) {
		e[cnt].to = to;
		e[cnt].id = id;
		e[cnt].len = len;
		e[cnt].next = box.get(from);
		box.put(from, cnt);
		cnt++;
	}

	public HashMap<Long, Boolean> Vis;

	// 判断地图是否连通
	public int dfs(long now, long T) {
		if (now == T)
			return 1;
		Vis.put(now, true);
		for (int t = box.get(now); t != -1; t = e[t].next) {
			long v = e[t].to;
			if (!Vis.get(v)) {
				if (dfs(v, T) == 1)
					return 1;
			}
		}
		return 0;
	}

	public int dfs(long now) {
		int sum = 1;
		Vis.put(now, true);
		for (int t = box.get(now); t != -1; t = e[t].next) {
			long v = e[t].to;
			if (!Vis.get(v)) {
				sum += dfs(v);
			}
		}
		return sum;
	}

	public int connectNum(MapLoc mymp) {
		Vis = new HashMap<Long, Boolean>();
		int num = 0;
		for (long id : mymp.PointSet.keySet()) {
			Vis.put(id, false);
		}
		for (Long id : mymp.PointSet.keySet()) {
			if (!Vis.get(id)) {
				System.out.println("connection " + (num + 1) + " = "
						+ dfs(id));
				num++;
			}
		}
		return num;
	}

	// 判断图是否连通
	public boolean isok(long S, long T) {
		Vis = new HashMap<Long, Boolean>();
		for (long id : mymp.PointSet.keySet()) {
			Vis.put(id, false);
		}
		if (dfs(S, T) == 1)
			return true;
		return false;
	}

	// 求S到T的最短路 避免初始化
	public double solve(long S, long T) {
		
		if (S == T)
			return 0.0;	
		// System.out.println(isok(mymp,S,T));
		Queue<Node> pq = new PriorityQueue<Node>();
		long tmp = Math.abs(rand.nextLong());
		dis.put(S, 0.0);
		flag.put(S, tmp);
		vis.put(S, false);
		pq.add(new Node(S, 0.0));
		
		// 用于临时存储的数据库
		String dbName = "MapLoc"+S+"-"+T;
		DB dbTemp = MongoManager.getDB(dbName);
		String shortestPathTemp = "shortestPathTemp";
		DBCollection coll = dbTemp.getCollection(shortestPathTemp);
		DBCursor cursor = null;
		while (!pq.isEmpty()) {
			Node now = pq.poll();
			long po = now.to;
			
			if(po == T)
				break;
				
			String _id = S+"-"+po;
			// 如果为起点
			if(S==po){
				DBObject object = new BasicDBObject();
				object.put("_id", _id);
				object.put("length", 0.0);
				coll.insert(object);
			}
			if (flag.get(po) == tmp && vis.get(po)) {
				// System.out.println("f");
				continue;
			}
			vis.put(po, true);
			flag.put(po, tmp);
			for (int t = box.get(po); t != -1; t = e[t].next) {
				long v = e[t].to;
				double len = e[t].len;				
				if (flag.get(v) != tmp && (len + now.len < dis.get(v))) {
					dis.put(v, len + now.len);
					cursor = coll.find(new BasicDBObject("_id", S+"-"+v));
					if(!cursor.hasNext()){
						DBObject object = new BasicDBObject();
						object.put("_id", S+"-"+v);
						object.put("length", dis.get(v));
						coll.insert(object);
					}else{
						DBObject updateCondition = new BasicDBObject();
						updateCondition.put("_id", S+"-"+v);
						DBObject updateValue = new BasicDBObject();
						updateValue.put("length", dis.get(v));
						DBObject updateSetValue = new BasicDBObject("$set", updateValue);
						coll.update(updateCondition, updateSetValue);
					}
					pq.add(new Node(v, len + now.len));
				}
			}
		}
		
		// 将临时数据库中的数据存入shortestPath
		cursor = coll.find(new BasicDBObject("_id", S+"-"+T));
		if(!cursor.hasNext()){
			// 删除临时数据库
			dbTemp.dropDatabase();
			System.out.println("数据库"+dbName+"已删除！");		
			return INF;
		}
		DBObject dbObject = cursor.next();
		double time = (double) dbObject.get("length")/SPEED;
		dbObject.put("time", time);
		DB db = MongoManager.getDB("MapLocNew");
		String collName = "shortestPath";
		DBCollection dbcoll = db.getCollection(collName);
		dbcoll.insert(dbObject);
		// 删除临时数据库
		dbTemp.dropDatabase();
		System.out.println("数据库"+dbName+"已删除！");		
		return dis.get(T);
	}
	
	public void printPathInfo(long sid) {
		DB db = null;
		DBCollection dbcoll = null;
		DBCursor dbcursor = null;
		db = MongoManager.getDB("MapLocNew");
		String collName = "shortestPath";
		dbcoll = db.getCollection(collName);
		DBObject query = new BasicDBObject();
		Pattern pattern = Pattern.compile(sid+"-.*$");
		query.put("_id", new BasicDBObject("$regex", pattern));
		dbcursor = dbcoll.find(query);
		while(dbcursor.hasNext()){
			DBObject dbObject = dbcursor.next();
	    	double length = (double) dbObject.get("length");
	    	String id = (String) dbObject.get("_id");
	    	double time = (double) dbObject.get("time");
	    	System.out.println(id+": length:"+length+", time:"+time);
		}
	}
	
	public void printPathInfo(long sid, long eid) {
		DB db = null;
		DBCollection dbcoll = null;
		DBCursor dbcursor = null;
		db = MongoManager.getDB("MapLocNew");
		String collName = "shortestPath";
		dbcoll = db.getCollection(collName);
		dbcursor = dbcoll.find(new BasicDBObject("_id", sid+"-"+eid));
    	DBObject dbObject = dbcursor.next();
    	double length = (double) dbObject.get("length");
    	double time = (double) dbObject.get("time");
    	System.out.println(sid+"-"+eid+": length:"+length+", time:"+time);
	}

}
