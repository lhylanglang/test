package spark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
/**
 * 读取原始CSV文件，找出经纬度的范围，以便截取地图
 * @author Administrator
 *
 */
public class ReadCSVSQL {
	public static void main(String[] args){
		SparkConf sparkConf = new SparkConf().setAppName("ReadCSVSQL").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		SQLContext sqlContext = new SQLContext(sc); 
		//----------------计时器--------------------//
		long startMili=System.currentTimeMillis();// 开始时间
		System.out.println("开始时间： "+startMili);
		//----------------计时器--------------------//
		DataFrame cars = sqlContext.read()
			    .format("com.databricks.spark.csv")
			    .option("inferSchema", "true")
			    .option("header", "true")
			    .load("D:\\BaiduYunDownload\\纽约出租数据\\15年1-6黄车-绿车数据\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06_1.csv");
//		DataFrame cars = (new CsvParser()).withUseHeader(true)
//				.csvFile(sqlContext, "D:\\BaiduYunDownload\\纽约出租数据\\15年1-6黄车-绿车数据\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06.csv");
		cars.show();
		cars.printSchema();
		cars.registerTempTable("Taxi");

		//使用sql语句找最大最小经纬度
		DataFrame scope = sqlContext.sql("SELECT MIN(pickup_longitude),MAX(pickup_longitude),MIN(pickup_latitude),MAX(pickup_latitude)"
				+ ",MIN(dropoff_longitude),MAX(dropoff_longitude),MIN(dropoff_latitude),MAX(dropoff_latitude) FROM Taxi");		
		double minlon = Math.min(scope.first().getDouble(0), scope.first().getDouble(4));		
		double maxlon = Math.max(scope.first().getDouble(1), scope.first().getDouble(5));
		double minlat = Math.min(scope.first().getDouble(2), scope.first().getDouble(6));
		double maxlat = Math.max(scope.first().getDouble(3), scope.first().getDouble(7));
		System.out.println("minlon:"+minlon+", maxlon:"+maxlon+", minlat:"+minlat+", maxlat:"+maxlat);

		//----------------计时器--------------------//
		long stopMili=System.currentTimeMillis();// 结束时间
		System.out.println("结束时间： "+stopMili);
		System.out.println("用时： "+(stopMili-startMili)/1000.0+"s");
		//----------------计时器--------------------//
	}
}
