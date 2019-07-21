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
	public static final double EPS = 1e-8; // �ж������Ƿ���ͬһ�ص�ľ��ȣ���ֵС�ڴ�ֵ��Ϊ���
	public static final double INF = 2100000000.0; //��������ȡֵ
	public static final double SPEED = 6.1388889; // ��·ƽ��ʱ�٣���λΪm/s  22.1km/h
	// ��
	public class Edge {
		public long to, id;
		public int next;
		public double len;
	}

	// �ڵ�
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

	public Random rand; // ���Բ��������
	public Edge[] e;// ������
	public HashMap<Long, Integer> box;
	public HashMap<Long, Boolean> vis;
	public HashMap<Long, Double> dis; // ��ŵ���ʼ��ľ���
	public HashMap<Long, Long> flag;
	public MapLoc mymp = null;
	public int cnt;// ������
	
	// �޲ι��췽��
	public Dijkstra() {
		
	}
	
	// �вι��췽��
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

	// ������ͼ
	public void buildMap() {
		// ��ʼ����ͼ��Ϣ
		for (Long id : mymp.PointSet.keySet()) {// �����㼯���еĵ�����
			box.put(id, -1);
			vis.put(id, false);
			dis.put(id, INF);
//			px.put(id, new Path(-1, -1));
			flag.put(id, (long) -1);
		}
		cnt = 0;
		// ������ͼ
		for (Long id : mymp.LineSet.keySet()) {
			Line line = mymp.LineSet.get(id);
			long from = line.p[0].id;
			long to = line.p[1].id;
			double len = line.length;
			Add(from, to, len, id);
			Add(to, from, len, id);
		}
		// System.out.println("Pointnum = "+mymp.PointNum);
		// System.out.println("��ͨ������"+connectNum(mymp));
		// System.out.println(cnt);
	}


	// ��ӱ���Ϣ
	public void Add(long from, long to, double len, long id) {
		e[cnt].to = to;
		e[cnt].id = id;
		e[cnt].len = len;
		e[cnt].next = box.get(from);
		box.put(from, cnt);
		cnt++;
	}

	public HashMap<Long, Boolean> Vis;

	// �жϵ�ͼ�Ƿ���ͨ
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

	// �ж�ͼ�Ƿ���ͨ
	public boolean isok(long S, long T) {
		Vis = new HashMap<Long, Boolean>();
		for (long id : mymp.PointSet.keySet()) {
			Vis.put(id, false);
		}
		if (dfs(S, T) == 1)
			return true;
		return false;
	}

	// ��S��T�����· �����ʼ��
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
		
		// ������ʱ�洢�����ݿ�
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
			// ���Ϊ���
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
		
		// ����ʱ���ݿ��е����ݴ���shortestPath
		cursor = coll.find(new BasicDBObject("_id", S+"-"+T));
		if(!cursor.hasNext()){
			// ɾ����ʱ���ݿ�
			dbTemp.dropDatabase();
			System.out.println("���ݿ�"+dbName+"��ɾ����");		
			return INF;
		}
		DBObject dbObject = cursor.next();
		double time = (double) dbObject.get("length")/SPEED;
		dbObject.put("time", time);
		DB db = MongoManager.getDB("MapLocNew");
		String collName = "shortestPath";
		DBCollection dbcoll = db.getCollection(collName);
		dbcoll.insert(dbObject);
		// ɾ����ʱ���ݿ�
		dbTemp.dropDatabase();
		System.out.println("���ݿ�"+dbName+"��ɾ����");		
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
