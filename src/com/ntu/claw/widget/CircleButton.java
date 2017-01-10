package com.ntu.claw.widget;

import com.ntu.claw.R;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * Created by David on 2014/8/18.
 */
public class CircleButton extends ImageView {

    private static final int PRESSED_COLOR_LIGHTUP = 255 / 25; // 用于提高亮度
    private static final int PRESSED_RING_ALPHA = 75; // 用于处理透明�?
    private static final int DEFAULT_PRESSED_RING_WIDTH_DIP = 5; // 默认的宽�?
    private static final int ANIMATION_TIME_ID = android.R.integer.config_shortAnimTime; // 动画响应时间
    // x,y坐标，内环圆半径，外环圆半径
    private int x;
    private int y;
    private int outerRadius;
    private int pressedRingRadius;
    // 内环圆画笔，外环圆画笔，文字画笔
    private Paint circlePaint;
    private Paint focusPaint;
    private Paint textPaint;
    // 动画单位时间内散�?大小
    private float animationProgress;
    // 动画散开大小
    private int pressedRingWidth;
    // 默认的底�?
    private int defaultColor = Color.BLACK;
    // 默认文字属�??
    private String text = "default";
    private int textColor = Color.BLACK;
    private Typeface defaultTypeface = Typeface.DEFAULT;
    private int textSize = 12;
    // 按压后散�?的颜�?
    private int pressedColor;
    // 图片信息
    private Drawable drawable;
    // 用来处理格式转换
    private Context context;
    // 用来处理动画
    private ObjectAnimator pressedanimator;

    public CircleButton(Context context) {
        super(context);
        init(context, null);
    }

    public CircleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // 定义按压后的动画效果
    @Override
    public void setPressed(boolean pressed) {
        // TODO Auto-generated method stub
        super.setPressed(pressed);
        if (circlePaint != null) {
            circlePaint.setColor(pressed ? pressedColor : defaultColor);
        }

        if (pressed) {
            showPressedRing();
        } else {
            hidePressedRing();
        }
    }

    // 按钮绘制
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        pressedRingRadius = outerRadius - pressedRingWidth - pressedRingWidth
                / 2;
        focusPaint.setStrokeWidth(pressedRingWidth);
        canvas.drawCircle(x, y, pressedRingRadius + animationProgress,
                focusPaint);
        canvas.drawCircle(x, y, outerRadius - pressedRingWidth, circlePaint);
        // 通过drawText把文字画出来
        if (text != null) {
            // //获取左边�?
            // int width =(int) textPaint.measureText(text)/2;
            // int textX=x-width;
            // 获取下边�?

            FontMetrics metrics = textPaint.getFontMetrics();
            int height = (int) (metrics.top - metrics.bottom) / 2;
            int textY = y - height / 2;
            canvas.drawText(text, x, textY, textPaint);
            Log.e("Test", "textsize:" + textSize);
            // Log.e("Text","text:"+y+" texty:"+textY);
        } else if (drawable != null) {
            set_fg_drawable(drawable);
        }
        super.onDraw(canvas);
    }

    // 设置按钮的中圆的属�??
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        x = w / 2;
        y = h / 2;
        outerRadius = Math.min(w, h) / 2;
        pressedRingRadius = outerRadius - pressedRingWidth - pressedRingWidth
                / 2;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
        this.invalidate();
    }

    public void set_bg_color(int color) {
        this.defaultColor = color;
        this.pressedColor = getHighlightColor(color, PRESSED_COLOR_LIGHTUP);

        circlePaint.setColor(defaultColor);
        focusPaint.setColor(defaultColor);
        focusPaint.setAlpha(PRESSED_RING_ALPHA);

        this.invalidate();
    }

    public void set_text(String string) {
        this.text = string;

        this.invalidate();
    }

    public void set_text_size_dp(int dp) {
        // 将dp值转换成px
        final float scale = context.getResources().getDisplayMetrics().density;
        textSize = (int) (dp * scale + 0.5f);

        textPaint.setTextSize(textSize);

        this.invalidate();
    }

    public void set_typeface(Typeface typeFace) {
        this.defaultTypeface = typeFace;

        textPaint.setTypeface(defaultTypeface);

        this.invalidate();
    }

    public void set_text_color(int color) {
        this.textColor = color;

        textPaint.setColor(textColor);

        this.invalidate();
    }

    public void set_fg_drawable(Drawable drawable) {
        this.setImageDrawable(drawable);

        this.invalidate();
    }

    public void set_pressed_ring_width(int dp) {
        //转化成dp格式
        this.pressedRingWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                        .getDisplayMetrics());
        this.invalidate();
    }

    @SuppressLint("NewApi")
	private void init(Context context, AttributeSet attrs) {
        this.setFocusable(true);
        this.setScaleType(ScaleType.CENTER_INSIDE);
        this.context = context;
        setClickable(true);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Style.FILL);

        focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        pressedRingWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PRESSED_RING_WIDTH_DIP,
                getResources().getDisplayMetrics());

        int color = Color.BLACK;
        // 获取自定义xml中的属�??
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CircleButton);
            color = a.getColor(R.styleable.CircleButton_bg_color, color);
            pressedRingWidth = (int) a.getDimension(
                    R.styleable.CircleButton_pressed_ring_width,
                    pressedRingWidth);
            text = a.getString(R.styleable.CircleButton_text);
            textSize = (int) a.getDimension(
                    R.styleable.CircleButton_text_size_dp, textSize);
            textColor = a.getColor(R.styleable.CircleButton_text_color,
                    textColor);
            a.recycle();
        }


        // Paint.setTextSize处理得是px格式而需要使用的是dp格式
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.e("UNDERSTAND", "textSize:"+textSize+" scale:"+scale);
        set_bg_color(color);
        setTextPaint();

        focusPaint.setStrokeWidth(pressedRingWidth);

        final int pressedAnimationTime = getResources().getInteger(
                ANIMATION_TIME_ID);
        pressedanimator = ObjectAnimator.ofFloat(this, "animationProgress", 0f,
                0f);
        pressedanimator.setDuration(pressedAnimationTime);
    }

    // 设置TextPaint属�??
    private void setTextPaint() {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(defaultTypeface);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setColor(textColor);
        this.invalidate();
    }

    // 获取高亮配色
    private int getHighlightColor(int color, int amount) {
        return Color.argb(Math.min(255, Color.alpha(color)),
                Math.min(255, Color.red(color) + amount),
                Math.min(255, Color.green(color) + amount),
                Math.min(255, Color.blue(color) + amount));
    }

    // 显示散开动画
    @SuppressLint("NewApi")
	private void showPressedRing() {
        pressedanimator.setFloatValues(animationProgress, pressedRingWidth);
        pressedanimator.start();
    }

    // 隐藏散开动画
    @SuppressLint("NewApi")
	private void hidePressedRing() {
        pressedanimator.setFloatValues(pressedRingWidth, 0f);
        pressedanimator.start();
    }

}
