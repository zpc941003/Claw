package com.ntu.claw.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.adapter.FriAskAdapter;
import com.ntu.claw.bean.FriAskBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class NewFriendsMsgActivity extends Activity{
	private ListView mListView;
	private FriAskAdapter adapter;
	private List<FriAskBean> mList;

	private FrameLayout nullnotify;
	
	private String selfmobile;
	private Context mContext;
	private static final String TAG="deletenotify";
	
	Dao dao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.activity_new_friends_msg);
		nullnotify=(FrameLayout) findViewById(R.id.id_nullnotify);
		selfmobile = (String) SPUtils.get(this, "mobile", "");
		dao = new Dao(this);
		deleteNotify();
		mListView = (ListView) findViewById(R.id.friAskList);
		MyApplication.unReadDot = false;
		initList();
	}

	private void deleteNotify() {
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {

					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						T.showShort(mContext, "¡¨Ω” ß∞‹ " + arg0.toString());
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


	public void initList() {
		mList = dao.getFriAsk(selfmobile);
		if(mList.isEmpty()){
			nullnotify.setVisibility(View.VISIBLE);
		}
		adapter = new FriAskAdapter(this, mList);
		mListView.setAdapter(adapter);
	}

	public void clearList(View view) {
		dao.clearFriAskList(selfmobile);
		mList.removeAll(mList);
		adapter.notifyDataSetChanged();
	}

	public void back(View view) {
		startActivity(new Intent(this, ContactActivity.class));
		finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ){
			startActivity(new Intent(this, ContactActivity.class));
			finish();
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(FriAskAdapter.TAG);
		MyApplication.getHttpQueues().cancelAll(TAG);
	}
}
