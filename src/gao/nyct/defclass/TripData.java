package gao.nyct.defclass;

import java.util.Date;
/**
 * 定义出租车旅途信息的泪
 * @author Administrator
 *
 */
public class TripData {
	public TripData(int vendorID, Date tpep_pickup_datetime,
			Date tpep_dropoff_datetime, int passenger_count,
			double trip_distance, double pickup_longitude,
			double pickup_latitude, int ratecodeID, String store_and_fwd_flag,
			double dropoff_longitude, double dropoff_latitude,
			int payment_type, double fare_amount, int extra, double mta_tax,
			double tip_amount, double tolls_amount,
			double improvement_surcharge, double total_amount) {
		this.vendorID = vendorID;
		this.tpep_pickup_datetime = tpep_pickup_datetime;
		this.tpep_dropoff_datetime = tpep_dropoff_datetime;
		this.passenger_count = passenger_count;
		this.trip_distance = trip_distance;
		this.pickup_longitude = pickup_longitude;
		this.pickup_latitude = pickup_latitude;
		this.ratecodeID = ratecodeID;
		this.store_and_fwd_flag = store_and_fwd_flag;
		this.dropoff_longitude = dropoff_longitude;
		this.dropoff_latitude = dropoff_latitude;
		this.payment_type = payment_type;
		this.fare_amount = fare_amount;
		this.extra = extra;
		this.mta_tax = mta_tax;
		this.tip_amount = tip_amount;
		this.tolls_amount = tolls_amount;
		this.improvement_surcharge = improvement_surcharge;
		this.total_amount = total_amount;
	}
	private int vendorID;
	private Date tpep_pickup_datetime;
	private Date tpep_dropoff_datetime;
	private int passenger_count;
	private double trip_distance;
	private double pickup_longitude;
	private double pickup_latitude;
	private int ratecodeID;
	private String store_and_fwd_flag;
	private double dropoff_longitude;
	private double dropoff_latitude;
	private int payment_type;
	private double fare_amount;
	private double extra;
	private double mta_tax;
	private double tip_amount;
	private double tolls_amount;
	private double improvement_surcharge;
	private double total_amount;
	public int getVendorID() {
		return vendorID;
	}
	public Date getTpep_pickup_datetime() {
		return tpep_pickup_datetime;
	}
	public Date getTpep_dropoff_datetime() {
		return tpep_dropoff_datetime;
	}
	public int getPassenger_count() {
		return passenger_count;
	}
	public double getTrip_distance() {
		return trip_distance;
	}
	public double getPickup_longitude() {
		return pickup_longitude;
	}
	public double getPickup_latitude() {
		return pickup_latitude;
	}
	public int getRatecodeID() {
		return ratecodeID;
	}
	public String getStore_and_fwd_flag() {
		return store_and_fwd_flag;
	}
	public double getDropoff_longitude() {
		return dropoff_longitude;
	}
	public double getDropoff_latitude() {
		return dropoff_latitude;
	}
	public int getPayment_type() {
		return payment_type;
	}
	public double getFare_amount() {
		return fare_amount;
	}
	public double getExtra() {
		return extra;
	}
	public double getMta_tax() {
		return mta_tax;
	}
	public double getTip_amount() {
		return tip_amount;
	}
	public double getTolls_amount() {
		return tolls_amount;
	}
	public double getImprovement_surcharge() {
		return improvement_surcharge;
	}
	public double getTotal_amount() {
		return total_amount;
	}
	
}
