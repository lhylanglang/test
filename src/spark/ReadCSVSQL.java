package spark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
/**
 * ��ȡԭʼCSV�ļ����ҳ���γ�ȵķ�Χ���Ա��ȡ��ͼ
 * @author Administrator
 *
 */
public class ReadCSVSQL {
	public static void main(String[] args){
		SparkConf sparkConf = new SparkConf().setAppName("ReadCSVSQL").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		SQLContext sqlContext = new SQLContext(sc); 
		//----------------��ʱ��--------------------//
		long startMili=System.currentTimeMillis();// ��ʼʱ��
		System.out.println("��ʼʱ�䣺 "+startMili);
		//----------------��ʱ��--------------------//
		DataFrame cars = sqlContext.read()
			    .format("com.databricks.spark.csv")
			    .option("inferSchema", "true")
			    .option("header", "true")
			    .load("D:\\BaiduYunDownload\\ŦԼ��������\\15��1-6�Ƴ�-�̳�����\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06_1.csv");
//		DataFrame cars = (new CsvParser()).withUseHeader(true)
//				.csvFile(sqlContext, "D:\\BaiduYunDownload\\ŦԼ��������\\15��1-6�Ƴ�-�̳�����\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06.csv");
		cars.show();
		cars.printSchema();
		cars.registerTempTable("Taxi");

		//ʹ��sql����������С��γ��
		DataFrame scope = sqlContext.sql("SELECT MIN(pickup_longitude),MAX(pickup_longitude),MIN(pickup_latitude),MAX(pickup_latitude)"
				+ ",MIN(dropoff_longitude),MAX(dropoff_longitude),MIN(dropoff_latitude),MAX(dropoff_latitude) FROM Taxi");		
		double minlon = Math.min(scope.first().getDouble(0), scope.first().getDouble(4));		
		double maxlon = Math.max(scope.first().getDouble(1), scope.first().getDouble(5));
		double minlat = Math.min(scope.first().getDouble(2), scope.first().getDouble(6));
		double maxlat = Math.max(scope.first().getDouble(3), scope.first().getDouble(7));
		System.out.println("minlon:"+minlon+", maxlon:"+maxlon+", minlat:"+minlat+", maxlat:"+maxlat);

		//----------------��ʱ��--------------------//
		long stopMili=System.currentTimeMillis();// ����ʱ��
		System.out.println("����ʱ�䣺 "+stopMili);
		System.out.println("��ʱ�� "+(stopMili-startMili)/1000.0+"s");
		//----------------��ʱ��--------------------//
	}
}
