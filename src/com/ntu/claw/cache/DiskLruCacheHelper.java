package com.ntu.claw.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import libcore.io.DiskLruCache;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.ntu.claw.utils.MD5;

public class DiskLruCacheHelper {

	private DiskLruCache mDiskLruCache = null; 
	private static DiskLruCacheHelper instance;
	
	private static final String UNIQUENAME="bitmap";
	
	/**
	 * ��DiskLruCache
	 * @param context
	 * @param uniqueName  �����ļ�����
	 */
	private DiskLruCacheHelper(Context context){
		try {  
		    File cacheDir = getDiskCacheDir(context, UNIQUENAME);  
		    if (!cacheDir.exists()) {  
		        cacheDir.mkdirs();  
		    }  
		    mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);  
		} catch (IOException e) {  
		    e.printStackTrace();  
		}  
	}
	
	private DiskLruCacheHelper(Context context, String uniqueName){
		try {  
		    File cacheDir = getDiskCacheDir(context, uniqueName);  
		    if (!cacheDir.exists()) {  
		        cacheDir.mkdirs();  
		    }  
		    mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);  
		} catch (IOException e) {  
		    e.printStackTrace();  
		}  
	}
	
	public static DiskLruCacheHelper getInstance(Context context) {  
        if (instance == null)  
        	instance = new DiskLruCacheHelper(context);  
        return instance;  
    }  
	
	public static DiskLruCacheHelper getInstance(Context context,String uniqueName) {  
        if (instance == null)  
        	instance = new DiskLruCacheHelper(context,uniqueName);  
        return instance;  
    }
	
	
    /** 
     * д��ͼƬ���ݵ��ļ����� 
     * 
     * @param imageUrl 
     * @param bitmap 
     */  
    public void writeToCache(String imageUrl, Bitmap bitmap) {  
        try {  
            String key = MD5.getMD5(imageUrl);  
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);  
            if (editor != null) {  
                OutputStream outputStream = editor.newOutputStream(0);  
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {  
                    editor.commit();  
                } else {  
                    editor.abort();  
                }  
  
            }  
            mDiskLruCache.flush();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
  
    /** 
     * �ӻ����ȡ���� 
     * 
     * @param imageUrl 
     * @return 
     */  
  
    public Bitmap readFromCache(String imageUrl) {  
        Bitmap bitmap = null;  
        try {  
            String key = MD5.getMD5(imageUrl);  
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);  
            if (snapShot != null) {//����ļ�����,��ȡ����ת��ΪBitmap����  
                InputStream is = snapShot.getInputStream(0);  
                bitmap = BitmapFactory.decodeStream(is);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return bitmap;  
    }  
    
    /**
     * ɾ�����л���
     */
    public void deleteCache(){
    	try {
			mDiskLruCache.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * ��ȡ�汾��
	 */
	public int getAppVersion(Context context) {  
	    try {  
	        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return info.versionCode;  
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();  
	    }  
	    return 1;  
	} 
	
	
	/**
	 * ��ȡ�����ַ
	 */
	public File getDiskCacheDir(Context context, String uniqueName) {  
	    String cachePath;  
	    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())  
	            || !Environment.isExternalStorageRemovable()) {  
	        cachePath = context.getExternalCacheDir().getPath();  
	    } else {  
	        cachePath = context.getCacheDir().getPath();  
	    }  
	    return new File(cachePath + File.separator + uniqueName);  
	} 
}
