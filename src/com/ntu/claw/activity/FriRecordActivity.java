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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
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
import com.ntu.claw.bean.TraceBean;
import com.ntu.claw.utils.L;
import com.ntu.claw.utils.T;

public class FriRecordActivity extends Activity implements OnScrollListener {

	private String mobile;
	private TextView tv_title;
	private List<TraceBean> mData;
	private LayoutInflater mInflater;
	private ListView mListView;
	private FriRecordAdapter mAdapter;
	private View layoutLoadMore;
	private FrameLayout nullRecord;
	private int offset;
	private static final int limit = 10;

	private boolean isLoading;

	private static final String TAG = "gettrace";

	int firstVisibleItem;
	int totalItemCount;// 总数量
	int lastVisibieItem;// 最后一个可见的item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fri_record);
		init();
		Intent intent = getIntent();
		tv_title.setText(intent.getStringExtra("username") + "的记录");
		mobile = intent.getStringExtra("mobile");
		loadData();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent=new Intent(FriRecordActivity.this,FriTraceActivity.class);
				intent.putExtra("trace_id", mData.get(position).getTrace_id());
				intent.putExtra("st", mData.get(position).getStarttime());
				intent.putExtra("et", mData.get(position).getEndtime());
				intent.putExtra("dis", mData.get(position).getDistance());
				startActivity(intent);
			}
		});
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem=firstVisibleItem;
		this.lastVisibieItem = firstVisibleItem + visibleItemCount;
		this.totalItemCount = totalItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (totalItemCount == lastVisibieItem
				&& scrollState == SCROLL_STATE_IDLE) {//滑动到底部
			if (!isLoading) {
				isLoading = true;
				mListView.addFooterView(layoutLoadMore);
				loadData();
			}
		}
	}

	private void loadData() {
		final ProgressDialog pd = new ProgressDialog(FriRecordActivity.this,
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
						int size=parseAndSetTrace(arg0);
						if (arg0.equals("[]")||size<limit) {
							isLoading = true;
							mListView.removeFooterView(layoutLoadMore);
						} else {
							isLoading = false;
						}
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(firstVisibleItem);
						offset = offset + limit;
						pd.dismiss();

					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(FriRecordActivity.this,
								"加载失败 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("mobile", mobile);
				map.put("offset", offset + "");
				map.put("limit", limit + "");
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}

	private void init() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		mListView = (ListView) findViewById(R.id.FriRecordList);
		nullRecord = (FrameLayout) findViewById(R.id.id_nullrecord);
		layoutLoadMore = getLayoutInflater().inflate(R.layout.load_more, null);
		mAdapter = new FriRecordAdapter();
		mInflater = LayoutInflater.from(this);
		mListView.setOnScrollListener(this);
		mData = new ArrayList<TraceBean>();
		offset = 0;
	}

	protected int parseAndSetTrace(String str) {
		JSONArray arr = null;
		try {
			arr = new JSONArray(str);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject item = arr.getJSONObject(i);
				TraceBean bean = new TraceBean();
				bean.setTrace_id(item.optString("trace_id", null));
				bean.setUser_id(item.optString("user_id", null));
				bean.setStarttime(item.optString("starttime", null));
				bean.setEndtime(item.optString("endtime", null));
				bean.setDistance(item.optString("distance", null));
				mData.add(bean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (mData.isEmpty()) {
			nullRecord.setVisibility(View.VISIBLE);
		}
		mListView.setAdapter(mAdapter);
		return arr.length();
	}

	public void back(View v) {
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(TAG);
	}

	class FriRecordAdapter extends BaseAdapter {

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
			if(st!=null&&st!=""&&et!=null&&et!=""){
				holder.tv_date.setText(bean.getStarttime().substring(0, 10));
				holder.tv_time.setText(st.substring(st.length() - 8, st.length())
						+ "-" + et.substring(et.length() - 8, et.length()));
			}else{
				holder.tv_date.setText(bean.getStarttime().substring(0, 10));
				holder.tv_time.setText("-");
			}
			return convertView;
		}

		private class ViewHolder {
			TextView tv_date;
			TextView tv_time;
		}
	}

}
