package spark;
import java.util.Arrays;
import com.databricks.spark.csv.CsvParser;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF4;
import org.apache.spark.sql.types.DataTypes;

import scala.reflect.api.TypeTags;
import scala.reflect.api.TypeTags.TypeTag;
/**
 * 读取原始CSV文件，找出经纬度的范围，以便截取地图
 * @author Administrator
 *
 */
public class ReadCSV {
	public static void main(String[] args){
		SparkConf sparkConf = new SparkConf().setAppName("ReadCSV").setMaster("local[2]");
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
//		DataFrame min = sqlContext.sql("SELECT pickup_longitude FROM Taxi ORDER BY pickup_longitude ASC");
//		System.out.println(cars.first().toString());	
		//自定义函数作用于每一行
//		sqlContext.udf().register("myFun", new UDF4<Double, Double, Double, Double, String>(){
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public String call(Double d1, Double d2, Double d3,
//					Double d4) throws Exception {
//				return Math.min(d1, d3)+","+Math.max(d1, d3)+","+Math.min(d2, d4)+","+Math.max(d2, d4);
//			}
//			
//		}, DataTypes.StringType);
//		DataFrame scope = sqlContext.sql("SELECT myFun(pickup_longitude,pickup_latitude,dropoff_longitude,dropoff_latitude) FROM Taxi");
//		for(Row row:scope.collect()){
//			System.out.println(row.toString());
//		}
		//排序
//		DataFrame minpickup_lon = cars.sort("pickup_longitude");
//		double lon1 = minpickup_lon.first().getDouble(5);
		//找出最小最大经纬度
		JavaRDD<String> rdd1 = cars.javaRDD().repartition(12).flatMap(new FlatMapFunction<Row,String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6536636165920970629L;

			@Override
			public Iterable<String> call(Row row) throws Exception {
				int i1 = row.fieldIndex("pickup_longitude");
				int i2 = row.fieldIndex("pickup_latitude");
				int i3 = row.fieldIndex("dropoff_longitude");
				int i4 = row.fieldIndex("dropoff_latitude");
				double lon1 = row.getDouble(i1);
				double lat1 = row.getDouble(i2);
				double lon2 = row.getDouble(i3);
				double lat2 = row.getDouble(i4);
				double minlon = Math.min(lon1, lon2);
				double maxlon = Math.max(lon1, lon2);
				double minlat = Math.min(lat1, lat2);
				double maxlat = Math.max(lat1, lat2);
				//去掉异常点
				if(minlon<-180||maxlon>180||minlat<-90||maxlat>90||(lon1==0&&lat1==0)||(lon2==0&&lat2==0))
					return Arrays.asList();
				return Arrays.asList(Math.min(lon1, lon2)+","+Math.max(lon1, lon2)+","+Math.min(lat1, lat2)+","+Math.max(lat1, lat2));
			}			
		});
		
		String scale = rdd1.reduce(new Function2<String, String, String>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 4976109809536845290L;

			@Override
			public String call(String str1, String str2) throws Exception {
				String s1[] = str1.split(",");
				String s2[] = str2.split(",");
				double minlon = Math.min(Double.parseDouble(s1[0]), Double.parseDouble(s2[0]));
				double maxlon = Math.max(Double.parseDouble(s1[1]), Double.parseDouble(s2[1]));
				double minlat = Math.min(Double.parseDouble(s1[2]), Double.parseDouble(s2[2]));
				double maxlat = Math.max(Double.parseDouble(s1[3]), Double.parseDouble(s2[3]));
				return minlon+","+maxlon+","+minlat+","+maxlat;
			}			
		});		
		System.out.println(scale);
		//----------------计时器--------------------//
		long stopMili=System.currentTimeMillis();// 结束时间
		System.out.println("结束时间： "+stopMili);
		System.out.println("用时： "+(stopMili-startMili)/1000.0+"s");
		//----------------计时器--------------------//
	}
}
