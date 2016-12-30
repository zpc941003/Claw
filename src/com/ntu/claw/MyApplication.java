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
	 * 未读好友通知
	 */
	public static boolean unReadDot;
	
	/**
	 * 存储上次定位
	 */
	public static BDLocation location=null;
	
	/**
	 * 是否第一次定位
	 */
	public static boolean isFirstIn = true;
	/**
	 * 记录按钮监听
	 */
	private static boolean recordFlag=false;
	
	private static String trace_id = null;
	
	private static double distance = 0.0;
	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
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
