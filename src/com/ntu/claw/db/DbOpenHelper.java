package com.ntu.claw.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME="claw.db";
	private static DbOpenHelper instance;

/*	private static final String CREATE_TABLE_SELFINFO = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_SELF + " ("
			+ Dao.COLUMN_SELFMOBILE + " TEXT PRIMARY KEY, "
			+ Dao.COLUMN_SELFNAME + " TEXT);";*/
	
	private static final String CREATE_TABLE_ICARE = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_ICARE + " ("
			+ Dao.COLUMN_FRIENDMOBILE + " TEXT PRIMARY KEY, "
			+ Dao.COLUMN_FRIENDNAME + " TEXT, "
			+ Dao.COLUMN_AVATER + " TEXT, "
			+ Dao.COLUMN_SELFMOBILE + " TEXT);";
	
	private static final String CREATE_TABLE_CAREI = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_CAREI + " ("
			+ Dao.COLUMN_FRIENDMOBILE + " TEXT PRIMARY KEY, "
			+ Dao.COLUMN_FRIENDNAME + " TEXT, "
			+ Dao.COLUMN_AVATER + " TEXT, "
			+ Dao.COLUMN_SELFMOBILE + " TEXT);";
	
	private static final String CREATE_TABLE_FRIASK = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_FRIASK + " ("
			+ Dao.COLUMN_FRIENDMOBILE + " TEXT, "
			+ Dao.COLUMN_FRIENDNAME + " TEXT, "
			+ Dao.COLUMN_SELFMOBILE + " TEXT, "
			+ Dao.COLUMN_AVATER + " TEXT, "
			+ Dao.COLUMN_ISAGREE +" TEXT);";
	
	private static final String CREATE_TABLE_TRACE = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_TRACE + " ("
			+ Dao.COLUMN_TRACEID + " TEXT PRIMARY KEY, "
			+ Dao.COLUMN_USERID + " TEXT, "
			+ Dao.COLUMN_STARTTIME + " TEXT, "
			+ Dao.COLUMN_ENDTIME + " TEXT, "
			+ Dao.COLUMN_DISTANCE +" TEXT);";
	
	private static final String CREATE_TABLE_RECORD = "CREATE TABLE IF NOT EXISTS "
			+ Dao.TABLE_RECORD + " ("
			+ Dao.COLUMN_TRACEID + " TEXT, "
/*			+ Dao.COLUMN_USERID +" TEXT, "*/
			+ Dao.COLUMN_LONGITUDE +" TEXT, "
			+ Dao.COLUMN_LATITUDE +" TEXT, "
			+ Dao.COLUMN_TIME +" TEXT, "
			+ Dao.COLUMN_ALTITUDE + " TEXT);";
	
	public DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * ≥ı ºΩ®±Ì
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL(CREATE_TABLE_SELFINFO);
		db.execSQL(CREATE_TABLE_ICARE);
		db.execSQL(CREATE_TABLE_CAREI);
		db.execSQL(CREATE_TABLE_FRIASK);
		db.execSQL(CREATE_TABLE_TRACE);
		db.execSQL(CREATE_TABLE_RECORD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}
	
	public static DbOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DbOpenHelper(context.getApplicationContext());
		}
		return instance;
	}
	
	public void closeDB() {
	    if (instance != null) {
	        try {
	            SQLiteDatabase db = instance.getWritableDatabase();
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        instance = null;
	    }
	}

}
