package com.ntu.claw.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.cache.DiskLruCacheHelper;
import com.ntu.claw.db.DbManager;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class SettingActivity extends Activity implements OnClickListener {
	private static final int SUBACTIVITY = 1;

	private Button bottomMapBtn;
	private Button bottomContactBtn;
	private Button bottomSettingBtn;

	private TextView tvName;
	private TextView tvMobile;
	private ImageView avater;

	private RelativeLayout layoutOfflineMap;
	private RelativeLayout selfRecord;
	private RelativeLayout myinfo;
	private RelativeLayout openGPS;
	private RelativeLayout clearCache;
	private RelativeLayout about;

	private Context context;
	private Button logoutBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		context = this;
		bottomMapBtn = (Button) findViewById(R.id.btn_map);
		bottomContactBtn = (Button) findViewById(R.id.btn_contact);
		bottomSettingBtn = (Button) findViewById(R.id.btn_setting);
		logoutBtn = (Button) findViewById(R.id.btn_logout);
		layoutOfflineMap = (RelativeLayout) findViewById(R.id.re_offlinemap);
		selfRecord = (RelativeLayout) findViewById(R.id.re_personalrecord);
		myinfo = (RelativeLayout) findViewById(R.id.re_myinfo);
		tvName = (TextView) findViewById(R.id.tv_name);
		tvMobile = (TextView) findViewById(R.id.tv_id);
		avater = (ImageView) findViewById(R.id.iv_avatar);
		openGPS = (RelativeLayout) findViewById(R.id.re_opengps);
		clearCache=(RelativeLayout) findViewById(R.id.re_clearcache);
		about=(RelativeLayout) findViewById(R.id.re_about);
		bottomSettingBtn.setSelected(true);
		tvName.setText(SPUtils.get(this, "selfname", "").toString());
		tvMobile.setText("手机号:" + SPUtils.get(this, "mobile", "").toString());
		initAvater();
		initClickEvent();
	}

	private void initAvater() {
		final String avaterUrl = SPUtils.get(context, "avater", "").toString();
		Bitmap bitmap = DiskLruCacheHelper.getInstance(context).readFromCache(
				avaterUrl);
		if (bitmap != null) {
			avater.setImageBitmap(bitmap);
		} else {
			ImageRequest request = new ImageRequest(avaterUrl,
					new Listener<Bitmap>() {
						public void onResponse(Bitmap arg0) {
							DiskLruCacheHelper.getInstance(context)
									.writeToCache(avaterUrl, arg0);
							avater.setImageBitmap(arg0);
						};
					}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
						public void onErrorResponse(VolleyError arg0) {
						};
					});
			MyApplication.getHttpQueues().add(request);
		}
	}

	private void initClickEvent() {
		bottomContactBtn.setOnClickListener(this);
		bottomMapBtn.setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		layoutOfflineMap.setOnClickListener(this);
		selfRecord.setOnClickListener(this);
		myinfo.setOnClickListener(this);
		openGPS.setOnClickListener(this);
		clearCache.setOnClickListener(this);
		about.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_contact:
			startActivity(new Intent(SettingActivity.this,
					ContactActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		case R.id.btn_map:
			startActivity(new Intent(SettingActivity.this, MainActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		case R.id.btn_logout:
			/**
			 * applcation中的数据还原
			 */
			MyApplication.setRecordFlag(false);
			MyApplication.setTrace_id(null);
			MyApplication.setDistance(0.0);
			MyApplication.location=null;
			SPUtils.clear(this);
			DbManager.getInstance().closeDB();
			startActivity(new Intent(SettingActivity.this, LoginActivity.class));
			finish();
			// overridePendingTransition(0, 0);
			break;
		case R.id.re_offlinemap:
			startActivity(new Intent(this, OffLineMapActivity.class));
			break;
		case R.id.re_personalrecord:
			startActivity(new Intent(this, SelfRecordActivity.class));
			break;
		case R.id.re_myinfo:
			startActivityForResult(new Intent(this, MyInfoActivity.class),
					SUBACTIVITY);
			break;
		case R.id.re_opengps:
			openGPS();
			break;
		case R.id.re_clearcache:
			DiskLruCacheHelper.getInstance(context).deleteCache();
			T.showShort(context, "清除成功");
			break;
		case R.id.re_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		default:
			break;
		}
	}

	private void openGPS() {
		if (isGpsEnabled()) {
			T.showShort(this, "GPS已打开");
		} else {
			openGpsSettings();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SUBACTIVITY:
			tvName.setText(SPUtils.get(this, "selfname", "").toString());
			tvMobile.setText("手机号:"
					+ SPUtils.get(this, "mobile", "").toString());
			initAvater();
			break;

		default:
			break;
		}
	}

	/**
	 * 判断Gps是否可用
	 * 
	 * @return {@code true}: 是<br>
	 *         {@code false}: 否
	 */
	public boolean isGpsEnabled() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * 打开Gps设置界面
	 */
	public void openGpsSettings() {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
