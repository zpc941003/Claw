package com.ntu.claw.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.utils.MD5;
import com.ntu.claw.utils.T;

public class RegisterActivity extends Activity {

	public static final String REGISTERTAG = "register";

	private EditText mobileEditText;
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mobileEditText = (EditText) findViewById(R.id.mobile_num);
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);

	}

	public void register(View view) {
		final String mobile = mobileEditText.getText().toString().trim();
		final String username = userNameEditText.getText().toString().trim();
		final String pwd = passwordEditText.getText().toString().trim();
		String confirm_pwd = confirmPwdEditText.getText().toString().trim();
		if (TextUtils.isEmpty(mobile)) {
			T.showShort(this, "手机号不能为空");
			mobileEditText.requestFocus();
			return;
		} else if (!mobile.matches("^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$")) {
			T.showShort(this, "请输入正确的手机号");
			mobileEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(username)) {
			T.showShort(this, "用户名不能为空");
			userNameEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(pwd)) {
			T.showShort(this, "密码不能为空");
			passwordEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(confirm_pwd)) {
			T.showShort(this, "确认密码不能为空");
			confirmPwdEditText.requestFocus();
			return;
		} else if (!pwd.equals(confirm_pwd)) {
			T.showShort(this, "两次输入的密码不一致");
			passwordEditText.requestFocus();
			return;
		}
		final ProgressDialog pd = new ProgressDialog(RegisterActivity.this,ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("注册中...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("success")) {
							T.showShort(RegisterActivity.this, "注册成功");
							finish();
						}
						if (arg0.equals("samemobile")) {
							T.showShort(RegisterActivity.this, "该手机号已被注册");
						}
						if (arg0.equals("fail")) {
							T.showShort(RegisterActivity.this, "注册失败");
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(RegisterActivity.this, "注册失败 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", REGISTERTAG);
				map.put("mobile", mobile);
				map.put("username", username);
				map.put("password", MD5.getMD5(pwd));
				return map;
			}
		};
		request.setTag(REGISTERTAG);
		MyApplication.getHttpQueues().add(request);
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(REGISTERTAG);
	}
}
