package data.store.hbase;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
/**
 * 将出租车行驶信息存入Mongodb数据库（仅作测试使用，最后将存入HBase数据库）
 * @author Administrator
 *
 */
public class MdbCar {

	static Mongo connection = null;
	static DB db = null;
	static DBCollection coll = null;

	public static void main(String[] args) throws IOException {
		SparkConf sparkConf = new SparkConf().setAppName("ReadCSV").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		SQLContext sqlContext = new SQLContext(sc); 
		connection = new MongoClient( "127.0.0.1" , 27017 );
		db = connection.getDB("MapLocNew");
		coll = db.getCollection("mapCar");
		if(db.collectionExists("mapCar")){
			coll.drop();
		}
		//----------------计时器--------------------//
		long startMili=System.currentTimeMillis();// 开始时间
		System.out.println("开始时间： "+startMili);
		//----------------计时器--------------------//
		DataFrame cars = sqlContext.read()
			    .format("com.databricks.spark.csv")
			    .option("inferSchema", "true")
			    .option("header", "true")
			    .load("E:\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06_1.csv");

		// 将符合要求的数据存入mongodb数据库
		cars.javaRDD().repartition(12).foreach(new VoidFunction<Row>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6536636165920970629L;

			@Override
			public void call(Row row) throws Exception {
				int vendorID = row.getInt(0);
				Date tpep_pickup_datetime = row.getTimestamp(1);
				Date tpep_dropoff_datetime = row.getTimestamp(2);				
				int passenger_count = row.getInt(3);
				double trip_distance = row.getDouble(4);				
				double pickup_longitude = row.getDouble(5);
				double pickup_latitude = row.getDouble(6);
				int ratecodeID = row.getInt(7);
				String store_and_fwd_flag = row.getString(8);
				double dropoff_longitude = row.getDouble(9);
				double dropoff_latitude = row.getDouble(10);
				int payment_type = row.getInt(11);
				double fare_amount = row.getDouble(12);
				double extra = row.getDouble(13);
				double mta_tax = row.getDouble(14);
				double tip_amount = row.getDouble(15);
				double tolls_amount = row.getDouble(16);
				double improvement_surcharge = row.getDouble(17);
				double total_amount = row.getDouble(18);
				double minlon = Math.min(pickup_longitude, dropoff_longitude);
				double maxlon = Math.max(pickup_longitude, dropoff_longitude);
				double minlat = Math.min(pickup_latitude, dropoff_latitude);
				double maxlat = Math.max(pickup_latitude, dropoff_latitude);
				//去掉异常点然后存入Mongodb
				if(minlon>-180&&maxlon<180&&minlat>-90&&maxlat<90&&(pickup_latitude!=0||pickup_latitude!=0)&&(dropoff_longitude!=0||dropoff_latitude!=0)){
					DBObject object = new BasicDBObject();
					Map<String, Double> pickup = new LinkedHashMap<String, Double>();
					Map<String, Double> dropoff = new LinkedHashMap<String, Double>();
					object.put("pickuptime", tpep_pickup_datetime.toString());
					object.put("dorpofftime", tpep_dropoff_datetime.toString());
					pickup.put("lat", pickup_latitude);
					pickup.put("lon", pickup_longitude);
					dropoff.put("lat", dropoff_latitude);
					dropoff.put("lon", dropoff_longitude);
					object.put("pickup", pickup);
					object.put("dropoff", dropoff);
					object.put("distance", trip_distance);
					coll.insert(object);
				}
			}	
			
		});
		connection.close();
		System.out.println("数据插入完毕！");
		//----------------计时器--------------------//
		long stopMili=System.currentTimeMillis();// 结束时间
		System.out.println("结束时间： "+stopMili);
		System.out.println("用时： "+(stopMili-startMili)/1000.0+"s");
		//----------------计时器--------------------//
					
	}

}
