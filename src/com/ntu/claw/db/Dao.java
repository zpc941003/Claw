package com.ntu.claw.db;

import java.util.List;

import android.content.Context;

import com.baidu.mapapi.model.LatLng;
import com.ntu.claw.bean.CareIFriBean;
import com.ntu.claw.bean.FriAskBean;
import com.ntu.claw.bean.ICareFriBean;
import com.ntu.claw.bean.RecordBean;
import com.ntu.claw.bean.TraceBean;

public class Dao {

	/**
	 * 表名
	 */
	public static final String TABLE_ICARE = "tb_icare";
	public static final String TABLE_CAREI = "tb_carei";
	public static final String TABLE_FRIASK = "tb_friask";

	public static final String TABLE_TRACE = "tb_trace";
	public static final String TABLE_RECORD = "tb_record";

	/**
	 * 列名
	 */
	public static final String COLUMN_SELFNAME = "username";
	public static final String COLUMN_FRIENDMOBILE = "friendmobile";
	public static final String COLUMN_FRIENDNAME = "friendname";
	public static final String COLUMN_SELFMOBILE = "selfmobile";
	public static final String COLUMN_ISAGREE = "isagree";
	public static final String COLUMN_AVATER = "avater";

	public static final String COLUMN_TRACEID = "trace_id";
	public static final String COLUMN_USERID = "user_id";
	public static final String COLUMN_STARTTIME = "starttime";
	public static final String COLUMN_ENDTIME = "endtime";
	public static final String COLUMN_DISTANCE = "distance";

	public static final String COLUMN_RECORDID = "record_id";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_ALTITUDE = "altitude";
	public static final String COLUMN_TIME = "locationtime";

	public Dao(Context context) {
		DbManager.getInstance().onInit(context);
	}

	public List<ICareFriBean> getICareList(String selfmobile) {
		return DbManager.getInstance().getICareList(selfmobile);
	}

	public List<CareIFriBean> getCareIList(String selfmobile) {
		return DbManager.getInstance().getCareIList(selfmobile);
	}

	public void saveICareList(List<ICareFriBean> list, String selfmobile) {
		DbManager.getInstance().saveICareList(list, selfmobile);
	}

	public void saveCareIList(List<CareIFriBean> list, String selfmobile) {
		DbManager.getInstance().saveCareIList(list, selfmobile);
	}

	public boolean findSameMobile(String mobile, String selfmobile) {
		return DbManager.getInstance().findSameMobile(mobile, selfmobile);
	}

	public void saveFriAsk(String selfmobile, String mobile, String friendName,String avater) {
		DbManager.getInstance().saveFriAsk(selfmobile, mobile, friendName,avater);
	}

	public List<FriAskBean> getFriAsk(String selfmobile) {
		return DbManager.getInstance().getFriAsk(selfmobile);
	}

	public void agreeFriask(String selfmobile, String mobile) {
		DbManager.getInstance().agreeFriask(selfmobile, mobile);
	}

	public void clearFriAskList(String selfmobile) {
		DbManager.getInstance().clearFriAskList(selfmobile);
	}

	public void deleteICare(String selfmobile, String mobile) {
		DbManager.getInstance().deleteICare(selfmobile, mobile);
	}

	public void deleteCareI(String selfmobile, String mobile) {
		DbManager.getInstance().deleteCareI(selfmobile, mobile);
	}

	public void addTrace(String trace_id, String user_id, String starttime) {
		DbManager.getInstance().addTrace(trace_id, user_id, starttime);
	}

	public void addRecord(RecordBean record) {
		DbManager.getInstance().addRecord(record);
	}

	public List<LatLng> getRecording(String trace_id) {
		return DbManager.getInstance().getRecording(trace_id);
	}

	public String getStarttime(String trace_id) {
		return DbManager.getInstance().getStarttime(trace_id);
	}

	public void endRecord(String trace_id, String endtime, String distance) {
		DbManager.getInstance().endRecord(trace_id, endtime, distance);
	}

	public List<TraceBean> getTrace(String mobile) {
		return DbManager.getInstance().getTrace(mobile);
	}

	public TraceBean getOneTrace(String trace_id) {
		return DbManager.getInstance().getOneTrace(trace_id);
	}

	public List<LatLng> getTraceRecord(String trace_id) {
		return DbManager.getInstance().getTraceRecord(trace_id);
	}

	public void updateAllTraceRecord(String mobile, List<TraceBean> listTrace,
			List<RecordBean> listRecord) {
		DbManager.getInstance().updateAllTraceRecord(mobile, listTrace,
				listRecord);
	}
	
	public void deleteTrace(String trace_id){
		DbManager.getInstance().deleteTrace(trace_id);
	}
	
	public void updateMobile(String updateMobile, String mobile){
		DbManager.getInstance().updateMobile(updateMobile, mobile);
	}
	
	public String getAvaterUrl(String mobile){
		return DbManager.getInstance().getAvaterUrl(mobile);
	}
}
