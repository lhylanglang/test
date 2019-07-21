import gao.nyct.defclass.GeoHash;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

/**
 * �Ծ�γ�Ƚ��з���
 * @author Administrator
 *
 */
public class Zoning {
	
	public static GeoHash geohash = null; //hash������
	
	public static void main(String[] args) {
		SparkConf sparkConf = new SparkConf().setAppName("OutputGps").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		SQLContext sqlContext = new SQLContext(sc); 
		geohash = new GeoHash();
		//----------------��ʱ��--------------------//
		long startMili=System.currentTimeMillis();// ��ʼʱ��
		System.out.println("��ʼʱ�䣺 "+startMili);
		//----------------��ʱ��--------------------//
		DataFrame cars = sqlContext.read()
			    .format("com.databricks.spark.csv")
			    .option("inferSchema", "true")
			    .option("header", "true")
			    .load("D:\\BaiduYunDownload\\ŦԼ��������\\15��1-6�Ƴ�-�̳�����\\yellow_tripdata_2015-06.csv\\yellow_tripdata_2015-06_1.csv");
		JavaRDD<String> regions = cars.javaRDD().repartition(12).flatMap(new FlatMapFunction<Row,String>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 313083597826471274L;

			@Override
			public Iterable<String> call(Row row) throws Exception {
				int i1 = row.fieldIndex("pickup_longitude");
				int i2 = row.fieldIndex("pickup_latitude");
				int i3 = row.fieldIndex("dropoff_longitude");
				int i4 = row.fieldIndex("dropoff_latitude"); 
				double lonp = row.getDouble(i1);
				double latp = row.getDouble(i2);
				double lond = row.getDouble(i3);
				double latd = row.getDouble(i4);
				String pickup = ""; //�ϳ��������
				String dropoff = ""; //�³��������
				int n = 6; //���볤�ȣ��������򻮷����ȣ�Խ����������ԽС
				if(lonp<180&&lonp>-180&&latp<90&&latp>-90&&!(lonp==0&&latp==0))
					pickup = geohash.encode(latp, lonp).substring(0, n);
				if(lond<180&&lond>-180&&latd<90&&latd>-90&&!(lond==0&&latd==0))
					dropoff = geohash.encode(latd, lond).substring(0, n);
				return Arrays.asList(pickup,dropoff);
			}
			
		});
		
		List<String> res = regions.distinct().collect();
		for(String s:res){
			System.out.println(s);
		}
		//----------------��ʱ��--------------------//
		long stopMili=System.currentTimeMillis();// ����ʱ��
		System.out.println("����ʱ�䣺 "+stopMili);
		System.out.println("��ʱ�� "+(stopMili-startMili)/1000.0+"s");
		//----------------��ʱ��--------------------//

	}

}
