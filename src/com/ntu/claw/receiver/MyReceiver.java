package com.ntu.claw.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import cn.jpush.android.api.JPushInterface;

import com.ntu.claw.activity.ContactActivity;
import com.ntu.claw.activity.NewFriendsMsgActivity;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;

public class MyReceiver extends BroadcastReceiver {
	public static final String TAG = "JPush";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "onReceive - " + intent.getAction());

		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			//System.out.println("�յ����Զ�����Ϣ����Ϣ�����ǣ�"
				//	+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			// �Զ�����Ϣ����չʾ��֪ͨ������ȫҪ������д����ȥ����
			ObservableObject.getInstance().updateValue(bundle.getString(JPushInterface.EXTRA_MESSAGE));

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			System.out.println("�յ���֪ͨ");
			// �����������Щͳ�ƣ�������Щ��������
			MyApplication.unReadDot = true;

			final String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			System.out.println(extras);
			
			//���󱣴浽���ݿ�
			new Thread(new Runnable() {
				@Override
				public void run() {
					Dao dao=new Dao(context);
					String selfmobile=(String) SPUtils.get(context, "mobile", "");
					try {
						JSONObject json=new JSONObject(extras);
						String mobile=json.optString("mobile","");
						String username=json.optString("username","");
						String avater=json.optString("avater","");
						dao.saveFriAsk(selfmobile, mobile, username,avater);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			if (ContactActivity.getInstance()!=null) {
				ContactActivity.getInstance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ImageView iv1 = (ImageView) ContactActivity
								.getInstance().findViewById(R.id.unread_dot);
						ImageView iv2 = (ImageView) ContactActivity
								.getInstance().findViewById(R.id.id_unread_dot);
						iv1.setVisibility(View.VISIBLE);
						iv2.setVisibility(View.VISIBLE);
					}
				});
			}

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {
			System.out.println("�û��������֪ͨ");
			// ����������Լ�д����ȥ�����û���������Ϊ
			Intent i = new Intent(context, NewFriendsMsgActivity.class); // �Զ���򿪵Ľ���
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		} else {
			Log.d(TAG, "Unhandled intent - " + intent.getAction());
		}
	}

}
