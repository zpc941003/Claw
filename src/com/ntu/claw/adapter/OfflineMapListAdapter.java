package com.ntu.claw.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntu.claw.R;
import com.ntu.claw.activity.OffLineMapActivity;
import com.ntu.claw.bean.OfflineMapCityBean;
import com.ntu.claw.bean.OfflineMapCityBean.Flag;

public class OfflineMapListAdapter extends BaseExpandableListAdapter {

	public List<OfflineMapCityBean> mGroupList;
    public Map<OfflineMapCityBean, List<OfflineMapCityBean>> mChildList;

    private Context mContext;

    public OfflineMapListAdapter(Context context, List<OfflineMapCityBean> groupList,
            Map<OfflineMapCityBean, List<OfflineMapCityBean>> childList) {
        mContext = context;
        mGroupList = groupList;
        mChildList = childList;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildList.get(getGroup(groupPosition)).get(childPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Object group = getGroup(groupPosition);
        return mChildList.get(group) == null ? 0 : mChildList.get(group).size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view,
            ViewGroup parent) {
        OfflineMapCityBean o = (OfflineMapCityBean) getGroup(groupPosition);
        view = LayoutInflater.from(mContext).inflate(R.layout.list_map_offline_city_header,
                null);
        // init view
        TextView txCityGroupName = (TextView) view.findViewById(R.id.tx_city_group_name);
        txCityGroupName.setText(o.getCityName());

        return view;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View view, ViewGroup parent) {
        final OfflineMapCityBean o = (OfflineMapCityBean) getChild(groupPosition, childPosition);
        view = LayoutInflater.from(mContext).inflate(R.layout.list_map_offline_city_content,
                null);
        // init view
        TextView txCityName = (TextView) view.findViewById(R.id.tx_city_name);
        TextView txSize = (TextView) view.findViewById(R.id.tx_size);
        TextView txProgress = (TextView) view.findViewById(R.id.tx_progress);
        ImageView imgDown = (ImageView) view.findViewById(R.id.img_down);
        // set data
        view.findViewById(R.id.layout_down).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(o.getProgress() >=100){
                    return;
                }
                
                int cityId = o.getCityCode();
                OffLineMapActivity holder = ((OffLineMapActivity) mContext);
                if (holder.isDownloadOnTheTask(cityId)) {
                    o.setFlag(Flag.NO_STATUS);
                    holder.removeTaskFromQueue(cityId);
                } else {
                    o.setFlag(Flag.PAUSE);
                    holder.addToDownloadQueue(cityId);

                }

                OfflineMapListAdapter.this.notifyDataSetChanged();
            }
        });

        imgDown.clearAnimation();
        imgDown.setVisibility(View.GONE);

        txCityName.setText(o.getCityName());
        Float sizeMB = o.getSize() / (1024f * 1024);
        if (sizeMB.toString().length() > 5)
            txSize.setText(sizeMB.toString().substring(0, 4) + "MB");
        else
            txSize.setText(sizeMB.toString() + "MB");

        int progress = o.getProgress();
        if (progress == 0) {
            imgDown.setVisibility(View.VISIBLE);
            imgDown.setTag("init");
            //imgDown.setImageResource(R.drawable.download);
        } else if (progress == 100) {
            o.setFlag(Flag.NO_STATUS);
            imgDown.setTag("over");
            imgDown.setVisibility(View.GONE);
            txProgress.setTextSize(15f);
            txProgress.setText("ÒÑÏÂÔØ");
        } else {
            imgDown.setVisibility(View.GONE);
            //imgDown.setImageResource(R.drawable.reflash_999);
            txProgress.setText(progress + "%");
        }

        if (o.getFlag().equals(Flag.PAUSE)) {
            imgDown.setTag("pause");
            imgDown.clearAnimation();
            imgDown.setVisibility(View.GONE);
            txProgress.setTextSize(15f);
            txProgress.setText("µÈ´ý");
        } else if (o.getFlag().equals(Flag.DOWNLOADING)) {
            imgDown.setVisibility(View.GONE);
            imgDown.setTag("start");
            //imgDown.setImageResource(R.drawable.reflash_999);
            //imgDown.startAnimation(
                    //AnimationUtils.loadAnimation(mContext, R.anim.rotate_forever));
            txProgress.setTextSize(11f);
        }
        return view;

    }

}
