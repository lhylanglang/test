package gao.map.preprocess;

import java.io.IOException;
import java.util.List;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

/**
 * �Ӵ���mongodb�еĵ���Ϣ��Ѱ�ҵ�·����㣬������mongodb���ݿ�
 * @author Administrator
 *
 */
public class FindAndInsertIntersection {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		
		Mongo connection = new MongoClient( "127.0.0.1" , 27017 );
		DB db = connection.getDB("MapLocNew");
		// �ж����ݼ����Ƿ���ڣ������ڴ������ݼ���
		String collectionName = "mapIntersection";
		// ɾ�����ؽ�
		if(db.collectionExists(collectionName)){
			db.getCollection(collectionName).drop();
		}
		DBObject dbs = new BasicDBObject();
//		db.createCollection(collectionName, dbs);
		
		DBCursor dbcsor = null; //-----------���ݿ��α�
		DBCollection dbcoll = null;  //--------��
		
		dbcoll = db.getCollection("mapPoint"); //��ȡ���ݱ�				
		dbcsor = dbcoll.find(); //��ȡ���ж�����Ϣ
		
		//�������ж��󣬽����������Ĳ������ݿ�
		while (dbcsor.hasNext()) {
			DBObject dbObject = dbcsor.next();
			List<Long> list = (List<Long>) dbObject.get("edge");
			if(list!=null&&list.size()>1){ //����ǽ���㣬�ڶ�������,�򽫸õ����mapIntersection
				DBCollection coll = db.getCollection(collectionName);
				coll.insert(dbObject);
			}
		}
		db.getCollection(collectionName).ensureIndex(new BasicDBObject("gis","2d"));
		connection.close();
		System.out.println("�������ݿ���ɣ�");
	}
	
}
