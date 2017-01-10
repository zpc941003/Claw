package com.ntu.claw.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.ntu.claw.R;
import com.ntu.claw.bean.TraceBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.MapUtils;
import com.ntu.claw.utils.T;

public class SelfTraceActivity extends Activity {

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private String trace_id;
	private TextView tv_title;
	private TextView tv_starttime;
	private TextView tv_endtime;
	private TextView tv_duration;
	private TextView tv_distance;
	private List<LatLng> mList;
	private BitmapDescriptor mMarker1;
	private BitmapDescriptor mMarker2;
	Dao dao = null;

	private boolean stopTh = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace);
		Intent intent = getIntent();
		trace_id = intent.getStringExtra("trace_id");
		dao = new Dao(this);
		initView();

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!stopTh) {
					runOnUiThread(new Runnable() {
						public void run() {
							initData();
							initMap();
						}
					});
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("a");
		stopTh = true;
	}

	private void initMap() {
		if (mList.isEmpty()) {
			T.showShort(this, "没有数据");
		} else {
			MapStatus mMapStatus = new MapStatus.Builder().zoom(18.0f)
					.target(MapUtils.getTraceCenter(mList)).build();// 设置缩放比例
			MapStatusUpdate msu = MapStatusUpdateFactory
					.newMapStatus(mMapStatus);
			mBaiduMap.setMapStatus(msu);
			if (mList.size() == 1) {
				OverlayOptions option = new MarkerOptions()
						.position(mList.get(mList.size() - 1)).icon(mMarker2)
						.zIndex(5);
				mBaiduMap.addOverlay(option);
			} else {
				OverlayOptions polyline = new PolylineOptions().width(12)
						.color(0xFF1296DB).points(mList);
				mBaiduMap.addOverlay(polyline);
				OverlayOptions option1 = new MarkerOptions()
						.position(mList.get(0)).icon(mMarker1).zIndex(5);
				OverlayOptions option2 = new MarkerOptions()
						.position(mList.get(mList.size() - 1)).icon(mMarker2)
						.zIndex(6);
				mBaiduMap.addOverlay(option1);
				mBaiduMap.addOverlay(option2);
				mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
					@Override
					public void onMapLoaded() {
						LatLngBounds.Builder builder = new LatLngBounds.Builder();
						for (LatLng p : mList) {
							builder = builder.include(p);
						}
						LatLngBounds latlngBounds = builder.build();
						MapStatusUpdate u = MapStatusUpdateFactory
								.newLatLngBounds(latlngBounds,
										mMapView.getWidth(),
										mMapView.getHeight());
						mBaiduMap.animateMapStatus(u);
					}
				});
			}
		}
	}

	private void initData() {
		TraceBean trace = dao.getOneTrace(trace_id);
		mList = dao.getTraceRecord(trace_id);
		tv_title.setText(trace.getStarttime().substring(0, 10));
		tv_starttime.setText(trace.getStarttime().substring(11));
		String dis = trace.getDistance();
		if (dis == null || dis == "") {
			dis = "" + (int) MapUtils.getDistance(mList);
		}
		tv_distance.setText(dis + "M");
		if (trace.getEndtime() != null && trace.getEndtime() != "") {
			tv_endtime.setText(trace.getEndtime().substring(11));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				long l = df.parse(trace.getEndtime()).getTime()
						- df.parse(trace.getStarttime()).getTime();
				long h = l / (60 * 60 * 1000);
				long m = l / (60 * 1000) - h * 60;
				long s = l / 1000 - h * 60 * 60 - m * 60;
				tv_duration.setText(h + "h" + m + "m" + s + "s");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public List<LatLng> optimizePoints(List<LatLng> inPoint) {
		int size = inPoint.size();
		if (size < 5) {
			return inPoint;
		} else {
			// Latitude
			inPoint.set(
					0,
					new LatLng(
							((3.0 * inPoint.get(0).latitude + 2.0
									* inPoint.get(1).latitude
									+ inPoint.get(2).latitude - inPoint.get(4).latitude) / 5.0),
							((3.0 * inPoint.get(0).longitude + 2.0
									* inPoint.get(1).longitude
									+ inPoint.get(2).longitude - inPoint.get(4).longitude) / 5.0)));
			inPoint.set(
					1,
					new LatLng(
							((4.0 * inPoint.get(0).latitude + 3.0
									* inPoint.get(1).latitude + 2
									* inPoint.get(2).latitude + inPoint.get(3).latitude) / 10.0),
							((4.0 * inPoint.get(0).longitude + 3.0
									* inPoint.get(1).longitude + 2
									* inPoint.get(2).longitude + inPoint.get(3).longitude) / 10.0)));

			inPoint.set(
					size - 2,
					new LatLng((4.0 * inPoint.get(size - 1).latitude + 3.0
							* inPoint.get(size - 2).latitude + 2
							* inPoint.get(size - 3).latitude + inPoint
							.get(size - 4).latitude) / 10.0, (4.0
							* inPoint.get(size - 1).longitude + 3.0
							* inPoint.get(size - 2).longitude + 2
							* inPoint.get(size - 3).longitude + inPoint
							.get(size - 4).longitude) / 10.0));
			inPoint.set(
					size - 1,
					new LatLng((3.0 * inPoint.get(size - 1).latitude + 2.0
							* inPoint.get(size - 2).latitude
							+ inPoint.get(size - 3).latitude - inPoint
							.get(size - 5).latitude) / 5.0, (3.0
							* inPoint.get(size - 1).longitude + 2.0
							* inPoint.get(size - 2).longitude
							+ inPoint.get(size - 3).longitude - inPoint
							.get(size - 5).longitude) / 5.0));
		}
		return inPoint;
	}

	private void initView() {
		mMapView = (MapView) findViewById(R.id.bmapRecord);
		mBaiduMap = mMapView.getMap();
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_starttime = (TextView) findViewById(R.id.tv_starttime);
		tv_endtime = (TextView) findViewById(R.id.tv_endtime);
		tv_duration = (TextView) findViewById(R.id.tv_duration);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		mMarker1 = BitmapDescriptorFactory.fromResource(R.drawable.startpoint);
		mMarker2 = BitmapDescriptorFactory.fromResource(R.drawable.endpoint);
	}

	public void back(View v) {
		finish();
	}

}
