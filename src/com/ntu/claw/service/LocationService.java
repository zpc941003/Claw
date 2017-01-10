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

	private static final int UPDATE_TIME = 5000;// ��λʱ����
	private LocationClient mLocationClient;// ��λ����api�����ж�λ��һЩ����
	private MyLocationListener mLocationListener;// ע��locationClient����λ�ɹ��Ļص�

	Dao dao = null;
	private static final String RECORDINGTAG = "recording";

	boolean isFirstRecord = true;// �Ƿ��״μ�¼
	// ��λģʽ�����õ�ǰλ��
	private LatLng local;
	// ��¼ģʽ�����õ�ǰ��λλ�ú���һ�ζ�λλ��
	private LatLng nowlocal = null; // ��ǰ
	private LatLng lastlocal = null; // ��һ��
	private double distance = 0.0; // �ܾ���

	private String lastLat="";
	private String lastLon="";
	
	private String selfmobile;
	/**
	 * ��λ�ص��ӿ�
	 */
	private OnLocationListener onLocationListener;

	/**
	 * ע��ص��ӿڵķ��������ⲿ����
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
		// ��λ��ʼ������
		initLocation();
		// ������λ
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		L.i("service-onDestroy");
		// ֹͣ��λ
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
		mLocationClient.registerLocationListener(mLocationListener);// ע��

		// ��λ����
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// ��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
		option.setIsNeedAltitude(true);
		option.setCoorType("bd09ll");// ��������
		option.setIsNeedAddress(true);// ���ص�ǰλ��
		option.setOpenGps(true);
		option.setScanSpan(UPDATE_TIME);// ÿ��������һ������
		mLocationClient.setLocOption(option);
	}

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) { // ��λ�ɹ��ص����õ�location

			final String trace_id = MyApplication.getTrace_id();
			final String longitude = "" + location.getLongitude();
			final String latitude = "" + location.getLatitude();
			final String altitude = "" + location.getAltitude();
			final String locationtime = "" + location.getTime();
			final String attr = location.getAddrStr();
			/**
			 * ��ʼ��¼����λ�����ϴ������ڱ������ݿ�
			 */
			if (MyApplication.isRecordFlag()/*&& location.getRadius()<=60*/) {//���˾��ȴ�ĵ�
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
				// ��ǰλ�ø���nowlocal
				nowlocal = local;
				// ���
				double dis = DistanceUtil.getDistance(nowlocal, lastlocal);
				// �����ܾ���
				distance += dis;
				MyApplication.setDistance(distance);
				lastlocal = nowlocal;

			}

			// ��λ�ɹ�д��application������activity���½�ȥʱ��ȡʵʱλ�ã�����ȴ���̨��λʱ��
			MyApplication.setLocation(location);
			if (onLocationListener != null) {
				onLocationListener.onLocation(location);// location�ص���ui
			}

		}
	}
}
