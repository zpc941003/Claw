package com.ntu.claw.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.utils.MD5;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class LoginActivity extends Activity {
	public static final String LOGINTAG = "login";
	private EditText passwordEditText;
	private EditText mobileEditText;

	private String currentMobile;
	private String currentPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mobileEditText = (EditText) findViewById(R.id.mobile_num);
		passwordEditText = (EditText) findViewById(R.id.password);
	}

	public void login(View view) {
		currentMobile = mobileEditText.getText().toString().trim();
		currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentMobile)) {
			T.showShort(this, "手机号不能为空");
			return;
		}
		if (!currentMobile.matches("^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$")) {
			T.showShort(this, "请输入正确的手机号");
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			T.showShort(this, "密码不能为空");
			return;
		}
		final ProgressDialog pd = new ProgressDialog(LoginActivity.this,ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("登录中...");
		pd.show();
		String url = MyApplication.ipAddress+"/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if(arg0.equals("noaccount")){
							T.showShort(LoginActivity.this, "该帐号未注册");
						}else if(arg0.equals("fail")){
							T.showShort(LoginActivity.this, "密码错误");
						}else{
							//登录成功保存状态
							String[] str=arg0.split(";");
							SPUtils.put(LoginActivity.this, "mobile", currentMobile);
							SPUtils.put(LoginActivity.this, "selfname", str[0]);
							SPUtils.put(LoginActivity.this, "avater", str[1]);
							T.showShort(LoginActivity.this, "登录成功");
							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							startActivity(intent);
							finish();
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(LoginActivity.this, "登录失败 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", LOGINTAG);
				map.put("mobile", currentMobile);
				map.put("password", MD5.getMD5(currentPassword));
				return map;
			}
		};
		request.setTag(LOGINTAG);
		MyApplication.getHttpQueues().add(request);
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(LOGINTAG);
	}

	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}
}
