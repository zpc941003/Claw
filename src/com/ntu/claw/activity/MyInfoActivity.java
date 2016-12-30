package com.ntu.claw.activity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.ntu.claw.cache.DiskLruCacheHelper;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.ImgUtils;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class MyInfoActivity extends Activity implements OnClickListener {

	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private static final int REQUESTCODE_TAKEPHOTO = 3;

	private TextView tvName;
	private TextView tvMobile;
	private ImageView imgAvater;

	private RelativeLayout reName;
	private RelativeLayout reMobile;
	private RelativeLayout rePwd;
	private RelativeLayout reAvater;

	private Context context;

	private static final String TAG1 = "updatename";
	private static final String TAG2 = "updatemobile";
	private static final String TAG3 = "uploadavater";

	private static final String url = MyApplication.ipAddress
			+ "/ClawServer/servlet/ManageServlet";

	Dao dao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);
		context = this;
		initView();
		initClickEvent();
		dao = new Dao(this);
		final String avaterUrl = SPUtils.get(context, "avater", "").toString();
		Bitmap bitmap = DiskLruCacheHelper.getInstance(context).readFromCache(
				avaterUrl);
		if (bitmap != null) {
			imgAvater.setImageBitmap(bitmap);
		} else {
			ImageRequest request = new ImageRequest(avaterUrl,
					new Listener<Bitmap>() {
						public void onResponse(Bitmap arg0) {
							DiskLruCacheHelper.getInstance(context)
									.writeToCache(avaterUrl, arg0);
							imgAvater.setImageBitmap(arg0);
						};
					}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
						public void onErrorResponse(VolleyError arg0) {
						};
					});
			MyApplication.getHttpQueues().add(request);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(TAG1);
		MyApplication.getHttpQueues().cancelAll(TAG2);
		MyApplication.getHttpQueues().cancelAll(TAG3);
	}

	private void initClickEvent() {
		reName.setOnClickListener(this);
		reMobile.setOnClickListener(this);
		rePwd.setOnClickListener(this);
		reAvater.setOnClickListener(this);
	}

	private void initView() {
		imgAvater = (ImageView) findViewById(R.id.img_avatar);
		reAvater = (RelativeLayout) findViewById(R.id.re_avatar);
		rePwd = (RelativeLayout) findViewById(R.id.re_cgpwd);
		reName = (RelativeLayout) findViewById(R.id.re_name);
		tvName = (TextView) findViewById(R.id.tv_name);
		reMobile = (RelativeLayout) findViewById(R.id.re_fxid);
		tvMobile = (TextView) findViewById(R.id.tv_fxid);
		tvName.setText(SPUtils.get(this, "selfname", "").toString());
		tvMobile.setText(SPUtils.get(this, "mobile", "").toString());
	}

	public void back(View v) {
		finish();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.re_name:
			final EditText editText = new EditText(this);
			editText.setText(tvName.getText());
			editText.setSelection(tvName.getText().length());
			new AlertDialog.Builder(this)
					.setTitle("修改用户名")
					.setView(editText)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String name = editText.getText().toString();
									if (!TextUtils.isEmpty(name)
											&& !name.equals(tvName.getText()
													.toString())) {
										updateName(name);
									}
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.re_fxid:
			final EditText editText2 = new EditText(this);
			editText2.setText(tvMobile.getText());
			editText2.setSelection(tvMobile.getText().length());
			new AlertDialog.Builder(this)
					.setTitle("修改绑定手机号")
					.setView(editText2)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String mobile = editText2.getText()
											.toString();
									if (!TextUtils.isEmpty(mobile)
											&& !mobile.equals(tvMobile
													.getText().toString())) {
										updateMobile(mobile);
									}
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.re_cgpwd:
			startActivity(new Intent(this, ChangePwdActivity.class));
			break;
		case R.id.re_avatar:
			pickHeadPhoto();
			break;
		default:
			break;
		}
	}

	private Uri imageUri;
	private void pickHeadPhoto() {
		new AlertDialog.Builder(this)
				.setTitle("上传头像")
				.setItems(new String[] { "拍照上传", "本地上传" },
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								dialog.dismiss();
								switch (arg1) {
								case 0:
									//拍照后图片存储路径
									File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
									try {
										if(outputImage.exists()){
											outputImage.delete();
										}
										outputImage.createNewFile();
									} catch (IOException e) {
										e.printStackTrace();
									}
									imageUri=Uri.fromFile(outputImage);
									Intent mIntent = new Intent(
											"android.media.action.IMAGE_CAPTURE");
									mIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
									startActivityForResult(mIntent,
											REQUESTCODE_TAKEPHOTO);//--------------------21
									break;
								case 1:
									Intent intent = new Intent(
											Intent.ACTION_PICK);
									intent.setDataAndType(
											MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
											"image/*");
									startActivityForResult(intent,
											REQUESTCODE_PICK);// ------------11
									break;
								default:
									break;
								}
							}
						}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_PICK:// ------------------------------------12
			if (data == null || data.getData() == null) {
				return;
			}
			startPhotoZoom(data.getData());// ---------------------------13
			break;
		case REQUESTCODE_CUTTING:// -------------------------------------15
			if (data != null) {
				uploadAvater(data);// -----------------------------------16
			}
			break;
		case REQUESTCODE_TAKEPHOTO://--------------------------------22
				startPhotoZoom(imageUri);//-----------------------------13
			break;
		default:
			break;
		}
	}

	private void uploadAvater(Intent data) {// ---------------------------16
		Bundle extras = data.getExtras();
		if (extras != null) {
			final Bitmap photo = extras.getParcelable("data");
			final ProgressDialog pd = new ProgressDialog(MyInfoActivity.this,
					ProgressDialog.THEME_HOLO_LIGHT);
			pd.setCanceledOnTouchOutside(false);
			pd.setMessage("请稍后...");
			pd.show();
			StringRequest request = new StringRequest(Method.POST, url,
					new Listener<String>() {
						@Override
						public void onResponse(String arg0) {
							pd.dismiss();
							if (arg0 != null) {
								SPUtils.put(MyInfoActivity.this, "avater", arg0);
								DiskLruCacheHelper.getInstance(context)
										.writeToCache(arg0, photo);
								setPicToView(photo);// ---------------------18
								T.showShort(context, "上传成功");
							}
						}
					}, new ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError arg0) {
							pd.dismiss();
							T.showShort(MyInfoActivity.this,
									"网络异常" + arg0.toString());
						}
					}) {
				@Override
				protected Map<String, String> getParams()
						throws AuthFailureError {
					Map<String, String> map = new HashMap<String, String>();
					map.put("action", TAG3);
					map.put("avater", ImgUtils.bitmapToBase64(photo));// --------------------17
					map.put("mobile",
							SPUtils.get(MyInfoActivity.this, "mobile", "")
									.toString());
					return map;
				}
			};
			request.setTag(TAG3);
			MyApplication.getHttpQueues().add(request);
		}
	}

	private void setPicToView(Bitmap photo) {// ------------------------------------------18
		Drawable drawable = new BitmapDrawable(getResources(), photo);
		imgAvater.setImageDrawable(drawable);
	}

	private void startPhotoZoom(Uri uri) {// --------------------------------------13
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 100);// 输出为100像素
		intent.putExtra("outputY", 100);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);// -------------------------14
	}

	protected void updateMobile(final String mobile) {
		final ProgressDialog pd = new ProgressDialog(MyInfoActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("请稍后...");
		pd.show();
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("success")) {
							dao.updateMobile(
									mobile,
									SPUtils.get(MyInfoActivity.this, "mobile",
											"").toString());
							tvMobile.setText(mobile);
							SPUtils.put(MyInfoActivity.this, "mobile", mobile);
							T.showShort(MyInfoActivity.this, "修改成功");
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(MyInfoActivity.this,
								"网络异常" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG2);
				map.put("mobile", SPUtils
						.get(MyInfoActivity.this, "mobile", "").toString());
				map.put("updatemobile", mobile);
				return map;
			}
		};
		request.setTag(TAG2);
		MyApplication.getHttpQueues().add(request);
	}

	protected void updateName(final String name) {
		final ProgressDialog pd = new ProgressDialog(MyInfoActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("请稍后...");
		pd.show();
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String arg0) {
						pd.dismiss();
						if (arg0.equals("success")) {
							tvName.setText(name);
							SPUtils.put(MyInfoActivity.this, "selfname", name);
							T.showShort(MyInfoActivity.this, "修改成功");
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(MyInfoActivity.this,
								"网络异常" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG1);
				map.put("mobile", SPUtils
						.get(MyInfoActivity.this, "mobile", "").toString());
				map.put("name", name);
				return map;
			}
		};
		request.setTag(TAG1);
		MyApplication.getHttpQueues().add(request);
	}
}
