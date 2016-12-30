package com.ntu.claw.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class AddContactActivity extends Activity {

	public static final String SEARCHFRITAG = "searchfri";
	public static final String FRIASKTAG = "friask";

	private EditText et;
	private LinearLayout searchedUserLayout;
	private TextView nameText;

	private ImageView avater;
	private String selfmobile;

	// 当前搜索到的电话
	private String currentmobile;

	Dao dao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		selfmobile = (String) SPUtils.get(this, "mobile", "");// 获取自己的手机号

		dao = new Dao(AddContactActivity.this);

		et = (EditText) findViewById(R.id.edit_note);
		searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);
		avater = (ImageView) findViewById(R.id.avatar);
	}

	/**
	 * 搜索联系人并显示
	 * 
	 * @param v
	 */
	public void searchContact(View v) {
		searchedUserLayout.setVisibility(View.GONE);
		final String mobile = et.getText().toString().trim();
		if (TextUtils.isEmpty(mobile)) {
			T.showShort(AddContactActivity.this, "请输入对方手机号");
			return;
		}
		if (mobile.equals(selfmobile)) {
			T.showShort(AddContactActivity.this, "不能查找自己");
			return;
		}
		final ProgressDialog pd = new ProgressDialog(AddContactActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("搜索中...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("fail")) {
							T.showShort(AddContactActivity.this, "没有找到该联系人");
						} else {
							String[] str = arg0.split(";");
							nameText.setText(str[0]);
							loadAvater(str[1]);
							searchedUserLayout.setVisibility(View.VISIBLE);
							currentmobile = mobile;
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(AddContactActivity.this,
								"网络异常" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", SEARCHFRITAG);
				map.put("mobile", mobile);
				return map;
			}
		};
		request.setTag(SEARCHFRITAG);
		MyApplication.getHttpQueues().add(request);

	}

	private void loadAvater(String avaterUrl) {
		ImageRequest request = new ImageRequest(avaterUrl,
				new Listener<Bitmap>() {
					public void onResponse(Bitmap arg0) {
						avater.setImageBitmap(arg0);
					};
				}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError arg0) {
					};
				});
		MyApplication.getHttpQueues().add(request);
	}

	/**
	 * 添加联系人
	 * 
	 * @param view
	 */
	public void addContact(View view) {
		if (dao.findSameMobile(currentmobile, selfmobile)) {
			T.showShort(AddContactActivity.this, "你已经关注了该联系人");
			return;
		}
		final ProgressDialog pd = new ProgressDialog(AddContactActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("发送请求中...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("success")) {
							T.showShort(AddContactActivity.this, "发送请求成功");
						}
						if (arg0.equals("ConnectError")) {
							T.showShort(AddContactActivity.this, "连接失败");
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(AddContactActivity.this,
								"网络异常" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", FRIASKTAG);
				map.put("selfmobile", selfmobile);// 自己
				map.put("mobile", currentmobile);// 对方
				return map;
			}
		};
		request.setTag(FRIASKTAG);
		MyApplication.getHttpQueues().add(request);

	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(SEARCHFRITAG);
		MyApplication.getHttpQueues().cancelAll(FRIASKTAG);
	}

	public void back(View v) {
		finish();
	}
}
