package com.ntu.claw.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.adapter.SortAdapterCareI;
import com.ntu.claw.bean.CareIFriBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.CareIPinyinComparator;
import com.ntu.claw.utils.CharacterParser;
import com.ntu.claw.utils.L;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;
import com.ntu.claw.widget.SideBar;
import com.ntu.claw.widget.SideBar.OnTouchingLetterChangedListener;

public class CareIFragment extends Fragment {
	private static final String CAREITAG = "getcarei";
	private static final String DELETETAG = "deletecontact";
	private static final String VOLLEYTAG = "deletewatcher";

	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapterCareI adapter;

	// listviewΪ��ʱ��ʾ����
	private FrameLayout fl;
	/**
	 * ����ת����ƴ������
	 */
	private CharacterParser characterParser;
	private List<CareIFriBean> SourceDateList = new ArrayList<CareIFriBean>();// ����adapter����Դ
	private List<CareIFriBean> mSortList = new ArrayList<CareIFriBean>();;// �������ݿ���ȡ����ԭʼlist

	/**
	 * ����ƴ��������ListView�����������
	 */
	private CareIPinyinComparator pinyinComparator;

	// ������
	private EditText query;
	private ImageButton clearSearch;

	private String selfmobile;

	Dao dao = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_carei, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		dao = new Dao(getActivity());
		selfmobile = (String) SPUtils.get(getActivity(), "mobile", "");
		fl = (FrameLayout) getView().findViewById(R.id.id_nullnotify);
		initView();
		volleyGetCareI();
	}

	private void initView() {
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new CareIPinyinComparator();

		sideBar = (SideBar) getView().findViewById(R.id.sidebar);
		dialog = (TextView) getView().findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// �����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// ����ĸ�״γ��ֵ�λ��
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) getView()
				.findViewById(R.id.CareI_friend_list);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ����Ҫ����adapter.getItem(position)����ȡ��ǰposition����Ӧ�Ķ���
/*				T.showShort(getActivity().getApplicationContext(),
						((CareIFriBean) adapter.getItem(position))
								.getUsername());*/
			}
		});

		if (selfmobile != "") {
			// ���ݿ��л�ȡ��������
			mSortList = dao.getCareIList(selfmobile);
			SourceDateList.removeAll(SourceDateList);
			SourceDateList.addAll(mSortList);
			SourceDateList = filledData(SourceDateList);
		}

		// �б�����Ϊ��ʱ��ʾ����
		if (SourceDateList.isEmpty() || SourceDateList == null) {
			fl.setVisibility(View.VISIBLE);
		} else {
			fl.setVisibility(View.GONE);
		}

		// ����a-z��������Դ����
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapterCareI(this.getActivity(), SourceDateList);
		sortListView.setAdapter(adapter);
		registerForContextMenu(sortListView);

		/**
		 * ������
		 */
		query = (EditText) getView().findViewById(R.id.query);
		query.setHint("����");
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				filterData(s.toString());
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);

				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
			}
		});
	}

	/**
	 * �ӷ�������ȡ�����б�
	 */
	private void volleyGetCareI() {
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String jsonResult) {
						L.i(jsonResult);
						List<CareIFriBean> serverList = new ArrayList<CareIFriBean>();
						serverList = pauseJson(jsonResult);
						// �������list��ͬ������ȡ����������ͬ������б����浽���ݿ�
						if (compareList(serverList)) {
							L.i("list equals");
							return;
						} else {
							L.i("list unequals");
							dao.saveCareIList(serverList, selfmobile);
							SourceDateList.removeAll(SourceDateList);
							SourceDateList.addAll(serverList);
							SourceDateList = filledData(SourceDateList);
							Collections.sort(SourceDateList, pinyinComparator);
							adapter.notifyDataSetChanged();// �����б�
						}
						// �б�����Ϊ��ʱ��ʾ����
						if (SourceDateList.isEmpty() || SourceDateList == null) {
							fl.setVisibility(View.VISIBLE);
						} else {
							fl.setVisibility(View.GONE);
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						T.showShort(getActivity(), "���º����б�ʧ��" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", CAREITAG);
				map.put("mobile", selfmobile);
				return map;
			}
		};
		request.setTag(CAREITAG);
		MyApplication.getHttpQueues().add(request);
	}

	/**
	 * ����������������json
	 * 
	 * @param jsonResult
	 * @return
	 */
	protected List<CareIFriBean> pauseJson(String jsonResult) {
		List<CareIFriBean> list = new ArrayList<CareIFriBean>();
		try {
			JSONArray arr = new JSONArray(jsonResult);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject item = arr.getJSONObject(i);
				CareIFriBean bean = new CareIFriBean();
				bean.setMobile(item.optString("mobile",""));
				bean.setUsername(item.optString("username",""));
				bean.setAvater(item.optString("avater", ""));
				list.add(bean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * �ȽϷ�������������list�����ݿ��ȡ��list�Ƿ���ͬ
	 * 
	 * @param serverList
	 * @return
	 */
	private boolean compareList(List<CareIFriBean> serverList) {
		if (serverList.size() != mSortList.size()) {
			return false;
		} else {
			for (int i = 0; i < serverList.size(); i++) {
				if ((!serverList.get(i).getMobile()
						.equals(mSortList.get(i).getMobile()))
						|| (!serverList.get(i).getUsername()
								.equals(mSortList.get(i).getUsername()))
						|| (!serverList.get(i).getAvater()
								.equals(mSortList.get(i).getAvater())))
					return false;
			}
			return true;
		}
	}

	/**
	 * ΪListView�������
	 * 
	 * @param date
	 * @return
	 */
	private List<CareIFriBean> filledData(List<CareIFriBean> mList) {
		for (int i = 0; i < mList.size(); i++) {
			// ����ת����ƴ��
			String pinyin = characterParser.getSelling(mList.get(i)
					.getUsername());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if (sortString.matches("[A-Z]")) {
				mList.get(i).setSortLetters(sortString.toUpperCase());
			} else {
				mList.get(i).setSortLetters("#");
			}
		}
		return mList;
	}

	/**
	 * ����������е�ֵ���������ݲ�����ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<CareIFriBean> filterDateList = new ArrayList<CareIFriBean>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (CareIFriBean sortModel : SourceDateList) {
				String name = sortModel.getUsername();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// ����a-z��������
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	// ɾ������ϵ��
	private CareIFriBean toDeleteWatcher;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		toDeleteWatcher = adapter
				.getItem(((AdapterContextMenuInfo) menuInfo).position);
		getActivity().getMenuInflater().inflate(R.menu.context_contact_list,
				menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			deleteContact(toDeleteWatcher);
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * ɾ����ϵ��
	 * 
	 * @param toDeleteUser
	 */
	private void deleteContact(final CareIFriBean toDeleteWatcher) {
		final ProgressDialog pd = new ProgressDialog(getActivity(),
				ProgressDialog.THEME_HOLO_LIGHT);
		pd.setMessage("ɾ����...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String result) {
						pd.dismiss();
						if (result.equals("success")) {
							dao.deleteCareI(selfmobile,
									toDeleteWatcher.getMobile());
							SourceDateList.remove(toDeleteWatcher);
							adapter.notifyDataSetChanged();
						}
						if (result.equals("fail")) {
							T.showShort(getActivity(), "ɾ��ʧ��");
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(getActivity(), "����ʧ��" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", DELETETAG);
				map.put("usermobile", selfmobile);
				map.put("watchermobile", toDeleteWatcher.getMobile());
				return map;
			}
		};
		request.setTag(VOLLEYTAG);
		MyApplication.getHttpQueues().add(request);
	}

	@Override
	public void onStop() {
		super.onStop();
		MyApplication.getHttpQueues().cancelAll(CAREITAG);
		MyApplication.getHttpQueues().cancelAll(VOLLEYTAG);
	}
}
