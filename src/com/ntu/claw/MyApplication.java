package com.ntu.claw;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;

public class MyApplication extends Application {
	public static RequestQueue queues;
	
	//public static final String ipAddress="http://192.168.191.1:8080";
	public static final String ipAddress="http://115.159.54.152:8080";
	//public static final String ipAddress="http://182.254.245.15:8080";
	
	/**
	 * δ������֪ͨ
	 */
	public static boolean unReadDot;
	
	/**
	 * �洢�ϴζ�λ
	 */
	public static BDLocation location=null;
	
	/**
	 * �Ƿ��һ�ζ�λ
	 */
	public static boolean isFirstIn = true;
	/**
	 * ��¼��ť����
	 */
	private static boolean recordFlag=false;
	
	private static String trace_id = null;
	
	private static double distance = 0.0;
	@Override
	public void onCreate() {
		super.onCreate();
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		queues = Volley.newRequestQueue(getApplicationContext());
	}

	public static RequestQueue getHttpQueues() {
		return queues;
	}

	public static BDLocation getLocation() {
		return location;
	}

	public static void setLocation(BDLocation location) {
		MyApplication.location = location;
	}

	public static boolean isRecordFlag() {
		return recordFlag;
	}

	public static void setRecordFlag(boolean recordFlag) {
		MyApplication.recordFlag = recordFlag;
	}

	public static String getTrace_id() {
		return trace_id;
	}

	public static void setTrace_id(String trace_id) {
		MyApplication.trace_id = trace_id;
	}

	public static double getDistance() {
		return distance;
	}

	public static void setDistance(double distance) {
		MyApplication.distance = distance;
	}
	

}
