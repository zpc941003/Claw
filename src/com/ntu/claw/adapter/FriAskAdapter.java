package com.ntu.claw.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ntu.claw.MyApplication;
import com.ntu.claw.R;
import com.ntu.claw.bean.FriAskBean;
import com.ntu.claw.db.Dao;
import com.ntu.claw.utils.SPUtils;
import com.ntu.claw.utils.T;

public class FriAskAdapter extends BaseAdapter {

	private List<FriAskBean> mlist = null;
	private Context mContext;

	public static final String TAG = "agreefriask";

	public FriAskAdapter(Context mContext, List<FriAskBean> list) {
		this.mContext = mContext;
		this.mlist = list;
	}

	/**
	 * 自定义接口，用于回调按钮点击事件到Activity
	 */
	public interface Callback {
		public void click(View v);
	}

	@Override
	public int getCount() {
		return this.mlist.size();
	}

	@Override
	public FriAskBean getItem(int position) {
		return mlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		final ViewHolder viewHolder;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.row_invite_msg, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.newFriname);
			viewHolder.status = (Button) view.findViewById(R.id.user_state);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		final FriAskBean bean = getItem(position);
		viewHolder.tvTitle.setText(bean.getUsername());
		if (bean.getStatus().equals("agree")) {
			viewHolder.status.setText("已同意");
			viewHolder.status.setBackgroundDrawable(null);
			viewHolder.status.setEnabled(false);
		}
		/**
		 * 同意请求
		 */
		viewHolder.status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				System.out.println("1");
				acceptInvitation(viewHolder.status, bean);
			}
		});
		return view;
	}

	protected void acceptInvitation(final Button status, final FriAskBean bean) {
		System.out.println("2");
		final ProgressDialog pd = new ProgressDialog(mContext);
		pd.setMessage("同意中...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		final String selfmobile = (String) SPUtils.get(mContext, "mobile", "");
		String url = MyApplication.ipAddress
				+ "/ClawServer/servlet/ManageServlet";
		StringRequest request = new StringRequest(Method.POST, url,
				new Listener<String>() {
					@Override
					public void onResponse(String result) {
						pd.dismiss();
						if (result.equals("success")) {
							Dao dao=new Dao(mContext);
							dao.agreeFriask(selfmobile, bean.getMobile());
							status.setText("已同意");
							status.setBackgroundDrawable(null);
							status.setEnabled(false);
						}
						if(result.equals("same")){
							T.showShort(mContext, "你已同意过了");
						}
						if(result.equals("fail")){
							T.showShort(mContext, "同意失败");
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						pd.dismiss();
						T.showShort(mContext, "连接失败" + arg0.toString());
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("action", TAG);
				map.put("usermobile", selfmobile);
				map.put("watchermobile", bean.getMobile());
				return map;
			}
		};
		request.setTag(TAG);
		MyApplication.getHttpQueues().add(request);
	}

	private static class ViewHolder {
		TextView tvTitle;
		Button status;
	}
}
