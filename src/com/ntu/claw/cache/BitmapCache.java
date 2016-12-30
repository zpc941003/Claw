package com.ntu.claw.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.ntu.claw.utils.L;
import com.ntu.claw.utils.MD5;

public class BitmapCache implements ImageCache {

	private LruCache<String, Bitmap> mCache;
	private Context mContext;

	public BitmapCache(Context context) {
		int maxSize = 4 * 1024 * 1024;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
		mContext = context;
	}

	@Override
	public Bitmap getBitmap(String url) {//内存为空则从磁盘读取再写入内存
		String key = MD5.getMD5(url);
		Bitmap bitmap = mCache.get(url);
		if (bitmap == null) {
			L.i("read from diskcache");
			bitmap = DiskLruCacheHelper.getInstance(mContext)
					.readFromCache(key);
			if (bitmap != null) {
				mCache.put(url, bitmap);
			}
		}
		return bitmap;
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		if (bitmap != null) {
			mCache.put(url, bitmap);
			DiskLruCacheHelper.getInstance(mContext).writeToCache(MD5.getMD5(url), bitmap);
		}
	}

}
