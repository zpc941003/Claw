package com.ntu.claw.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.android.data.JPushLocalNotification;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.cache.DiskLruCacheHelper;
import com.ntu.claw.db.Dao;
import com.ntu.claw.receiver.ObservableObject;
import com.ntu.claw.service.LocationService;
import com.ntu.claw.service.OnLocationListener;
import com.ntu.claw.utils.ImgUtils;
import com.ntu.claw.utils.L;
import com.ntu.claw.utils.NetUtils;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;
import com.ntu.claw.widget.CircleButton;

public class MainActivity extends Activity implements OnClickListener, Observer {

	LocationService mLocationservice;
	Intent serviceIntent;

	private MapView mMapView;
	private BaiduMap mBaiduMap;

	private double mLatitude;// γ��
	private double mLongtitude;// ����

	private BitmapDescriptor mMarker;

	// ͼƬ��Դ
	private int[] res = { R.id.id_menu, R.id.id_info, R.id.id_maplayer,
			R.id.id_record, R.id.id_friendlocation, R.id.id_mylocation };
	// ���ImageView
	private List<ImageView> imageViewList = new ArrayList<ImageView>();
	// ���ǲ˵��ǲ���չ��
	private boolean isNotExpand = true;

	private RelativeLayout rl; // �ײ�layout
	private FrameLayout fl; // ���ֲ�
	private Animation myAnimationTranslate;// �ײ��������ϻ����˵�����
	private AlphaAnimation myAlphaAnimation;// ���ֽ��䶯��
	private CircleButton cb;// �ײ���¼Բ�ΰ�ť
	private TextView startTime;
	private TextView endTime;
	private TextView distanceView;
	private Handler handler;// ����ʱ��

	boolean isFirstRecord = true;// �Ƿ��״μ�¼
	// ��λģʽ�����õ�ǰλ��
	private LatLng local;
	// ��¼ģʽ�����õ�ǰ��λλ�ú���һ�ζ�λλ��
	private LatLng nowlocal = null; // ��ǰ
	private LatLng lastlocal = null; // ��һ��

	private static final int TIMEMESSAGE = 0;

	private static final String STARTRECORDTAG = "startrecord";
	private static final String ENDRECORDTAG = "endrecord";
	private static final String TAG = "receivenotify";
	private static final String TAG1="currenttrace";

	Dao dao = null;

	private Button bottomMapBtn;
	private Button bottomContactBtn;
	private Button bottomSettingBtn;

	private String selfmobile;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
		ObservableObject.getInstance().addObserver(this);
		// ��ȡ�Լ��ֻ���
		dao = new Dao(this);
		selfmobile = (String) SPUtils.get(this, "mobile", "");
		setAlias();
		bottomMapBtn = (Button) findViewById(R.id.btn_map);
		bottomContactBtn = (Button) findViewById(R.id.btn_contact);
		bottomSettingBtn = (Button) findViewById(R.id.btn_setting);
		bottomMapBtn.setSelected(true);
		receiveNotify();
		initClickEvent();
		initView();
		clickListen();// �������

		// ������̨��λ����
		serviceIntent = new Intent(MainActivity.this, LocationService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mConnection, Service.BIND_AUTO_CREATE);

		disposeText();
		disposeDistance();
		disposeTrace();
		disposeTime();

		initMarkerClickEvent();
		initMapClickEvent();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		// ������Դ��service���������ⶪʧ����ã�����service������ǿ��ɱ��
		public void onServiceDisconnected(ComponentName arg0) {
			mLocationservice = null;
		}

		@Override
		// ������Դ��service�ɹ����ӻ����
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			getFirstLocation();
			mLocationservice = ((LocationService.MyBinder) binder).getService();
			getLocation();
		}
	};

	/**
	 * ������activity�л���mainactivityʱ�� ��application�л�ȡ�ϴκ�̨��λ�����ݲ����ø���ͼ ʹ��λ��ʱ��ʾ���Ż�����
	 */
	private void getFirstLocation() {
		if (MyApplication.getLocation() != null) {
			MyLocationData data = new MyLocationData.Builder()// builderģʽ
					.accuracy(MyApplication.getLocation().getRadius())// ����
					.latitude(MyApplication.getLocation().getLatitude())// ά��
					.longitude(MyApplication.getLocation().getLongitude())// ����
					.build();
			// System.out.println("γ�ȣ�"+MyApplication.getLocation().getLatitude()+"  ���ȣ�"+MyApplication.getLocation().getLongitude()+" �߶�:"+MyApplication.getLocation().getAltitude()+" �ٶ�"+MyApplication.location.hasSpeed());
			mBaiduMap.setMyLocationData(data);// ��λ�����ø���ͼ,�ѵ����ø���ͼ
			LatLng latlng = new LatLng(MyApplication.getLocation()
					.getLatitude(), MyApplication.getLocation().getLongitude());
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
			mBaiduMap.animateMapStatus(msu);// ����������λ��
			T.showShort(MainActivity.this, MyApplication.getLocation()
					.getAddrStr());
		}
	}

	private void getLocation() {
		mLocationservice.setOnLocationListener(new OnLocationListener() {

			@Override
			public void onLocation(BDLocation location) {
				MyLocationData data = new MyLocationData.Builder()// builderģʽ
						.accuracy(location.getRadius())// ����
						.latitude(location.getLatitude())// ά��
						.longitude(location.getLongitude())// ����
						.build();
				mBaiduMap.setMyLocationData(data);// ��λ�����ø���ͼ,�ѵ����ø���ͼ
				mLatitude = location.getLatitude();
				mLongtitude = location.getLongitude();
				local = new LatLng(location.getLatitude(), location
						.getLongitude());
				// System.out.println("���ȣ�"+location.getRadius()+"γ�ȣ�"+mLatitude+"  ���ȣ�"+mLongtitude+" �߶�:"+location.getAltitude()+" ʱ��"+location.getTime());
				// T.showShort(MainActivity.this,
				// "����:"+location.getRadius()+" ����:"+location.getDirection()+" �ٶ�:"+location.getSpeed());
				if (MyApplication.isFirstIn) {
					LatLng latlng = new LatLng(location.getLatitude(), location
							.getLongitude());
					MapStatusUpdate msu = MapStatusUpdateFactory
							.newLatLng(latlng);
					mBaiduMap.animateMapStatus(msu);// ����������λ��
					MyApplication.isFirstIn = false;
					T.showShort(MainActivity.this, location.getAddrStr());
				}
				if (MyApplication.isRecordFlag() && location.getRadius() <= 60) {// ��ʼ��¼�켣
					double distance = MyApplication.getDistance();
					distanceView.setText((int) distance + "");
					if (isFirstRecord) {
						lastlocal = local;
						isFirstRecord = false;
					}
					// ��ǰλ�ø���nowlocal
					nowlocal = local;
					// ����
					drawTrace();
					lastlocal = nowlocal;
				}
			}
		});
	}

	private void initView() {// ��һ����ʾ��ͼ����
		for (int i = 0; i < res.length; i++) {
			ImageView imageView = (ImageView) findViewById(res[i]);
			// �����list��
			imageViewList.add(imageView);
		}
		cb = (CircleButton) findViewById(R.id.recordbtn);
		rl = (RelativeLayout) findViewById(R.id.bottomlayout);
		fl = (FrameLayout) findViewById(R.id.shadelayout);
		startTime = (TextView) findViewById(R.id.starttime);
		endTime = (TextView) findViewById(R.id.endtime);
		distanceView = (TextView) findViewById(R.id.id_distance);
		/**
		 * ����Բ�ΰ�ť����
		 */
		// cb.set_text("��ʼ��¼");
		cb.set_text_color(Color.WHITE);
		cb.set_bg_color(Color.rgb(105, 190, 145));
		cb.set_text_size_dp(20);
		cb.set_pressed_ring_width(20);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);// �������ű���
		mBaiduMap.setMapStatus(msu);
	}

	private void initMarker(String mobile) {
		String avaterUrl = dao.getAvaterUrl(mobile);
		Bitmap bitmap = DiskLruCacheHelper.getInstance(MainActivity.this)
				.readFromCache(avaterUrl);
		if (bitmap != null && avaterUrl != null & avaterUrl != "") {
			mMarker = BitmapDescriptorFactory
					.fromBitmap(ImgUtils.zoomImg(
							ImgUtils.getRoundeBitmapWithWhite(ImgUtils
									.toRound(bitmap)), 100, 100));
		} else {
			mMarker = BitmapDescriptorFactory.fromBitmap(ImgUtils.zoomImg(
					ImgUtils.getRoundeBitmapWithWhite(BitmapFactory
							.decodeResource(this.getResources(),
									R.drawable.defaultmarker)), 100, 100));
		}
	}

	List<Marker> mListMarker = new ArrayList<Marker>();// �洢marker

	private void addOverlays(LatLng latlng, String addr, String name,
			String mobile) {
		boolean flag = false;
		for (Marker mar : mListMarker) {
			if (mar.getExtraInfo().get("mobile").equals(mobile)) {
				flag = true;
				mar.setPosition(latlng);
				Bundle bundle = new Bundle();
				bundle.putSerializable("addr", addr);
				bundle.putSerializable("mobile", mobile);
				bundle.putSerializable("name", name);
				mar.setExtraInfo(bundle);
				break;
			}
		}
		if (!flag) {
			initMarker(mobile);
			OverlayOptions options = new MarkerOptions().position(latlng)
					.icon(mMarker).zIndex(5);
			Marker marker = (Marker) mBaiduMap.addOverlay(options);
			Bundle bundle = new Bundle();
			bundle.putSerializable("addr", addr);
			bundle.putSerializable("mobile", mobile);
			bundle.putSerializable("name", name);
			marker.setExtraInfo(bundle);
			mListMarker.add(marker);
		}
		// addInfoWindow();
	}

	@Override
	public void update(Observable observable, Object data) {
		String[] str = data.toString().split(";");
		System.out.println(data.toString());
		if (str[0].equals("N")) {
			for (Marker mar : mListMarker) {
				if (mar.getExtraInfo().get("mobile").equals(str[1])) {
					mar.remove();
					mListMarker.remove(mar);
					break;
				}
			}
		} else {
			addOverlays(
					new LatLng(Double.parseDouble(str[1]),
							Double.parseDouble(str[0])), str[2], str[3], str[4]);
		}
	}

	private void initMarkerClickEvent() {
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				addInfoWindow(marker.getPosition(),
						marker.getExtraInfo().get("addr").toString(), marker
								.getExtraInfo().get("name").toString(), marker
								.getExtraInfo().get("mobile").toString());
				return true;
			}
		});
	}

	private void initMapClickEvent() {
		mBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent arg0) {
				mBaiduMap.hideInfoWindow();
				if (isNotExpand == false) {
					closeAnim();
				}
			}
		});
	}

	private void addInfoWindow(final LatLng ll, String addr, String name,
			final String mobile) {
		InfoWindow mInfoWindow;
		View infoWindow = LayoutInflater.from(MainActivity.this).inflate(
				R.layout.view_infowindow, null);
		TextView tvName = (TextView) infoWindow.findViewById(R.id.fri_name);
		tvName.setText(name);
		TextView tvAddr = (TextView) infoWindow.findViewById(R.id.fri_addr);
		tvAddr.setText("��ǰλ�ã�" + addr);
		mInfoWindow = new InfoWindow(infoWindow, ll, -55);
		mBaiduMap.showInfoWindow(mInfoWindow);
		LinearLayout call = (LinearLayout) infoWindow
				.findViewById(R.id.call_LL);
		LinearLayout trace = (LinearLayout) infoWindow
				.findViewById(R.id.navigation_LL);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent phoneIntent = new Intent("android.intent.action.CALL",
						Uri.parse("tel:" + mobile));
				startActivity(phoneIntent);
			}
		});
		trace.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getTrace(mobile,ll);
			}
		});
	}

	Overlay mOverlay=null;
	protected void getTrace(final String mobile, final LatLng ll) {
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/LocationServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						System.out.println(arg0);
						List<LatLng> mList=new ArrayList<LatLng>();
						try {
							JSONArray arr = new JSONArray(arg0);
							for (int i = 0; i < arr.length(); i++) {
								JSONObject item = arr.getJSONObject(i);
								LatLng latlng = new LatLng(Double.parseDouble(item.optString(
										"latitude", null)), Double.parseDouble(item.optString(
										"longitude", null)));
								mList.add(latlng);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						if(mList.size()>1){
							if(mOverlay!=null){
								mOverlay.remove();
							}
							OverlayOptions polyline = new PolylineOptions().width(12)
									.color(0xFF7646D5).points(mList);
							mOverlay=mBaiduMap.addOverlay(polyline);
						}
						LatLngBounds.Builder builder = new LatLngBounds.Builder();
						for (LatLng p : mList) {
							builder = builder.include(p);
						}
						builder = builder.include(ll);
						LatLngBounds latlngBounds = builder.build();
						MapStatusUpdate u = MapStatusUpdateFactory
								.newLatLngBounds(latlngBounds,
										mMapView.getWidth(),
										mMapView.getHeight());
						mBaiduMap.animateMapStatus(u);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						T.showShort(mContext, "����ʧ�� " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG1);
				map.put("mobile", mobile);
				return map;
			}
		};
		request.setTag(TAG1);
		MyApplication.getHttpQueues().add(request);
	}

	private void clickListen() {
		fl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				closeBottomLayout();
			}
		});
		cb.setOnClickListener(new OnClickListener() {// ��ʼ��¼
			@Override
			public void onClick(View arg0) {
				if (!MyApplication.isRecordFlag()) {
					Date date = new Date();
					SimpleDateFormat df1 = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
					final String st1 = df1.format(date);// ��ʼ��¼ʱ��
					final String st2 = df2.format(date);
					String url = MyApplication.ipAddress
							+ "/ClawServer/servlet/LocationServlet";
					StringRequest request = new StringRequest(Method.POST, url,
							new Listener<String>() {

								@Override
								public void onResponse(String arg0) {// ��ʼ��¼����������trace_id
									if (arg0.equals("fail")) {
										T.showShort(MainActivity.this, "����������");
									} else {
										startTime.setText(st2);
										dao.addTrace(arg0, selfmobile, st1);
										MyApplication.setTrace_id(arg0);
										MyApplication.setRecordFlag(true);
										cb.set_text("������¼");
										T.showShort(MainActivity.this, "��ʼ��¼");
										new TimeThread().start();
										// �����߳����洦����Ϣ������UI����
										handler = new Handler() {
											@SuppressLint("HandlerLeak")
											@Override
											public void handleMessage(
													Message msg) {
												super.handleMessage(msg);
												switch (msg.what) {
												case TIMEMESSAGE:
													Date date = new Date();
													SimpleDateFormat df = new SimpleDateFormat(
															"HH:mm:ss");
													endTime.setText(df
															.format(date));
													break;
												default:
													break;
												}
											}
										};
									}
								}
							}, new ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError arg0) {
									T.showShort(MainActivity.this, "����ʧ�� "
											+ arg0.toString());
								}
							}) {
						@Override
						protected Map<String, String> getParams()
								throws AuthFailureError {
							Map<String, String> map = new HashMap<String, String>();
							map.put("action", STARTRECORDTAG);
							map.put("mobile", selfmobile);
							map.put("starttime", st1);
							return map;
						}
					};
					request.setTag(STARTRECORDTAG);
					MyApplication.getHttpQueues().add(request);
				} else {
					/**
					 * ������¼
					 */

					final String trace_id = MyApplication.getTrace_id();
					final String distance = distanceView.getText().toString();
					Date date = new Date();
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					final String et = df.format(date);
					MyApplication.setRecordFlag(false);
					MyApplication.setTrace_id(null);
					MyApplication.setDistance(0.0);
					dao.endRecord(trace_id, et, distance);

					cb.set_text("��ʼ��¼");
					T.showShort(MainActivity.this, "record end");

					String url = MyApplication.ipAddress
							+ "/ClawServer/servlet/LocationServlet";
					StringRequest request = new StringRequest(Method.POST, url,
							new Listener<String>() {

								@Override
								public void onResponse(String arg0) {
									if (arg0.equals("fail")) {
										T.showShort(MainActivity.this, "����������");
									} else {
										L.i("end record success");
									}
								}
							}, new ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError arg0) {
									T.showShort(MainActivity.this, "����ʧ�� "
											+ arg0.toString());
								}
							}) {
						@Override
						protected Map<String, String> getParams()
								throws AuthFailureError {
							Map<String, String> map = new HashMap<String, String>();
							map.put("action", ENDRECORDTAG);
							map.put("trace_id", trace_id);
							map.put("endtime", et);
							map.put("distance", distance);
							map.put("mobile", selfmobile);
							return map;
						}
					};
					request.setTag(ENDRECORDTAG);
					MyApplication.getHttpQueues().add(request);
				}
			}
		});
	}

	private void disposeDistance() {
		if (MyApplication.isRecordFlag()) {
			double distance = MyApplication.getDistance();
			distanceView.setText((int) distance + "");
		}
	}

	/**
	 * ���½���ҳ����ؾ���
	 */
	private void disposeText() {
		if (MyApplication.isRecordFlag()) {
			cb.set_text("������¼");
		} else {
			cb.set_text("��ʼ��¼");
		}
	}

	/**
	 * ���½���ҳ������Ѿ����صĹ켣
	 */
	private void disposeTrace() {
		if (MyApplication.isRecordFlag()) {
			List<LatLng> points = new ArrayList<LatLng>();
			points = dao.getRecording(MyApplication.getTrace_id());
			System.out.println("point:" + points.size());
			if (points.size() >= 2) {
				OverlayOptions polyline = new PolylineOptions().width(12)
						.color(0xFF1296DB).points(points);
				mBaiduMap.addOverlay(polyline);
			}
		}
	}

	/**
	 * ���½���ҳ������Ѿ�����ʱ��
	 */
	private void disposeTime() {
		if (MyApplication.isRecordFlag()) {

			String st = dao.getStarttime(MyApplication.getTrace_id());
			startTime.setText(st.substring(st.length() - 8, st.length()));

			new TimeThread().start();
			// �����߳����洦����Ϣ������UI����
			handler = new Handler() {
				@SuppressLint("HandlerLeak")
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					switch (msg.what) {
					case TIMEMESSAGE:
						Date date = new Date();
						SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
						endTime.setText(df.format(date));
						break;
					default:
						break;
					}
				}
			};

		}
	}

	// ʱ���߳�
	class TimeThread extends Thread {
		@Override
		public void run() {
			do {
				if (!MyApplication.isRecordFlag()) {
					break;
				} else {
					try {
						Thread.sleep(1000);
						Message msg = new Message();
						msg.what = TIMEMESSAGE; // ��Ϣ(һ������ֵ)
						handler.sendMessage(msg);// ÿ��1�뷢��һ��msg��handler
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (true);
		}
	}

	private void drawTrace() {
		// ����
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(nowlocal);
		points.add(lastlocal);
		OverlayOptions polyline = new PolylineOptions().width(12)
				.color(0xFF1296DB).points(points);
		mBaiduMap.addOverlay(polyline);
	}

	private void centerToMyLocation() {// �ػص�ǰ��λ
		LatLng latlng = new LatLng(mLatitude, mLongtitude);// ���þ�γ��
		MapStatus mMapStatus = new MapStatus.Builder().target(latlng).zoom(18)
				.build();
		// ����MapStatusUpdate�����Ա�������ͼ״̬��Ҫ�����ı仯
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// �ı��ͼ״̬
		mBaiduMap.animateMapStatus(mMapStatusUpdate);// ����������λ��
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// ������λ
		mBaiduMap.setMyLocationEnabled(true);// ��ͼ����λ
		/*
		 * if (!mLocationClient.isStarted()) { mLocationClient.start(); }
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// ֹͣ��λ
		mBaiduMap.setMyLocationEnabled(false);
		MyApplication.getHttpQueues().cancelAll(STARTRECORDTAG);
		MyApplication.getHttpQueues().cancelAll(ENDRECORDTAG);
		MyApplication.getHttpQueues().cancelAll(TAG);
		MyApplication.getHttpQueues().cancelAll(TAG1);
		// mLocationClient.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();
	}

	public void clickview(View view) {
		switch (view.getId()) {
		// ���˵������
		case R.id.id_menu:
			// ���˵�û��չ��ʱ�����
			if (isNotExpand == true) {
				// ��������
				startAnim();
			} else {
				// �رն���
				closeAnim();
			}
			break;
		// ����������������ʱ�������¼�
		case R.id.id_mylocation:
			centerToMyLocation();
			break;
		case R.id.id_maplayer:
			if (mBaiduMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL) {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			} else {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			}
			break;
		case R.id.id_record:
			startBottomLayout();
			closeAnim();
			break;
		case R.id.id_info:
			closeAnim();
			startActivity(new Intent(this, SelfRecordActivity.class));
			break;
		case R.id.id_friendlocation:
			if (mListMarker.size() != 0) {
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				for (Marker mar : mListMarker) {
					builder = builder.include(mar.getPosition());
				}
				builder.include(local);
				LatLngBounds latlngBounds = builder.build();
				MapStatusUpdate u = MapStatusUpdateFactory
						.newLatLngBounds(latlngBounds, mMapView.getWidth(),
								mMapView.getHeight());
				mBaiduMap.animateMapStatus(u);
			} else {
				T.showShort(this, "��ǰû�к����ڼ�¼");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * ���������ײ�layout
	 */
	private void startBottomLayout() {
		rl.setVisibility(View.VISIBLE);
		myAnimationTranslate = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				0, Animation.RELATIVE_TO_PARENT, 1,
				Animation.RELATIVE_TO_PARENT, 0);
		myAnimationTranslate.setDuration(500);
		myAnimationTranslate.setInterpolator(AnimationUtils.loadInterpolator(
				MainActivity.this,
				android.R.anim.accelerate_decelerate_interpolator));
		rl.startAnimation(myAnimationTranslate);
		myAnimationTranslate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				myAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
				myAlphaAnimation.setDuration(500);
				fl.setAnimation(myAlphaAnimation);
				fl.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
			}
		});
	}

	/**
	 * �����رյײ�layout
	 */
	private void closeBottomLayout() {
		rl.setVisibility(View.GONE);
		myAnimationTranslate = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				0, Animation.RELATIVE_TO_PARENT, 0,
				Animation.RELATIVE_TO_PARENT, 1);
		myAnimationTranslate.setDuration(500);
		myAnimationTranslate.setInterpolator(AnimationUtils.loadInterpolator(
				MainActivity.this,
				android.R.anim.accelerate_decelerate_interpolator));
		rl.startAnimation(myAnimationTranslate);
		myAnimationTranslate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				myAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
				myAlphaAnimation.setDuration(500);
				fl.setAnimation(myAlphaAnimation);
				fl.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
			}
		});
	}

	// �����ر����ǲ˵�
	private void closeAnim() {
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		for (int i = 1; i < res.length; i++) {
			float angle = (180 * 1.0f / (res.length - 2)) * (i - 1) + 90;
			PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat(
					"translationX",
					(float) (Math.sin((angle * 1.57 / 90)) * 0.25 * width), 0);
			PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat(
					"translationY",
					(float) (Math.cos((angle * 1.57 / 90)) * 0.25 * width), 0);
			ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
					imageViewList.get(i), holder1, holder2);
			// ObjectAnimator animator =
			// ObjectAnimator.ofFloat(imageViewList.get(i), "translationY", i *
			// 60,
			// 0);
			animator.setDuration(300);
			animator.start();
			isNotExpand = true;

		}
	}

	// �����������ǲ˵�
	private void startAnim() {
		// ��ȡ��Ļ���
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		// ������һ���������˵���ImageView�б�
		for (int i = 1; i < res.length; i++) {
			// ��ȡչ���Ƕ�
			float angle = (180 * 1.0f / (res.length - 2)) * (i - 1) + 90;
			// ��ȡXλ��
			PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat(
					"translationX", 0,
					(float) (Math.sin((angle * 1.57 / 90)) * 0.25 * width));
			// ��ȡYλ��
			PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat(
					"translationY", 0,
					(float) (Math.cos((angle * 1.57 / 90)) * 0.25 * width));
			// ����ImageView�����Զ���
			ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
					imageViewList.get(i), holder1, holder2);
			// ObjectAnimator animator =
			// ObjectAnimator.ofFloat(imageViewList.get(i), "translationY", 0, i
			// *
			// 60);
			// ����ʱ��
			animator.setDuration(800);
			// �����ӳ�ʱ��
			animator.setFrameDelay(500 * i);
			// ���ü�����
			animator.setInterpolator(new BounceInterpolator());
			// ��������
			animator.start();
			isNotExpand = false;
		}
	}

	private void initClickEvent() {
		bottomContactBtn.setOnClickListener(this);
		bottomSettingBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_contact:
			startActivity(new Intent(MainActivity.this, ContactActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		case R.id.btn_setting:
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		default:
			break;
		}
	}

	/**
	 * // ���ñ��������óɹ�ʱ���� SharePreference ��д״̬���Ժ󲻱�������
	 */
	private void setAlias() {
		if (SPUtils.get(this, "alias", "1").equals(selfmobile)) {
			L.i("have set alias");
			return;
		} else {
			L.i("haven't set alias");
			// �����Լ����ֻ���Ϊ����
			mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
					selfmobile));
		}
	}

	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
		@Override
		public void gotResult(int code, String alias, Set<String> tags) {
			String logs;
			switch (code) {
			case 0:
				SPUtils.put(MainActivity.this, "alias", selfmobile);
				logs = "Set tag and alias success";
				L.i(logs);
				break;
			case 6002:
				logs = "Failed to set alias and tags due to timeout. Try again after 10s.";
				L.i(logs);
				if (NetUtils.isConnected(getApplicationContext())) {
					mHandler.sendMessageDelayed(
							mHandler.obtainMessage(MSG_SET_ALIAS, alias),
							1000 * 10);
				} else {
					logs = "No network";
					L.i(logs);
				}
				break;
			default:
				logs = "Failed with errorCode = " + code;
				L.e(logs);
			}
			T.showShort(MainActivity.this, logs);
		}
	};

	private static final int MSG_SET_ALIAS = 1001;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SET_ALIAS:
				L.d("Set alias in handler.");
				JPushInterface.setAliasAndTags(getApplicationContext(),
						(String) msg.obj, null, mAliasCallback);
				break;
			default:
				L.i("Unhandled msg - " + msg.what);
			}
		}
	};

	private void receiveNotify() {
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						System.out.println(arg0);
						if (!arg0.equals("no")) {
							try {
								JSONArray arr = new JSONArray(arg0);
								for (int i = 0; i < arr.length(); i++) {
									JSONObject item = arr.getJSONObject(i);
									String mobile = item
											.optString("mobile", "");
									String username = item.optString(
											"username", "");
									String avater = item
											.optString("avater", "");
									JPushLocalNotification ln = new JPushLocalNotification();
									ln.setContent("�����µĺ��ѹ�ע����");
									ln.setTitle("Claw");
									ln.setNotificationId(System
											.currentTimeMillis());
									Map<String, Object> map = new HashMap<String, Object>();
									map.put("mobile", mobile);
									map.put("username", username);
									map.put("avater", avater);
									JSONObject json = new JSONObject(map);
									ln.setExtras(json.toString());
									JPushInterface.addLocalNotification(
											getApplicationContext(), ln);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						T.showShort(mContext, "����ʧ�� " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("mobile", selfmobile);
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}

}
