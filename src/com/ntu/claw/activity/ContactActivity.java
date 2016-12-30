package com.ntu.claw.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ntu.claw.MyApplication;
import com.ntu.claw.R;

public class ContactActivity extends Activity implements OnClickListener {

	private Button bottomMapBtn;
	private Button bottomContactBtn;
	private Button bottomSettingBtn;
	private Button btnICare;
	private Button btnCareI;

	private Fragment iCareFrag;
	private Fragment careIFrag;

	public static final int ICAREFRAG = 0;
	public static final int CAREIFRAG = 1;
	
	private ImageView btnAdd;
	private ImageView unReadDot1;
	private ImageView unReadDot2;
	private LinearLayout btnAddFri;
	private LinearLayout btnNotify;
	private LinearLayout dialogLayout;
	
	private static ContactActivity instance;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		bottomMapBtn = (Button) findViewById(R.id.btn_map);
		bottomContactBtn = (Button) findViewById(R.id.btn_contact);
		bottomSettingBtn = (Button) findViewById(R.id.btn_setting);
		bottomContactBtn.setSelected(true);
		btnICare=(Button) findViewById(R.id.btn_i_care);
		btnCareI=(Button) findViewById(R.id.btn_care_i);
		btnAdd=(ImageView) findViewById(R.id.iv_add);
		btnAddFri=(LinearLayout) findViewById(R.id.id_addfrd);
		btnNotify=(LinearLayout) findViewById(R.id.id_notify);
		dialogLayout=(LinearLayout) findViewById(R.id.pop_dialog_layout);
		unReadDot1=(ImageView) findViewById(R.id.unread_dot);
		unReadDot2=(ImageView) findViewById(R.id.id_unread_dot);
		initClickEvent();
		setFragSelected(ICAREFRAG);
		instance=this;
		if(MyApplication.unReadDot==true){
			unReadDot1.setVisibility(View.VISIBLE);
			unReadDot2.setVisibility(View.VISIBLE);
		}
	}

	public static ContactActivity getInstance(){
		return instance;
	}
	
	private void initClickEvent() {
		bottomSettingBtn.setOnClickListener(this);
		bottomMapBtn.setOnClickListener(this);
		btnICare.setOnClickListener(this);
		btnCareI.setOnClickListener(this);
		btnAdd.setOnClickListener(this);
		btnAddFri.setOnClickListener(this);
		btnNotify.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_setting:
			startActivity(new Intent(ContactActivity.this,
					SettingActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		case R.id.btn_map:
			startActivity(new Intent(ContactActivity.this, MainActivity.class));
			finish();
			overridePendingTransition(0, 0);
			break;
		case R.id.btn_i_care:
			setFragSelected(ICAREFRAG);
			break;
		case R.id.btn_care_i:
			setFragSelected(CAREIFRAG);
			break;
		case R.id.iv_add:
			popAndHideDialog();
			break;
		case R.id.id_addfrd:
			startActivity(new Intent(ContactActivity.this,AddContactActivity.class));
			popAndHideDialog();
			break;
		case R.id.id_notify:
			startActivity(new Intent(ContactActivity.this,NewFriendsMsgActivity.class));
			popAndHideDialog();
			finish();
			break;
		default:
			break;
		}
	}

	private void popAndHideDialog() {
		if(dialogLayout.getVisibility()==View.VISIBLE){
			dialogLayout.setVisibility(View.GONE);
		}else{
			dialogLayout.setVisibility(View.VISIBLE);
		}
	}

	private void setFragSelected(int i) {
		FragmentManager fm = getFragmentManager();
		// ¿ªÆôFragmentÊÂÎñ
		FragmentTransaction transaction = fm.beginTransaction();
		switch (i) {
		case ICAREFRAG:
			if (iCareFrag == null) {
				iCareFrag = new ICareFragment();
			}
			transaction.replace(R.id.fragment_container, iCareFrag);
			btnICare.setSelected(true);
			btnCareI.setSelected(false);
			break;
		case CAREIFRAG:
			if (careIFrag == null) {
				careIFrag = new CareIFragment();
			}
			transaction.replace(R.id.fragment_container, careIFrag);
			btnCareI.setSelected(true);
			btnICare.setSelected(false);
			break;
		default:
			break;
		}
		transaction.commit();
	}
}
