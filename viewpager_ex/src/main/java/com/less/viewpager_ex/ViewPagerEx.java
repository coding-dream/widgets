package com.less.viewpager_ex;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 *
 * @author deeper
 * @date 2017/10/26
 */

public class ViewPagerEx extends ViewGroup {
    private static final String TAG = ViewPagerEx.class.getSimpleName();
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mLastX = 0;
    private int mLastY = 0;

    private int mChildrenSize;
    private int mChildWidth;
    private int mChildIndex;

    public ViewPagerEx(Context context) {
        super(context);
    }

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mScroller == null) {
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // onMeasure(ViewGroup )不仅需要测量并设置自己的尺寸setMeasuredDimension，还要测量children的尺寸(View获取尺寸后自己设置setMeasuredDimension)
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算出所有的childView的宽和高(注意:ViewGroup默认的measureChildren();没有去除子View margin的影响,所以这里不采用,且Android系统其它布局均没有采用此方法)
        int maxHeight = 0;
        int maxWidth = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

                MyLayoutParams lp = (MyLayoutParams) child.getLayoutParams();
                // child View本身包含padding,注意在View的onDraw绘图时候考虑padding的影响,其margin则完全又ViewGroup掌控,需要在ViewGroup的onLayout中考虑.
                // ViewPager的width即是 所有child的width之和 + ViewGroup自己的padding (margin都是由parent处理,不需要自己处理)
                maxWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                // ViewPager的height即是 child中拥有最大height的View的height.
                maxHeight = Math.max(maxHeight,child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }
        }

        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        int width;
        int height;
        // ViewGroup设置自己的setMeasuredDimension时候,也要考虑widthMode和heightMode的影响,使其支持match_parent和wrap_content方式.
        if (widthMode == MeasureSpec.EXACTLY) {
            width = sizeWidth;
        }else {
            width = maxWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = sizeHeight;
        }else {
            height = maxHeight;
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        mChildrenSize = count;

        int viewpagerPaddingLeft = getPaddingLeft();
        int viewpagerPaddingTop = getPaddingTop();

        int childLeft = viewpagerPaddingLeft;
        int childTop = viewpagerPaddingTop;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                mChildWidth = width;

                childLeft += lp.leftMargin;// 当前View
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                childLeft += width + lp.rightMargin;// 下一个View
            }
        }
    }

    /**
     * ******************************** 让你的 Child View 支持 margin ********************************
     * 从xml中生成child View 的LayoutParams(查看MarginLayoutParams源码可知道和自定义View属性一样的设置方法)
     * 为了让child View支持 margin,我们需要实现generateLayoutParams(AttributeSet attrs),generateLayoutParams(ViewGroup.LayoutParams lp),generateDefaultLayoutParams()几个方法
     * 使用自定义的MyLayoutParams(参考FrameLayout源码)
     * -----------------------------------------------------------------------------------------------
     * 源码讲解: View 类有个 ViewGroup.LayoutParams参数(可设 多种子类)，至于child View是如何支持margin,padding参数的?
     * 查阅ViewGroup的addView()方法.child View的margin等属性是通过ViewGroup.addView()方法实现的,addView()首先
     * 调用LayoutParams params = child.getLayoutParams();如果为空,调用generateDefaultLayoutParams,最后会调
     * 用child.setLayoutParams(params);详情查阅源码!
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MyLayoutParams(getContext(), attrs);
    }

    /**
     * 从java代码中生成child View 的LayoutParams
     * @param lp
     * @return
     */
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new MyLayoutParams(lp);
    }

    /**
     * 生成 child View 默认的 LayoutParams实例
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MyLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public static class MyLayoutParams extends MarginLayoutParams {

        public MyLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public MyLayoutParams(int width, int height) {
            super(width, height);
        }

        public MyLayoutParams(LayoutParams lp) {
            super(lp);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercepted = false;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                break;
            }
            default:
                break;
        }

        Log.d(TAG, "intercepted=" + intercepted);
        mLastX = x;
        mLastY = y;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX, 0);
                break;
            }
            case MotionEvent.ACTION_UP: {
                int scrollX = getScrollX();
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= 50) {
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
                } else {
                    mChildIndex = (scrollX + mChildWidth / 2) / mChildWidth;
                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
                int dx = mChildIndex * mChildWidth - scrollX;
                smoothScrollBy(dx, 0);
                mVelocityTracker.clear();
                break;
            }
            default:
                break;
        }

        mLastX = x;
        mLastY = y;
        return true;
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(getScrollX(), 0, dx, 0, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
