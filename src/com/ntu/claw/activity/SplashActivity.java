package com.ntu.claw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.ntu.claw.R;
import com.ntu.claw.utils.SPUtils;

public class SplashActivity extends Activity {

	private RelativeLayout rootLayout;

	public static final int SLEEPTIME = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		rootLayout = (RelativeLayout) findViewById(R.id.splash_root);

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
				if (SPUtils.get(SplashActivity.this, "mobile", "") == "") {
					startActivity(new Intent(SplashActivity.this,
							LoginActivity.class));
					finish();
				} else {
					startActivity(new Intent(SplashActivity.this,
							MainActivity.class));
					finish();
				}
			}
		}).start();
	}

}
