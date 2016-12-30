package com.ntu.claw.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.ntu.claw.R;
import com.ntu.claw.bean.CareIFriBean;

public class SortAdapterCareI extends BaseAdapter implements SectionIndexer{
	private List<CareIFriBean> list = null;
	private Context mContext;
	private RequestQueue queue;
	private ImageLoader imageLoader;
	
	public SortAdapterCareI(Context mContext, List<CareIFriBean> list) {
		this.mContext = mContext;
		this.list = list;
		queue = Volley.newRequestQueue(mContext);
		imageLoader = new ImageLoader(queue,
				new com.ntu.claw.cache.BitmapCache(mContext));
	}
	
	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<CareIFriBean> list){
		this.list = list;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public CareIFriBean getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final CareIFriBean mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.row_careifriend, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.carei_username);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.imgAvater=(ImageView) view.findViewById(R.id.avatar);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		
		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		}else{
			viewHolder.tvLetter.setVisibility(View.GONE);
		}
	
		viewHolder.tvTitle.setText(this.list.get(position).getUsername());
		ImageListener listener = ImageLoader.getImageListener(
				viewHolder.imgAvater, R.drawable.default_useravatar,
				R.drawable.default_useravatar);
		imageLoader.get(this.list.get(position).getAvater(), listener);
		return view;
	}
	
	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		ImageView imgAvater;
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	@Override
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	@Override
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

}
