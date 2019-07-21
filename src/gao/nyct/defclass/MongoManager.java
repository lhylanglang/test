package gao.nyct.defclass;
import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

public class MongoManager {
	private final static String HOST = "localhost";// 端口
	private final static int PORT = 27017;// 端口
	private final static int POOLSIZE = 100;// 连接数量
	private final static int BLOCKSIZE = 100; // 等待队列长度
	private static Mongo mongo = null;

	private MongoManager() { }

	static {
		initDBPrompties();
	}

	public static DB getDB(String dbName) {
		return mongo.getDB(dbName);
	}

	/**
	 * 初始化连接池
	 */
	private static void initDBPrompties() {
		// 其他参数根据实际情况进行添加
		try {
			mongo = new MongoClient(HOST, PORT);
			MongoOptions opt = mongo.getMongoOptions();
			opt.connectionsPerHost = POOLSIZE;
			opt.threadsAllowedToBlockForConnectionMultiplier = BLOCKSIZE;
		} catch (UnknownHostException e) {
		} catch (MongoException e) {

		}

	}
}
