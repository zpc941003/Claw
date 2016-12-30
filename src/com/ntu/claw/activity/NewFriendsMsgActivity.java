package com.ntu.claw.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.adapter.FriAskAdapter;
import com.ntu.claw.adapter.FriAskAdapter.Callback;
import com.ntu.claw.bean.FriAskBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;

public class NewFriendsMsgActivity extends Activity{
	private ListView mListView;
	private FriAskAdapter adapter;
	private List<FriAskBean> mList;

	private FrameLayout nullnotify;
	
	private String selfmobile;

	Dao dao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_friends_msg);
		nullnotify=(FrameLayout) findViewById(R.id.id_nullnotify);
		selfmobile = (String) SPUtils.get(this, "mobile", "");
		dao = new Dao(this);
		mListView = (ListView) findViewById(R.id.friAskList);
		MyApplication.unReadDot = false;
		initList();
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
	public void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(FriAskAdapter.TAG);
	}
}
