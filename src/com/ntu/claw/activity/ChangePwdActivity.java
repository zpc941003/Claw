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
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class ChangePwdActivity extends Activity {

	private EditText oldPwd;
	private EditText newPwd;
	private EditText twicePwd;

	private static final String TAG = "changepwd";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pwd);
		oldPwd = (EditText) findViewById(R.id.tv_oldpwd);
		newPwd = (EditText) findViewById(R.id.tv_newpwd);
		twicePwd = (EditText) findViewById(R.id.tv_twicepwd);
	}

	public void back(View v) {
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(TAG);
	}

	public void confirm(View v) {
		final String oldpwd = oldPwd.getText().toString().trim();
		final String newpwd = newPwd.getText().toString().trim();
		final String twicepwd = twicePwd.getText().toString().trim();
		if (TextUtils.isEmpty(oldpwd) || TextUtils.isEmpty(newpwd)
				|| TextUtils.isEmpty(twicepwd)) {
			T.showShort(this, "密码不能为空");
			return;
		}
		if (!newpwd.equals(twicepwd)) {
			T.showShort(this, "两次输入的密码不一致");
			return;
		}
		final ProgressDialog pd = new ProgressDialog(this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("请稍后...");
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("error")) {
							T.showShort(ChangePwdActivity.this, "密码错误");
							oldPwd.requestFocus();
						} else if (arg0.equals("success")) {
							T.showShort(ChangePwdActivity.this, "修改成功");
							finish();
						} else{
							T.showShort(ChangePwdActivity.this, "修改失败");
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(ChangePwdActivity.this,
								"网络异常 " + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("mobile",
						SPUtils.get(ChangePwdActivity.this, "mobile", "")
								.toString());
				map.put("oldpwd", MD5.getMD5(oldpwd));
				map.put("newpwd", MD5.getMD5(newpwd));
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}
}
