package com.ntu.claw.activity;

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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.bean.RecordBean;
import com.ntu.claw.bean.TraceBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class SelfRecordActivity extends Activity {

	private ListView mListView;
	private List<TraceBean> mData;
	private SelfRecordAdapter mAdapter;
	private LayoutInflater mInflater;

	private String selfMobile;

	private FrameLayout nullRecord;
	private static final String TAG = "clouddownload";
	private static final String DELETETAG = "deletetrace";
	Dao dao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_self_record);
		selfMobile = SPUtils.get(this, "mobile", "").toString();
		nullRecord = (FrameLayout) findViewById(R.id.id_nullrecord);
		dao = new Dao(this);
		initData();
		mListView = (ListView) findViewById(R.id.selfRecordList);
		mInflater = LayoutInflater.from(this);
		mAdapter = new SelfRecordAdapter();
		mListView.setAdapter(mAdapter);
		registerForContextMenu(mListView);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(SelfRecordActivity.this,
						SelfTraceActivity.class);
				intent.putExtra("trace_id", mData.get(position).getTrace_id());
				startActivity(intent);
			}
		});
	}

	private TraceBean toDeleteTrace;
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		toDeleteTrace = mAdapter.getItem(((AdapterContextMenuInfo)menuInfo).position);
		getMenuInflater().inflate(R.menu.context_trace_list, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_trace){
			deleteRecord(toDeleteTrace);
		}
		return super.onContextItemSelected(item);
	}
	
	private void deleteRecord(final TraceBean trace) {
		final ProgressDialog pd = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
		pd.setMessage("删除中...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/LocationServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String result) {
						pd.dismiss();
						if (result.equals("success")) {
							dao.deleteTrace(trace.getTrace_id());
							mData.remove(trace);
							mAdapter.notifyDataSetChanged();
						}
						if(result.equals("fail")){
							T.showShort(SelfRecordActivity.this, "删除失败");
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(SelfRecordActivity.this, "连接失败" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", DELETETAG);
				map.put("trace_id", trace.getTrace_id());
				return map;
			}
		};
		request.setTag(DELETETAG);
		MyApplication.getHttpQueues().add(request);
	}

	public void clouddownload(View v) {
		final ProgressDialog pd = new ProgressDialog(SelfRecordActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("同步中...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/LocationServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						String[] str = arg0.split(";");
						dao.updateAllTraceRecord(selfMobile,
								parseTrace(str[0]), parseRecord(str[1]));
						initData();
						mAdapter.notifyDataSetChanged();
						pd.dismiss();
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(SelfRecordActivity.this,
								"同步失败 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("mobile", selfMobile);
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}

	private void initData() {
		mData = new ArrayList<TraceBean>();
		mData = dao.getTrace(selfMobile);
		if (mData.isEmpty()) {
			nullRecord.setVisibility(View.VISIBLE);
		}
	}

	protected List<RecordBean> parseRecord(String str) {
		List<RecordBean> list = new ArrayList<RecordBean>();
		try {
			JSONArray arr = new JSONArray(str);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject item = arr.getJSONObject(i);
				RecordBean bean = new RecordBean();
				bean.setTrace_id(item.optString("trace_id", null));
				bean.setLongitude(item.optString("longitude", null));
				bean.setAltitude(item.optString("altitude", null));
				bean.setLatitude(item.optString("latitude", null));
				bean.setLocationtime(item.optString("locationtime", null));
				list.add(bean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	protected List<TraceBean> parseTrace(String str) {
		List<TraceBean> list = new ArrayList<TraceBean>();
		try {
			JSONArray arr = new JSONArray(str);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject item = arr.getJSONObject(i);
				TraceBean bean = new TraceBean();
				bean.setTrace_id(item.optString("trace_id",null));
				bean.setUser_id(item.optString("user_id",null));
				bean.setStarttime(item.optString("starttime",null));
				bean.setEndtime(item.optString("endtime",null));
				bean.setDistance(item.optString("distance",null));
				list.add(bean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(TAG);
		MyApplication.getHttpQueues().cancelAll(DELETETAG);
	}

	public void back(View v) {
		finish();
	}

	class SelfRecordAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public TraceBean getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TraceBean bean = mData.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_trace_record,
						parent, false);
				holder.tv_date = (TextView) convertView
						.findViewById(R.id.tx_tracedate);
				holder.tv_time = (TextView) convertView
						.findViewById(R.id.tx_tracetime);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String st = bean.getStarttime();
			String et = bean.getEndtime();
			holder.tv_date.setText(bean.getStarttime().substring(0, 10));
			holder.tv_time.setText(st.substring(st.length() - 8, st.length())
					+ "-" + et.substring(et.length() - 8, et.length()));
			return convertView;
		}

		private class ViewHolder {
			TextView tv_date;
			TextView tv_time;
		}
	}
}
