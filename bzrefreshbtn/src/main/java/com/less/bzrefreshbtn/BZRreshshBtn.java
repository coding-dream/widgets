package com.less.bzrefreshbtn;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by deeper on 2017/11/7.
 * 仿哔哩哔哩刷新按钮，未支持padding和wrap_content(需重写 onMeasure)
 */

public class BZRreshshBtn extends View {

    private int borderColor = Color.parseColor("#fb7299");
    private float borderWidth = 0;
    private float borderRadius = 120;

    private String text = "";
    private int textColor = Color.parseColor("#fb7299");
    private float textSize = 28;

    private int iconSrc = R.mipmap.tag_center_refresh_icon;
    private float iconSize = 28;
    private Bitmap iconBitmap;
    private float space4TextAndIcon = 20;
    private float degress = 0;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ObjectAnimator mAnimator;

    public BZRreshshBtn(Context context) {
        this(context, null);
    }

    public BZRreshshBtn(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BZRreshshBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取自定义属性值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BZRreshshBtn);
        borderColor = ta.getColor(R.styleable.BZRreshshBtn_refresh_btn_borderColor, Color.parseColor("#fb7299"));
        borderWidth = ta.getDimension(R.styleable.BZRreshshBtn_refresh_btn_borderWidth, dipToPx(1));
        borderRadius = ta.getDimension(R.styleable.BZRreshshBtn_refresh_btn_borderRadius, dipToPx(18));
        text = ta.getString(R.styleable.BZRreshshBtn_refresh_btn_text);
        if (text == null) {
            text = "点击换一批";
        }
        textColor = ta.getColor(R.styleable.BZRreshshBtn_refresh_btn_textColor, Color.parseColor("#fb7299"));
        textSize = ta.getDimension(R.styleable.BZRreshshBtn_refresh_btn_textSize, spToPx(14));
        iconSrc = ta.getResourceId(R.styleable.BZRreshshBtn_refresh_btn_iconSrc, R.mipmap.tag_center_refresh_icon);
        iconSize = ta.getDimension(R.styleable.BZRreshshBtn_refresh_btn_iconSize, dipToPx(16));
        space4TextAndIcon = ta.getDimension(R.styleable.BZRreshshBtn_refresh_btn_space4TextAndIcon, dipToPx(10));

        ta.recycle();

        // icon
        iconBitmap = BitmapFactory.decodeResource(getResources(), iconSrc);
        iconBitmap = zoomImg(iconBitmap, iconSize, iconSize);

        // 属性动画（即控制该View的某个属性值发生改变）
        mAnimator = ObjectAnimator.ofObject(this, "degress", new FloatEvaluator(), 0, 360);
        mAnimator.setDuration(2000);
        mAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /***************************************************************************************
         * 1. 每次调用canvas.drawXXXX系列函数来绘图进，都会产生一个全新的Canvas画布并在此画布上面作画（就好像我要画个乌龟，但是没有纸，就重新拿个纸并在上面画画，如果有现成的纸，那我就直接在这上面画了【restore】），图层的概念
         * 2. 如果在DrawXXX前，调用平移、旋转等函数来对Canvas进行了操作，那么这个操作是不可逆的！每次产生的画布的最新位置都是这些操作后的位置。
         * 3. 在Canvas与屏幕合成时，超出屏幕范围的图像是不会显示出来的。
         * 4. save和restore是配合使用的，save把【当前】的画布（图层）状态放入特定的栈中，多次save，就会放入多个画布（图层）状态，restore则正好相反，restore则是取出栈顶的画布，并在此画布上面操作。简单理解为push和pop的操作。
         */

        // 1、画圆角矩形
        if (borderWidth > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(borderColor);
            mPaint.setStrokeWidth(borderWidth);
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), borderRadius, borderRadius, mPaint);
            // 保存当前画布到栈顶
            canvas.save();
        }

        // 取出栈顶的画布作为当前画布（正准备画画，发现没有纸，要么造张纸，要么拿张纸，这里选择拿张纸重复利用）
        canvas.restore();
        // 2、画字
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
        mPaint.setStyle(Paint.Style.FILL);
        float measureText = mPaint.measureText(text);
        float measureAndIcon = measureText + space4TextAndIcon + iconSize;
        float textStartX = getWidth() / 2 - measureAndIcon / 2;
        float textBaseY = getHeight() / 2 + (Math.abs(mPaint.ascent()) - mPaint.descent()) / 2;
        canvas.drawText(text, textStartX, textBaseY, mPaint);
        // 保存当前画布到栈顶
        canvas.save();

        // 取出栈顶的画布作为当前画布
        canvas.restore();
        // 3、画刷新图标
        float iconStartX = textStartX + measureText + space4TextAndIcon;
        float centerX = iconStartX + iconSize / 2;
        int centerY = getHeight() / 2;
        canvas.rotate(degress, centerX, centerY);
        canvas.drawBitmap(iconBitmap, iconStartX, getHeight() / 2 - iconSize / 2, mPaint);
        canvas.save();
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            textColor = Color.parseColor("#fb7200");
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            textColor = Color.parseColor("#fb7299");
        }
        return super.onTouchEvent(event);
    }

    public void start() {
        if (mAnimator != null && mAnimator.isRunning()) {
            return;
        }
        mAnimator.start();
    }

    public void stop() {
        // 一定要cancel掉，否则会导致内存泄露
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        setDegress(0);
    }

    public float getDegress() {
        return degress;
    }

    public void setDegress(float degress) {
        this.degress = degress;
        invalidate();
    }

    private Bitmap zoomImg(Bitmap bm, float newWidth, float newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    private float dipToPx(float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}