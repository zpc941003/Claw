package com.ntu.claw.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.utils.MapUtils;
import com.ntu.claw.utils.T;

public class FriTraceActivity extends Activity {

	private static final String TAG = "getrecord";
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private String trace_id;
	private TextView tv_title;
	private TextView tv_starttime;
	private TextView tv_endtime;
	private TextView tv_duration;
	private TextView tv_distance;
	private List<LatLng> mList=new ArrayList<LatLng>();
	private BitmapDescriptor mMarker1;
	private BitmapDescriptor mMarker2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace);
		Intent intent = getIntent();
		initView(intent);
		initData();
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
				OverlayOptions polyline = new PolylineOptions().width(15)
						.color(0xAA7646D5).points(mList);
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
		final ProgressDialog pd = new ProgressDialog(FriTraceActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("加载中...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/LocationServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						parse(arg0);
						initMap();
						pd.dismiss();
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(FriTraceActivity.this,
								"加载失败 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("trace_id", trace_id);
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}

	private void parse(String arg0) {
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
	}

	private void initView(Intent intent) {
		mMapView = (MapView) findViewById(R.id.bmapRecord);
		mBaiduMap = mMapView.getMap();
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_starttime = (TextView) findViewById(R.id.tv_starttime);
		tv_endtime = (TextView) findViewById(R.id.tv_endtime);
		tv_duration = (TextView) findViewById(R.id.tv_duration);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		mMarker1 = BitmapDescriptorFactory.fromResource(R.drawable.startpoint);
		mMarker2 = BitmapDescriptorFactory.fromResource(R.drawable.endpoint);
		trace_id = intent.getStringExtra("trace_id");
		tv_title.setText(intent.getStringExtra("st").substring(0, 10));
		tv_starttime.setText(intent.getStringExtra("st").substring(11));
		tv_endtime.setText(intent.getStringExtra("et").substring(11));
		tv_distance.setText(intent.getStringExtra("dis") + "M");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			long l = df.parse(intent.getStringExtra("et")).getTime()
					- df.parse(intent.getStringExtra("st")).getTime();
			long h = l / (60 * 60 * 1000);
			long m = l / (60 * 1000) - h * 60;
			long s = l / 1000 - h * 60 * 60 - m * 60;
			tv_duration.setText(h + "h" + m + "m" + s + "s");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void back(View v) {
		finish();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(TAG);
	}
}
