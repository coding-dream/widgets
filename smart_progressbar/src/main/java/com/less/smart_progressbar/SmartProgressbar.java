package com.less.smart_progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 *
 * @author deeper
 * @date 2017/10/25
 */

public class SmartProgressbar extends View {
    private static final float DEFAULT_CONTENT_WIDTH = 100;
    private static final float DEFAULT_CONTENT_HEIGHT = 100;

    /**
     * *********************************** 自定义属性 ***********************************
     * textSize 字体大小
     */
    private float textSize;
    /**
     * textColor 字体颜色
     */
    private int textColor;
    /**
     * progressColor 进度条颜色
     */
    private int progressColor;
    /**
     * unProgressColor 进度条背景颜色
     */
    private int unProgressColor;
    /**
     * currentProgress 当前进度
     */
    private int currentProgress = 0;
    /**
     * maxProgress 最大进度
     */
    private int maxProgress = 100;
    /**
     * progressWidth 进度条宽度
     */
    private float progressWidth;

    /**
     * *********************************** 非自定义属性 ***********************************
     */
    private Paint circlePaint;
    private Paint arcPaint;
    private Paint textPaint;
    private String currentText;
    private RectF rectF = new RectF(0,0,0,0);

    public SmartProgressbar(Context context) {
        super(context);
    }

    public SmartProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SmartProgressbar);
        // 推荐-》 直接获取，获取不到的则设为默认，而if else或switch方式-》如果用户没有设置xml属性，则跳过case
        textSize = sp2px(typedArray.getDimension(R.styleable.SmartProgressbar_textSize, 14f));
        textColor = typedArray.getColor(R.styleable.SmartProgressbar_textColor, Color.parseColor("#3498DB"));
        unProgressColor = typedArray.getColor(R.styleable.SmartProgressbar_unProgressColor, Color.parseColor("#113498DB"));
        progressColor = typedArray.getColor(R.styleable.SmartProgressbar_progressColor, Color.parseColor("#3498DB"));
        currentProgress = typedArray.getInt(R.styleable.SmartProgressbar_currentProgress, 0);
        maxProgress = typedArray.getInt(R.styleable.SmartProgressbar_maxProgress, 100);
        progressWidth = dip2px(typedArray.getDimension(R.styleable.SmartProgressbar_progressWidth, 4));

        typedArray.recycle();
        initPaint();
    }

    private void initPaint() {
        // 默认圆环的颜色
        circlePaint = new Paint();
        // 抗锯齿功能
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        // 默认圆环的width
        circlePaint.setStrokeWidth(progressWidth);
        // 默认圆环的颜色
        circlePaint.setColor(unProgressColor);
        // 设置阴影
        // circlePaint.setShadowLayer(10, 15, 15, Color.GREEN);

        // 进度弧形的画笔，即圆环划过的部分
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(progressWidth);
        arcPaint.setColor(progressColor);

        // 中间字体的画笔
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 多数情况下widthSize和widthMeasureSpec相等(里面包含padding)
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // widthSize包含padding
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // heightSize包含padding
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        // measure width
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                // 很少用,忽略
                break;
            case MeasureSpec.AT_MOST:
                // 表示子布局限制在一个最大值内(一般为wrap_content)
                // 测量此View中 可能绘制的内容(如文字或者图片) 的大小来确定wrap_content大小，或者简单的设置某一个固定的默认值
                width = (int) dip2px(DEFAULT_CONTENT_WIDTH) + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                // march_parent 或者 具体值如(50px)
                width = widthSize;
                break;
            default:
                break;
        }
        // measure height
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                // ignore
                break;
            case MeasureSpec.AT_MOST:
                // wrap_content
                // 无论用户padding设置多大，我们绘制的内容的大小(DEFAULT_CONTENT_HEIGHT)都不会变形,所以我们这里设置wrap_content时候 + getPaddingTop() + getPaddingBottom()
                height = (int) dip2px(DEFAULT_CONTENT_HEIGHT) + getPaddingTop() + getPaddingBottom();
                // 下面如果这样完全固定设置整个View的width,则用户设置不同的padding会导致 《内容》压缩，如果设置过大，内容都不见了。
                // height = (int) dip2px(200);
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            default:
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // margin不需要子View处理，由ViewGroup处理，但需要处理padding
        // 设置画布背景颜色
        canvas.drawARGB(0,0,0,0);// 透明
        // 圆心坐标(x,y)
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        // 圆环半径(去除padding)
        float _radius1 = (getWidth() - getPaddingLeft() - getPaddingRight() - progressWidth ) / 2;
        float _radius2 = (getHeight() - getPaddingTop() - getPaddingBottom() - progressWidth ) / 2;
        float radius = Math.min(_radius1, _radius2);

        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // 圆弧的矩形坐标(centerX,centerY,left,right 等都是相对坐标，相对于该View)
        rectF.left = centerX - radius;
        rectF.right = centerX + radius;
        rectF.top = centerY - radius;
        rectF.bottom = centerY + radius;
        /*
         * RectF oval:生成椭圆的矩形
         * float startAngle：弧开始的角度，以X轴正方向为0度
         * float sweepAngle：弧持续的角度
         * boolean useCenter:是否有弧的两边，True，弧的两边，False，只有一条弧
         */
        canvas.drawArc(rectF, 0, 360 * currentProgress / maxProgress, false, arcPaint);
        // draw 中间文字
        currentText = String.format("%d", 100 * currentProgress / maxProgress) + "%";
        // 测量当前文本的宽度
        float drawTextWidth = textPaint.measureText(currentText);
        canvas.drawText(currentText,centerX - drawTextWidth / 2.0f, centerY - ((textPaint.descent() + textPaint.ascent()) / 2.0f),textPaint);
    }

    public int getProgress() {
        return currentProgress;
    }

    public void setProgress(int progress) {
        this.currentProgress = progress;
        // 重绘
        invalidate();
    }

    // dp2ptx
    public float dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    // sp2px
    public float sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (spValue * fontScale + 0.5f);
    }
}
