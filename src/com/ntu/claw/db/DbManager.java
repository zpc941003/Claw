package com.ntu.claw.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.ntu.claw.bean.CareIFriBean;
import com.ntu.claw.bean.FriAskBean;
import com.ntu.claw.bean.ICareFriBean;
import com.ntu.claw.bean.RecordBean;
import com.ntu.claw.bean.TraceBean;

public class DbManager {

	static private DbManager dbMgr = new DbManager();
	private DbOpenHelper dbHelper;

	void onInit(Context context) {
		dbHelper = DbOpenHelper.getInstance(context);
	}

	/**
	 * 保证只有一个线程访问数据库
	 * 
	 * @return
	 */
	public static synchronized DbManager getInstance() {
		return dbMgr;
	}

	synchronized public void closeDB() {
		if (dbHelper != null) {
			dbHelper.closeDB();
		}
	}

	/**
	 * 获取我关注的好友列表
	 * 
	 * @param selfmobile
	 * @return
	 */
	synchronized public List<ICareFriBean> getICareList(String selfmobile) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<ICareFriBean> list = new ArrayList<ICareFriBean>();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_ICARE
					+ " where " + Dao.COLUMN_SELFMOBILE + "=" + selfmobile,
					null);
			while (cursor.moveToNext()) {
				ICareFriBean acfb = new ICareFriBean();
				acfb.setUsername(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDNAME)));
				acfb.setMobile(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDMOBILE)));
				acfb.setAvater(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_AVATER)));
				list.add(acfb);
			}
			cursor.close();
			db.close();
		}

		return list;
	}

	/**
	 * 获取关注我的好友列表
	 * 
	 * @param selfmobile
	 * @return
	 */
	synchronized public List<CareIFriBean> getCareIList(String selfmobile) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<CareIFriBean> list = new ArrayList<CareIFriBean>();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_CAREI
					+ " where " + Dao.COLUMN_SELFMOBILE + "=" + selfmobile,
					null);
			while (cursor.moveToNext()) {
				CareIFriBean cifb = new CareIFriBean();
				cifb.setUsername(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDNAME)));
				cifb.setMobile(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDMOBILE)));
				cifb.setAvater(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_AVATER)));
				list.add(cifb);
			}
			cursor.close();
			db.close();
		}

		return list;
	}

	/**
	 * 保存我关注的好友列表
	 * 
	 * @param list
	 * @param selfmobile
	 */
	synchronized public void saveICareList(List<ICareFriBean> list,
			String selfmobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_ICARE, "selfmobile=?",
					new String[] { selfmobile });
			for (ICareFriBean iCareFriBean : list) {
				ContentValues values = new ContentValues();
				values.put(Dao.COLUMN_FRIENDMOBILE, iCareFriBean.getMobile());
				values.put(Dao.COLUMN_FRIENDNAME, iCareFriBean.getUsername());
				values.put(Dao.COLUMN_AVATER, iCareFriBean.getAvater());
				values.put(Dao.COLUMN_SELFMOBILE, selfmobile);
				db.insert(Dao.TABLE_ICARE, null, values);
			}
			db.close();
		}
	}

	/**
	 * 保存关注我的好友列表
	 * 
	 * @param list
	 * @param selfmobile
	 */
	synchronized public void saveCareIList(List<CareIFriBean> list,
			String selfmobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_CAREI, "selfmobile=?",
					new String[] { selfmobile });
			for (CareIFriBean careIFriBean : list) {
				ContentValues values = new ContentValues();
				values.put(Dao.COLUMN_FRIENDMOBILE, careIFriBean.getMobile());
				values.put(Dao.COLUMN_FRIENDNAME, careIFriBean.getUsername());
				values.put(Dao.COLUMN_AVATER, careIFriBean.getAvater());
				values.put(Dao.COLUMN_SELFMOBILE, selfmobile);
				db.insert(Dao.TABLE_CAREI, null, values);
			}
			db.close();
		}
	}

	/**
	 * 查找是否已经是我关注的人
	 * 
	 * @param mobile
	 * @return
	 */
	synchronized public boolean findSameMobile(String mobile, String selfmobile) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		boolean flag = false;
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_ICARE
					+ " where " + Dao.COLUMN_FRIENDMOBILE + "=" + mobile
					+ " and " + Dao.COLUMN_SELFMOBILE + "=" + selfmobile, null);
			if (cursor.moveToNext()) {
				flag = true;
			}
			cursor.close();
			db.close();
		}
		return flag;
	}

	/**
	 * 插入一条好友请求记录
	 * 
	 * @param selfmobile
	 * @param mobile
	 * @param friendName
	 */
	synchronized public void saveFriAsk(String selfmobile, String mobile,
			String friendName) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(Dao.COLUMN_FRIENDMOBILE, mobile);
			values.put(Dao.COLUMN_FRIENDNAME, friendName);
			values.put(Dao.COLUMN_SELFMOBILE, selfmobile);
			values.put(Dao.COLUMN_ISAGREE, "disagree");
			db.insert(Dao.TABLE_FRIASK, null, values);
			db.close();
		}
	}

	/**
	 * 获取所有好友请求记录
	 * 
	 * @param selfmobile
	 */
	synchronized public List<FriAskBean> getFriAsk(String selfmobile) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<FriAskBean> list = new ArrayList<FriAskBean>();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_FRIASK
					+ " where " + Dao.COLUMN_SELFMOBILE + "=" + selfmobile,
					null);
			while (cursor.moveToNext()) {
				FriAskBean fab = new FriAskBean();
				fab.setUsername(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDNAME)));
				fab.setMobile(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_FRIENDMOBILE)));
				fab.setStatus(cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_ISAGREE)));
				list.add(fab);
			}
			db.close();
		}
		return list;
	}

	/**
	 * 同意请求
	 * 
	 * @param selfmobile
	 * @param mobile
	 */
	synchronized public void agreeFriask(String selfmobile, String mobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(Dao.COLUMN_ISAGREE, "agree");
			db.update(Dao.TABLE_FRIASK, values, Dao.COLUMN_SELFMOBILE
					+ "=? and " + Dao.COLUMN_FRIENDMOBILE + "=?", new String[] {
					selfmobile, mobile });
			db.close();
		}
	}

	/**
	 * 清空好友请求列表
	 * 
	 * @param selfmobile
	 */
	synchronized public void clearFriAskList(String selfmobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_FRIASK, "selfmobile=?",
					new String[] { selfmobile });
			db.close();
		}
	}

	/**
	 * 删除我关注的人
	 * 
	 * @param selfmobile
	 * @param mobile
	 */
	synchronized public void deleteICare(String selfmobile, String mobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_ICARE, Dao.COLUMN_SELFMOBILE + "=? and "
					+ Dao.COLUMN_FRIENDMOBILE + "=?", new String[] {
					selfmobile, mobile });
			db.close();
		}
	}

	/**
	 * 删除关注我的人
	 * 
	 * @param selfmobile
	 * @param mobile
	 */
	synchronized public void deleteCareI(String selfmobile, String mobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_CAREI, Dao.COLUMN_SELFMOBILE + "=? and "
					+ Dao.COLUMN_FRIENDMOBILE + "=?", new String[] {
					selfmobile, mobile });
			db.close();
		}
	}

	/**
	 * 点击开始记录插入一条轨迹数据
	 * 
	 * @param trace_id
	 * @param user_id
	 * @param starttime
	 */
	synchronized public void addTrace(String trace_id, String user_id,
			String starttime) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(Dao.COLUMN_TRACEID, trace_id);
			values.put(Dao.COLUMN_USERID, user_id);
			values.put(Dao.COLUMN_STARTTIME, starttime);
			db.insert(Dao.TABLE_TRACE, null, values);
			db.close();
		}
	}

	/**
	 * 插入一条定位数据
	 * 
	 * @param record
	 */
	synchronized public void addRecord(RecordBean record) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(Dao.COLUMN_TRACEID, record.getTrace_id());
			// values.put(Dao.COLUMN_USERID, record.getUser_id());
			values.put(Dao.COLUMN_LONGITUDE, record.getLongitude());
			values.put(Dao.COLUMN_LATITUDE, record.getLatitude());
			values.put(Dao.COLUMN_TIME, record.getLocationtime());
			values.put(Dao.COLUMN_ALTITUDE, record.getAltitude());
			db.insert(Dao.TABLE_RECORD, null, values);
			db.close();
		}
	}

	/**
	 * 重新进入地图界面加载过去轨迹
	 * 
	 * @param trace_id
	 * @return
	 */
	synchronized public List<LatLng> getRecording(String trace_id) {
		List<LatLng> list = new ArrayList<LatLng>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_RECORD
					+ " where " + Dao.COLUMN_TRACEID + "='" + trace_id
					+ "' order by " + Dao.COLUMN_TIME, null);
			while (cursor.moveToNext()) {
				LatLng latlng = new LatLng(
						Double.parseDouble(cursor.getString(cursor
								.getColumnIndex(Dao.COLUMN_LATITUDE))),
						Double.parseDouble(cursor.getString(cursor
								.getColumnIndex(Dao.COLUMN_LONGITUDE))));
				list.add(latlng);
			}
			cursor.close();
			db.close();
		}
		return list;
	}

	/**
	 * 重新进入地图界面加载开始时间
	 * 
	 * @param trace_id
	 * @return
	 */
	synchronized public String getStarttime(String trace_id) {
		String starttime = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_TRACE
					+ " where " + Dao.COLUMN_TRACEID + "='" + trace_id + "'",
					null);
			if (cursor.moveToNext()) {
				starttime = cursor.getString(cursor
						.getColumnIndex(Dao.COLUMN_STARTTIME));
			}
			cursor.close();
			db.close();
		}
		return starttime;
	}

	/**
	 * 结束记录，插入结束时间和距离
	 * 
	 * @param trace_id
	 * @param endtime
	 * @param distance
	 */
	synchronized public void endRecord(String trace_id, String endtime,
			String distance) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("update " + Dao.TABLE_TRACE + " set "
					+ Dao.COLUMN_ENDTIME + "='" + endtime + "',"
					+ Dao.COLUMN_DISTANCE + "='" + distance + "' where "
					+ Dao.COLUMN_TRACEID + "='" + trace_id + "'");
			db.close();
		}
	}

	/**
	 * 获取所有trace
	 * 
	 * @param mobile
	 * @return
	 */
	synchronized public List<TraceBean> getTrace(String mobile) {
		List<TraceBean> list = new ArrayList<TraceBean>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor c = db.rawQuery("select * from " + Dao.TABLE_TRACE
					+ " where " + Dao.COLUMN_USERID + "='" + mobile + "' and "
					+ Dao.COLUMN_ENDTIME + " is not null order by "
					+ Dao.COLUMN_STARTTIME + " desc", null);
			if (c != null) {
				while (c.moveToNext()) {
					TraceBean item = new TraceBean();
					item.setTrace_id(c.getString(c
							.getColumnIndex(Dao.COLUMN_TRACEID)));
					item.setStarttime(c.getString(c
							.getColumnIndex(Dao.COLUMN_STARTTIME)));
					item.setEndtime(c.getString(c
							.getColumnIndex(Dao.COLUMN_ENDTIME)));
					item.setDistance(c.getString(c
							.getColumnIndex(Dao.COLUMN_DISTANCE)));
					list.add(item);
				}
				c.close();
			}
			db.close();
		}
		return list;
	}

	/**
	 * 获取一条trace
	 * 
	 * @param trace_id
	 * @return
	 */
	synchronized public TraceBean getOneTrace(String trace_id) {
		TraceBean item = new TraceBean();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor c = db.rawQuery("select * from " + Dao.TABLE_TRACE
					+ " where " + Dao.COLUMN_TRACEID + "='" + trace_id + "'",
					null);
			if (c != null) {
				if (c.moveToNext()) {
					item.setTrace_id(c.getString(c
							.getColumnIndex(Dao.COLUMN_TRACEID)));
					item.setStarttime(c.getString(c
							.getColumnIndex(Dao.COLUMN_STARTTIME)));
					item.setEndtime(c.getString(c
							.getColumnIndex(Dao.COLUMN_ENDTIME)));
					item.setDistance(c.getString(c
							.getColumnIndex(Dao.COLUMN_DISTANCE)));
				}
				c.close();
			}
			db.close();
		}
		return item;
	}

	/**
	 * 获取一条轨迹所有经纬度点
	 * 
	 * @param trace_id
	 * @return
	 */
	synchronized public List<LatLng> getTraceRecord(String trace_id) {
		List<LatLng> list = new ArrayList<LatLng>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + Dao.TABLE_RECORD
					+ " where " + Dao.COLUMN_TRACEID + "='" + trace_id
					+ "' order by " + Dao.COLUMN_TIME, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					LatLng latlng = new LatLng(Double.parseDouble(cursor
							.getString(cursor
									.getColumnIndex(Dao.COLUMN_LATITUDE))),
							Double.parseDouble(cursor.getString(cursor
									.getColumnIndex(Dao.COLUMN_LONGITUDE))));
					list.add(latlng);
				}
				cursor.close();
			}
			db.close();
		}
		return list;
	}

	/**
	 * 更新所有trace和record
	 * 
	 * @param mobile
	 * @param listTrace
	 * @param listRecord
	 */
	synchronized public void updateAllTraceRecord(String mobile,
			List<TraceBean> listTrace, List<RecordBean> listRecord) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_TRACE, "user_id=?", new String[] { mobile });
			for (TraceBean tb : listTrace) {
				ContentValues values = new ContentValues();
				values.put(Dao.COLUMN_TRACEID, tb.getTrace_id());
				values.put(Dao.COLUMN_USERID, tb.getUser_id());
				values.put(Dao.COLUMN_STARTTIME, tb.getStarttime());
				values.put(Dao.COLUMN_ENDTIME, tb.getEndtime());
				values.put(Dao.COLUMN_DISTANCE, tb.getDistance());
				db.insert(Dao.TABLE_TRACE, null, values);
			}
			// List<TraceBean> list=new ArrayList<TraceBean>();
			// String[] arr=new String[list.size()];
			// for (int i = 0; i < arr.length; i++) {
			// arr[i]=list.get(i).getTrace_id();
			// }
			// db.delete(Dao.TABLE_RECORD, "trace_id=?", arr);
			db.execSQL("delete from " + Dao.TABLE_RECORD + " where "
					+ Dao.COLUMN_TRACEID + " in(select " + Dao.COLUMN_TRACEID
					+ " from " + Dao.TABLE_TRACE + " where "
					+ Dao.COLUMN_USERID + "='" + mobile + "')");
			for (RecordBean rb : listRecord) {
				ContentValues values = new ContentValues();
				values.put(Dao.COLUMN_TRACEID, rb.getTrace_id());
				// values.put(Dao.COLUMN_USERID, rb.getUser_id());
				values.put(Dao.COLUMN_LONGITUDE, rb.getLongitude());
				values.put(Dao.COLUMN_LATITUDE, rb.getLatitude());
				values.put(Dao.COLUMN_ALTITUDE, rb.getAltitude());
				values.put(Dao.COLUMN_TIME, rb.getLocationtime());
				db.insert(Dao.TABLE_RECORD, null, values);
			}
			db.close();
		}
	}

	/**
	 * 删除轨迹
	 * 
	 * @param trace_id
	 */
	synchronized public void deleteTrace(String trace_id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(Dao.TABLE_TRACE, "trace_id=?", new String[] { trace_id });
			db.delete(Dao.TABLE_RECORD, "trace_id=?", new String[] { trace_id });
			db.close();
		}
	}

	/**
	 * 更新手机号
	 * @param updateMobile
	 * @param mobile
	 */
	synchronized public void updateMobile(String updateMobile, String mobile) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("update " + Dao.TABLE_CAREI + " set "
					+ Dao.COLUMN_SELFMOBILE + "='" + updateMobile + "' where "
					+ Dao.COLUMN_SELFMOBILE + "='" + mobile + "';");
			db.execSQL("update " + Dao.TABLE_ICARE + " set "
					+ Dao.COLUMN_SELFMOBILE + "='" + updateMobile + "' where "
					+ Dao.COLUMN_SELFMOBILE + "='" + mobile + "';");
			db.execSQL("update " + Dao.TABLE_FRIASK + " set "
					+ Dao.COLUMN_SELFMOBILE + "='" + updateMobile + "' where "
					+ Dao.COLUMN_SELFMOBILE + "='" + mobile + "';");
			db.execSQL("update " + Dao.TABLE_TRACE + " set "
					+ Dao.COLUMN_USERID + "='" + updateMobile + "' where "
					+ Dao.COLUMN_USERID + "='" + mobile + "';");
			db.close();
		}
	}
}
