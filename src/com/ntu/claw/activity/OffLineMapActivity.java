package com.ntu.claw.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ExpandableListView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.ntu.claw.R;
import com.ntu.claw.adapter.OfflineMapListAdapter;
import com.ntu.claw.bean.OfflineMapCityBean;
import com.ntu.claw.bean.OfflineMapCityBean.Flag;
import com.ntu.claw.service.OfflineMapDownService;
import com.ntu.claw.utils.NetUtils;
import com.ntu.claw.utils.T;

public class OffLineMapActivity extends Activity {
	
	OfflineMapListAdapter adapter;
	ExpandableListView listView;
	List<OfflineMapCityBean> cities = new ArrayList<OfflineMapCityBean>();
	Map<OfflineMapCityBean, List<OfflineMapCityBean>> childCities = new HashMap<OfflineMapCityBean, List<OfflineMapCityBean>>();

	public static final int quanguo = 1;
	public static final int beijing = 131;
	public static final int shanghai = 289;
	public static final int tianjing = 332;
	public static final int chongqing = 132;
	public static final int xianggang = 2912;
	public static final int aomen = 2911;
	OfflineMapCityBean quanguoo;
	ArrayList<OfflineMapCityBean> quanguooChilds = new ArrayList<OfflineMapCityBean>();
	OfflineMapCityBean zhixia;
	ArrayList<OfflineMapCityBean> zhixiaChilds = new ArrayList<OfflineMapCityBean>();
	OfflineMapCityBean gangao;
	ArrayList<OfflineMapCityBean> gangaoChilds = new ArrayList<OfflineMapCityBean>();
	
	
	private void initCities(ArrayList<MKOLSearchRecord> offlineCityList,
			ArrayList<MKOLUpdateElement> allUpdateInfo) {
		for (MKOLSearchRecord record : offlineCityList) {
			OfflineMapCityBean cityBean = new OfflineMapCityBean();
			cityBean.setCityName(record.cityName);
			cityBean.setCityCode(record.cityID);
			cityBean.setSize(record.size);

			if (allUpdateInfo != null) {
				for (MKOLUpdateElement ele : allUpdateInfo) {
					if (ele.cityID == record.cityID) {
						cityBean.setProgress(ele.ratio);
						break;
					}
				}
			}

			if (record.childCities != null) {
				cities.add(cityBean);
				initChildCities(cityBean, record.childCities, allUpdateInfo);
			} else {
				int cityCode = cityBean.getCityCode();
				if (cityCode == quanguo) {
					quanguooChilds.add(cityBean);
				} else if (cityCode == beijing || cityCode == shanghai
						|| cityCode == tianjing || cityCode == chongqing) {
					zhixiaChilds.add(cityBean);
				} else if (cityCode == xianggang || cityCode == aomen) {
					gangaoChilds.add(cityBean);
				}
			}
		}
	}

	private void initChildCities(OfflineMapCityBean p,
			ArrayList<MKOLSearchRecord> offlineCityList,
			ArrayList<MKOLUpdateElement> allUpdateInfo) {
		ArrayList<OfflineMapCityBean> childs = new ArrayList<OfflineMapCityBean>();
		for (MKOLSearchRecord record : offlineCityList) {
			OfflineMapCityBean cityBean = new OfflineMapCityBean();
			cityBean.setCityName(record.cityName);
			cityBean.setCityCode(record.cityID);
			cityBean.setSize(record.size);

			if (allUpdateInfo != null) {
				for (MKOLUpdateElement ele : allUpdateInfo) {
					if (ele.cityID == record.cityID) {
						cityBean.setProgress(ele.ratio);
						break;
					}
				}
			}
			childs.add(cityBean);
		}
		childCities.put(p, childs);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_off_line_map);
		/* initTitleBar(); */

		quanguoo = new OfflineMapCityBean();
		quanguoo.setCityName("全国概略图");
		cities.add(quanguoo);
		childCities.put(quanguoo, quanguooChilds);
		zhixia = new OfflineMapCityBean();
		zhixia.setCityName("直辖市");
		cities.add(zhixia);
		childCities.put(zhixia, zhixiaChilds);
		gangao = new OfflineMapCityBean();
		gangao.setCityName("港澳");
		cities.add(gangao);
		childCities.put(gangao, gangaoChilds);
		adapter = new OfflineMapListAdapter(this, cities, childCities);
		listView = (ExpandableListView) findViewById(R.id.offlineMapListView);
		listView.setGroupIndicator(null);
		listView.setAdapter(adapter);

		showNetWorkEnvironmentToast();
	}

	private void showNetWorkEnvironmentToast() {
		if (NetUtils.isConnected(this)) {
			if (!NetUtils.isWifi(this)) {
				T.showShort(this, "当前非wifi环境");
			}
		}
	}

	private void startOmdService() {
		Intent serviceIntent = new Intent(this, OfflineMapDownService.class);
		startService(serviceIntent);
	}

	private boolean isBindService = false;
	private OfflineMapDownService offlineMapDownService;

	public void bindOmdService() {
		if (!isBindService) {
			isBindService = true;
			Intent gattServiceIntent = new Intent(this,
					OfflineMapDownService.class);
			bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		}
	}

	public void unBindOmdService() {
		if (isBindService) {
			isBindService = false;
			unbindService(mServiceConnection);
			offlineMapDownService = null;
		}
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			offlineMapDownService = ((OfflineMapDownService.LocalBinder) service)
					.getService();
			ArrayList<MKOLSearchRecord> offlineCityList = offlineMapDownService
					.getOfflineCityList();
			ArrayList<MKOLUpdateElement> allUpdateInfo = offlineMapDownService
					.getAllUpdateInfo();
			initCities(offlineCityList, allUpdateInfo);

			listView.expandGroup(0); // quanguo
			listView.expandGroup(1); // zhixia
			listView.expandGroup(2); // gangao

			// moveToSelfCity();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			offlineMapDownService = null;
		}
	};

	/*
	 * private void moveToSelfCity() { String cityCode = Preferences. if
	 * (TextUtils.isEmpty(cityCode)) { return; } int myCityCode =
	 * Integer.parseInt(cityCode); boolean find = false; int groupIndex = 0; for
	 * (int i = 0; i < cities.size(); i++) { if (find) break;
	 * 
	 * if (cities.get(i).getCityCode() == myCityCode) { groupIndex = i; find =
	 * true; continue; } List<OfflineMapCityBean> childList =
	 * childCities.get(cities.get(i)); if (childCities.get(cities.get(i)) !=
	 * null) { for (int j = 0; j < childList.size(); j++) { if
	 * (childList.get(j).getCityCode() == myCityCode) { groupIndex = i; find =
	 * true; break; } } } }
	 * 
	 * listView.expandGroup(groupIndex); listView.setSelection(groupIndex); }
	 */

	public boolean isDownloadOnTheTask(int cityId) {
		return offlineMapDownService.isDownloadOnTheTask(cityId);
	}

	public void removeTaskFromQueue(int cityId) {
		offlineMapDownService.removeTaskFromQueue(cityId);
	}

	public void addToDownloadQueue(int cityId) {
		offlineMapDownService.addToDownloadQueue(cityId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unBindOmdService();

		unregisterReceiver(mOfflineMapReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();

		startOmdService();
		bindOmdService();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(OfflineMapDownService.ACTION_DOWNLOAD_UPDATE);

		registerReceiver(mOfflineMapReceiver, intentFilter);
	}

	private final BroadcastReceiver mOfflineMapReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (OfflineMapDownService.ACTION_DOWNLOAD_UPDATE.equals(action)) {
				int state = intent.getIntExtra(
						OfflineMapDownService.EXTRA_DATA_STATE, 0);
				int ratio = intent.getIntExtra(
						OfflineMapDownService.EXTRA_DATA_RATIO, 0);
				boolean updateUI = false;
				for (OfflineMapCityBean bean : cities) {
					if (updateUI)
						break;

					if (bean.getCityCode() == state) {
						bean.setProgress(ratio);
						bean.setFlag(Flag.DOWNLOADING);
						updateUI = true;
					} else {
						List<OfflineMapCityBean> cs = childCities.get(bean);
						if (cs == null) {
							continue;
						}
						for (OfflineMapCityBean bean2 : cs) {
							if (bean2.getCityCode() == state) {
								bean2.setProgress(ratio);
								bean2.setFlag(Flag.DOWNLOADING);
								updateUI = true;
								break;
							}
						}
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
	};

	public void back(View v) {
		finish();
	}
	
	public void forward(View v){
		startActivity(new Intent(this,OfflineMapDoneActivity.class));
	}
	/*
	 * private void initTitleBar() { TitleBar titleBar = (TitleBar)
	 * findViewById(R.id.title_bar); titleBar.setTitle(R.string.offline_maps);
	 * titleBar.setBelongActivity(this); }
	 */

	/*
	 * public static void launch(Context context) { Intent intent = new
	 * Intent(context, OfflineMapDownActivity.class);
	 * context.startActivity(intent); ((Activity)
	 * context).overridePendingTransition(R.anim.slide_in_right,
	 * R.anim.keep_original_state); if(!NetworkUtils.isNetConnect(context)){
	 * ((BaseActivity)context).showToast(R.string.network_wrong); }
	 * 
	 * }
	 */

}