package com.ntu.claw.bean;

public class RecordBean {

	private String trace_id;
//	private String user_id;
	private String longitude;
	private String latitude;
	private String altitude;
	private String locationtime;
	public String getTrace_id() {
		return trace_id;
	}
	public void setTrace_id(String trace_id) {
		this.trace_id = trace_id;
	}
/*	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}*/
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getAltitude() {
		return altitude;
	}
	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}
	public String getLocationtime() {
		return locationtime;
	}
	public void setLocationtime(String locationtime) {
		this.locationtime = locationtime;
	}
	
	
}
