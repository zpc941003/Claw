package com.ntu.claw.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ntu.claw.R;
import com.ntu.claw.bean.OfflineMapCityBean;

public class OfflineMapDoneActivity extends Activity {

	private MKOfflineMap mOfflineMap;
	private ListView mListView;
	private LayoutInflater mInflater;
	private OfflineDoneAdapter mAdapter;

	private List<OfflineMapCityBean> mData = new ArrayList<OfflineMapCityBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map_done);
		initOfflineMap();
		initData();
		initView();
	}

	private void initView() {
		mInflater = LayoutInflater.from(this);
		mAdapter = new OfflineDoneAdapter();
		mListView = (ListView) findViewById(R.id.OfflineMapDoneListView);
		mListView.setAdapter(mAdapter);
	}

	private void initData() {
		List<MKOLUpdateElement> list = mOfflineMap.getAllUpdateInfo();
		if (list != null) {
			for (MKOLUpdateElement mkolUpdateElement : list) {
				OfflineMapCityBean bean = new OfflineMapCityBean();
				bean.setCityName(mkolUpdateElement.cityName);
				bean.setCityCode(mkolUpdateElement.cityID);
				mData.add(bean);
			}
		}
	}

	public void back(View v) {
		finish();
	}

	private void initOfflineMap() {
		mOfflineMap = new MKOfflineMap();
		// …Ë÷√º‡Ã˝
		mOfflineMap.init(new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
			}
		});
	}

	@Override
	protected void onDestroy() {
		mOfflineMap.destroy();
		super.onDestroy();
	}

	class OfflineDoneAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final OfflineMapCityBean bean = mData.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_map_offline_done,
						parent, false);
				holder.cityName = (TextView) convertView
						.findViewById(R.id.tx_del_city_name);
				holder.delete = (ImageView) convertView
						.findViewById(R.id.img_deletemap);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.cityName.setText(bean.getCityName());
			holder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mOfflineMap.remove(bean.getCityCode());
					mData.remove(position);
					mAdapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView cityName;
			ImageView delete;

		}

	}
}
