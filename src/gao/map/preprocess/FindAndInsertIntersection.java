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
 * 从存入mongodb中的点信息中寻找道路交叉点，并存入mongodb数据库
 * @author Administrator
 *
 */
public class FindAndInsertIntersection {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		
		Mongo connection = new MongoClient( "127.0.0.1" , 27017 );
		DB db = connection.getDB("MapLocNew");
		// 判断数据集合是否存在，不存在创建数据集合
		String collectionName = "mapIntersection";
		// 删除表，重建
		if(db.collectionExists(collectionName)){
			db.getCollection(collectionName).drop();
		}
		DBObject dbs = new BasicDBObject();
//		db.createCollection(collectionName, dbs);
		
		DBCursor dbcsor = null; //-----------数据库游标
		DBCollection dbcoll = null;  //--------表
		
		dbcoll = db.getCollection("mapPoint"); //获取数据表				
		dbcsor = dbcoll.find(); //获取所有对象信息
		
		//遍历所有对象，将符合条件的插入数据库
		while (dbcsor.hasNext()) {
			DBObject dbObject = dbcsor.next();
			List<Long> list = (List<Long>) dbObject.get("edge");
			if(list!=null&&list.size()>1){ //如果是交叉点，在多条边上,则将该点插入mapIntersection
				DBCollection coll = db.getCollection(collectionName);
				coll.insert(dbObject);
			}
		}
		db.getCollection(collectionName).ensureIndex(new BasicDBObject("gis","2d"));
		connection.close();
		System.out.println("插入数据库完成！");
	}
	
}
