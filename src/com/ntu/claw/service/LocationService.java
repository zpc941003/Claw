package com.ntu.claw.service;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ntu.claw.MyApplication;
import com.ntu.claw.activity.MainActivity;
import com.ntu.claw.bean.RecordBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.L;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class LocationService extends Service {

	private static final int UPDATE_TIME = 5000;// 定位时间间隔
	private LocationClient mLocationClient;// 定位核心api，进行定位的一些设置
	private MyLocationListener mLocationListener;// 注册locationClient，定位成功的回调

	Dao dao = null;
	private static final String RECORDINGTAG = "recording";

	boolean isFirstRecord = true;// 是否首次记录
	// 定位模式下设置当前位置
	private LatLng local;
	// 记录模式下设置当前定位位置和上一次定位位置
	private LatLng nowlocal = null; // 当前
	private LatLng lastlocal = null; // 上一次
	private double distance = 0.0; // 总距离

	private String lastLat="";
	private String lastLon="";
	
	private String selfmobile;
	/**
	 * 定位回调接口
	 */
	private OnLocationListener onLocationListener;

	/**
	 * 注册回调接口的方法，供外部调用
	 * 
	 * @param onLocationListner
	 */
	public void setOnLocationListener(OnLocationListener onLocationListner) {
		this.onLocationListener = onLocationListner;
	}

	public class MyBinder extends Binder {
		public LocationService getService() {
			return LocationService.this;
		}
	}

	@Override
	public void onCreate() {
		L.i("service-onCreate");
		dao = new Dao(getApplicationContext());
		selfmobile=SPUtils.get(getApplicationContext(), "mobile", "").toString();
		// 定位初始化设置
		initLocation();
		// 开启定位
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		L.i("service-onDestroy");
		// 停止定位
		mLocationClient.stop();
		MyApplication.getHttpQueues().cancelAll(RECORDINGTAG);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		L.i("service-onBind");
		return new MyBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		L.i("service-onUnbind");
		return super.onUnbind(intent);
	}

	private void initLocation() {
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);// 注册

		// 定位配置
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setIsNeedAltitude(true);
		option.setCoorType("bd09ll");// 坐标类型
		option.setIsNeedAddress(true);// 返回当前位置
		option.setOpenGps(true);
		option.setScanSpan(UPDATE_TIME);// 每隔多少秒一次请求
		mLocationClient.setLocOption(option);
	}

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) { // 定位成功回调，得到location

			final String trace_id = MyApplication.getTrace_id();
			final String longitude = "" + location.getLongitude();
			final String latitude = "" + location.getLatitude();
			final String altitude = "" + location.getAltitude();
			final String locationtime = "" + location.getTime();
			final String attr = location.getAddrStr();
			/**
			 * 开始记录，定位数据上传并存在本地数据库
			 */
			if (MyApplication.isRecordFlag()/*&& location.getRadius()<=60*/) {//过滤精度大的点
				final String rec;
				if(lastLat.equals(latitude)&& lastLon.equals(longitude)){
					rec="N";
				}else{
					rec="Y";
				}
				lastLat = latitude;
				lastLon = longitude;
				String url = MyApplication.ipAddress
						+ "/ClawServer/servlet/LocationServlet";
				StringRequest request = new StringRequest(Method.POST, url,
						new Listener<String>() {

							@Override
							public void onResponse(String arg0) {
								if (arg0.equals("success")) {
									RecordBean record = new RecordBean();
									record.setTrace_id(trace_id);
									record.setLongitude(longitude);
									record.setLatitude(latitude);
									record.setAltitude(altitude);
									record.setLocationtime(locationtime);
									dao.addRecord(record);
								} else {
									//L.i("server error");
								}
							}
						}, new ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError arg0) {
								L.i("connection error");
							}
						}) {
					@Override
					protected Map<String, String> getParams()
							throws AuthFailureError {
						Map<String, String> map = new HashMap<String, String>();
						map.put("action", RECORDINGTAG);
						map.put("trace_id", trace_id);
						map.put("rec", rec);
						map.put("addr", attr);
						map.put("longitude", longitude);
						map.put("latitude", latitude);
						map.put("locationtime", locationtime);
						map.put("altitude", altitude);
						map.put("mobile", selfmobile);
						return map;
					}
				};
				request.setTag(RECORDINGTAG);
				MyApplication.getHttpQueues().add(request);

				local = new LatLng(location.getLatitude(),
						location.getLongitude());
				if (isFirstRecord) {
					lastlocal = local;
					isFirstRecord = false;
				}
				// 当前位置赋予nowlocal
				nowlocal = local;
				// 测距
				double dis = DistanceUtil.getDistance(nowlocal, lastlocal);
				// 计算总距离
				distance += dis;
				MyApplication.setDistance(distance);
				lastlocal = nowlocal;

			}

			// 定位成功写入application，便于activity重新进去时获取实时位置，避免等待后台定位时间
			MyApplication.setLocation(location);
			if (onLocationListener != null) {
				onLocationListener.onLocation(location);// location回调给ui
			}

		}
	}
}
