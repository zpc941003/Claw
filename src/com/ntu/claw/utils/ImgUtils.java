package com.ntu.claw.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

public class ImgUtils {
	
	public static Bitmap drawable2Bitmap(Drawable drawable) {
        return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
    }
	
	 public static Bitmap zoomImg(Bitmap bm, int newWidth ,int newHeight){   
		    // ���ͼƬ�Ŀ��   
		    int width = bm.getWidth();   
		    int height = bm.getHeight();   
		    // �������ű���   
		    float scaleWidth = ((float) newWidth) / width;   
		    float scaleHeight = ((float) newHeight) / height;   
		    // ȡ����Ҫ���ŵ�matrix����   
		    Matrix matrix = new Matrix();   
		    matrix.postScale(scaleWidth, scaleHeight);   
		    // �õ��µ�ͼƬ   www.2cto.com
		    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);   
		    return newbm;   
		}  
	
    /**
     * ��ȡ��ɫ�߿�
     * 
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundeBitmapWithWhite(Bitmap bitmap) {
        int size = bitmap.getWidth();
        int num = 20;
        int sizebig = size + num;
        Bitmap output = Bitmap.createBitmap(sizebig, sizebig, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = Color.parseColor("#FFFFFF");
        final Paint paint = new Paint();
        final float roundPx = sizebig / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawBitmap(bitmap, num / 2, num / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_ATOP));

        RadialGradient gradient = new RadialGradient(roundPx, roundPx, roundPx,
                new int[] { Color.WHITE, Color.WHITE,
                        Color.parseColor("#AAAAAAAA") }, new float[] { 0.f,
                        0.97f, 1.0f }, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        return output;
    }
	
    /**
     * �����ɫ�߿�
     *
     * @param src         ԴͼƬ
     * @param borderWidth �߿���
     * @param color       �߿����ɫֵ
     * @return ����ɫ�߿�ͼ
     */
    public static Bitmap addFrame(Bitmap src, int borderWidth, int color) {
        return addFrame(src, borderWidth, color, false);
    }

    /**
     * �����ɫ�߿�
     *
     * @param src         ԴͼƬ
     * @param borderWidth �߿���
     * @param color       �߿����ɫֵ
     * @param recycle     �Ƿ����
     * @return ����ɫ�߿�ͼ
     */
    public static Bitmap addFrame(Bitmap src, int borderWidth, int color, boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        int doubleBorder = borderWidth << 1;
        int newWidth = src.getWidth() + doubleBorder;
        int newHeight = src.getHeight() + doubleBorder;
        Bitmap ret = Bitmap.createBitmap(newWidth, newHeight, src.getConfig());
        Canvas canvas = new Canvas(ret);
        Rect rect = new Rect(0, 0, newWidth, newHeight);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        // setStrokeWidth�Ǿ��л��ģ�����Ҫ�����Ŀ�Ȳ��ܻ���������һ��Ŀ���ǿյ�
        paint.setStrokeWidth(doubleBorder);
        canvas.drawRect(rect, paint);
        canvas.drawBitmap(src, borderWidth, borderWidth, null);
        if (recycle && !src.isRecycled()) src.recycle();
        return ret;
    }

    /**
     * ��ӵ�Ӱ
     *
     * @param src              ԴͼƬ��
     * @param reflectionHeight ��Ӱ�߶�
     * @return ����ӰͼƬ
     */
    public static Bitmap addReflection(Bitmap src, int reflectionHeight) {
        return addReflection(src, reflectionHeight, false);
    }

    /**
     * ��ӵ�Ӱ
     *
     * @param src              ԴͼƬ��
     * @param reflectionHeight ��Ӱ�߶�
     * @param recycle          �Ƿ����
     * @return ����ӰͼƬ
     */
    public static Bitmap addReflection(Bitmap src, int reflectionHeight, boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        // ԭͼ�뵹Ӱ֮��ļ��
        final int REFLECTION_GAP = 0;
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionBitmap = Bitmap.createBitmap(src, 0, srcHeight - reflectionHeight,
                srcWidth, reflectionHeight, matrix, false);
        Bitmap ret = Bitmap.createBitmap(srcWidth, srcHeight + reflectionHeight, src.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(reflectionBitmap, 0, srcHeight + REFLECTION_GAP, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        LinearGradient shader = new LinearGradient(0, srcHeight,
                0, ret.getHeight() + REFLECTION_GAP,
                0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.MIRROR);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, srcHeight + REFLECTION_GAP,
                srcWidth, ret.getHeight(), paint);
        if (!reflectionBitmap.isRecycled()) reflectionBitmap.recycle();
        if (recycle && !src.isRecycled()) src.recycle();
        return ret;
    }

	
    /**
     * �ж�bitmap�����Ƿ�Ϊ��
     *
     * @param src ԴͼƬ
     * @return {@code true}: ��<br>{@code false}: ��
     */
    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
	
    /**
     * תΪԲ��ͼƬ
     *
     * @param src ԴͼƬ
     * @return Բ��ͼƬ
     */
    public static Bitmap toRound(Bitmap src) {
        return toRound(src, false);
    }

    /**
     * תΪԲ��ͼƬ
     *
     * @param src     ԴͼƬ
     * @param recycle �Ƿ����
     * @return Բ��ͼƬ
     */
    public static Bitmap toRound(Bitmap src, boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        int radius = Math.min(width, height) >> 1;
        Bitmap ret = Bitmap.createBitmap(width, height, src.getConfig());
        Paint paint = new Paint();
        Canvas canvas = new Canvas(ret);
        Rect rect = new Rect(0, 0, width, height);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width >> 1, height >> 1, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
        if (recycle && !src.isRecycled()) src.recycle();
        return ret;
    }

	/** 
	 * bitmapתΪbase64 
	 * @param bitmap 
	 * @return 
	 */  
	public static String bitmapToBase64(Bitmap bitmap) {  
	    String result = null;  
	    ByteArrayOutputStream baos = null;  
	    try {  
	        if (bitmap != null) {  
	            baos = new ByteArrayOutputStream();  
	            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
	  
	            baos.flush();  
	            baos.close();  
	  
	            byte[] bitmapBytes = baos.toByteArray();  
	            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);  
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        try {  
	            if (baos != null) {  
	                baos.flush();  
	                baos.close();  
	            }  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	    return result;  
	}  
	  
	/** 
	 * base64תΪbitmap 
	 * @param base64Data 
	 * @return 
	 */  
	public static Bitmap base64ToBitmap(String base64Data) {  
	    byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);  
	    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);  
	}  
}
