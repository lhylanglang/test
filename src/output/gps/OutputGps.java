package output.gps;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
/**
 * �����³��ص�ľ�γ�����Ϊjson�ļ������࣬�Ա��ڵ�ͼ�ϱ�ע
 * @author Administrator
 *
 */
public class OutputGps {
	public static OutputJson outjsonpickup = null;
	public static OutputJson outjsondropoff = null;
	public static void main(String[] args){
		SparkConf sparkConf = new SparkConf().setAppName("OutputGps").setMaster("local[2]");
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
		//����򳵾�γ��
		outjsonpickup = new OutputJson("H:\\PythonMapStatic\\data2\\nyctaxi\\yellow_tripdata_2015-06_pickup.json");
		outjsonpickup.init();
		outjsondropoff = new OutputJson("H:\\PythonMapStatic\\data2\\nyctaxi\\yellow_tripdata_2015-06_dropoff.json");
		outjsondropoff.init();
		cars.javaRDD().repartition(12).foreach(new VoidFunction<Row>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 313083597826471274L;

			@Override
			public void call(Row row) throws Exception {
				int i1 = row.fieldIndex("pickup_longitude");
				int i2 = row.fieldIndex("pickup_latitude");
				int i3 = row.fieldIndex("dropoff_longitude");
				int i4 = row.fieldIndex("dropoff_latitude"); 
				double lonp = row.getDouble(i1);
				double latp = row.getDouble(i2);
				double lond = row.getDouble(i3);
				double latd = row.getDouble(i4);
				if(lonp<180&&lonp>-180&&latp<90&&latp>-90&&!(lonp==0&&latp==0))
					outjsonpickup.write(lonp, latp);	
				if(lond<180&&lond>-180&&latd<90&&latd>-90&&!(lond==0&&latd==0))
					outjsondropoff.write(lond, latd);	
			}
			
		});
		outjsonpickup.close();	
		outjsondropoff.close();
		System.out.println("�����ɣ�");
		//----------------��ʱ��--------------------//
		long stopMili=System.currentTimeMillis();// ����ʱ��
		System.out.println("����ʱ�䣺 "+stopMili);
		System.out.println("��ʱ�� "+(stopMili-startMili)/1000.0+"s");
		//----------------��ʱ��--------------------//
	}
}
