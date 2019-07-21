package gao.nyct.defclass;
import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

public class MongoManager {
	private final static String HOST = "localhost";// �˿�
	private final static int PORT = 27017;// �˿�
	private final static int POOLSIZE = 100;// ��������
	private final static int BLOCKSIZE = 100; // �ȴ����г���
	private static Mongo mongo = null;

	private MongoManager() { }

	static {
		initDBPrompties();
	}

	public static DB getDB(String dbName) {
		return mongo.getDB(dbName);
	}

	/**
	 * ��ʼ�����ӳ�
	 */
	private static void initDBPrompties() {
		// ������������ʵ������������
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
