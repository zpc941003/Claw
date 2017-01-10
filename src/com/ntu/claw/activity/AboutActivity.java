package com.ntu.claw.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ntu.claw.R;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
	
	public void back(View v){
		finish();
	}
}
