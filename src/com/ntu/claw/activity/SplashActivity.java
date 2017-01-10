package com.ntu.claw.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class SplashActivity extends Activity {

	private RelativeLayout rootLayout;
	private Context mContext;

	private static final int SLEEPTIME = 2000;
	private String selfmobile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		mContext = this;
		rootLayout = (RelativeLayout) findViewById(R.id.splash_root);
		selfmobile = SPUtils.get(mContext, "mobile", "").toString();

		AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
		animation.setDuration(1500);
		rootLayout.startAnimation(animation);
	}



	@Override
	protected void onStart() {
		super.onStart();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(SLEEPTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (selfmobile != "") {
					startActivity(new Intent(mContext, MainActivity.class));
					finish();
				} else {
					startActivity(new Intent(mContext, LoginActivity.class));
					finish();
				}
			}
		}).start();
	}

}
