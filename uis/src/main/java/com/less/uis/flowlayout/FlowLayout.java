package com.less.uis.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.less.uis.R;

import java.util.LinkedList;
import java.util.List;

/**
 * @author deeper
 * @date 2017/12/3
 *
 * <p>
 *     <a href='https://github.com/wangli0'>github</a>
 *     说明: 关于自定义ViewGroup的开发比View逻辑稍微复杂,大多数人开发方式均是移动当前坐标并加以判断的,这种方式则需要在onMesure和onLayout中分别判断,且代码逻辑晦涩难懂,甚至自己都被搞迷糊了.
 *     能否试图改变常规的开发方式,ViewGroup无非是对每个子View进行布局排列组合,不管是哪种布局,都可以给子View标上一个序号,就像电影院的座位一样,第x排,第y列,第几个进场的position人,那么就确定了.
 *     而且onMeasure和onLayout是分不开的两个操作,判断逻辑几乎一样,能否把主要的逻辑都放入到onMeasure中且给每个子View加上标号,那么所有类型的ViewGroup的onLayout代码逻辑就几乎一模一样了.
 *     这样不仅使自定义ViewGroup变得更加简单,甚至可以抽出公共逻辑,让自定义ViewGroup只需写一个方法即可实现清晰逻辑功能强大的ViewGroup.
 * </p>
 * fix bug1: AS自动导包R资源某个paddingTop变量,导致布局和理想不一致,需额外注意工具带来的坑爹bug.
 */

public class FlowLayout  extends ViewGroup {
    private float mVerticalSpacing;
    private float mHorizontalSpacing;
    private int mTextColor;
    private int mTextSize;
    private int mTvBackground;

    private int mTextPaddingH;
    private int mTextPaddingV;

    private int x_index = 0;
    private int y_index = 1;

    private List<ViewTag> viewTags = new LinkedList<>();

    public interface OnFlowItemClickListener {
        void onItemClick(String message);
    }
    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs,0);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);

        mVerticalSpacing = a.getDimension(R.styleable.FlowLayout_vSpace,dip2px(16));
        mHorizontalSpacing = a.getDimension(R.styleable.FlowLayout_hSpace,dip2px(16));
        mTextSize = a.getDimensionPixelOffset(R.styleable.FlowLayout_mTextSize,sp2px(8));
        mTextColor = a.getColor(R.styleable.FlowLayout_mTextColor, Color.GRAY);
        mTvBackground = a.getResourceId(R.styleable.FlowLayout_mTvBackground,android.R.drawable.list_selector_background);
        mTextPaddingH = a.getDimensionPixelOffset(R.styleable.FlowLayout_mTextPaddingH,dip2px(7));
        mTextPaddingV = a.getDimensionPixelOffset(R.styleable.FlowLayout_mTextPaddingV,dip2px(4));
        a.recycle();
    }

    public void setFlowListener(List<String> list, final OnFlowItemClickListener onFlowItemClickListener) {
        for (int i = 0; i < list.size(); i++) {
            final TextView tv = new TextView(getContext());

            tv.setText(list.get(i));
            tv.setTextSize(mTextSize);
            tv.setTextColor(mTextColor);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(mTextPaddingH, mTextPaddingV, mTextPaddingH, mTextPaddingV);
            tv.setBackgroundResource(mTvBackground);
            tv.setClickable(true);
            this.addView(tv,new MyLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFlowItemClickListener.onItemClick(tv.getText().toString());
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        resetAll();
        ViewTag tag = null;
        // widthSize 和 heightSize包含自身的padding
        int widthSize = resolveSize(0, widthMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int currentUsed = 0;

        // 这里把左边一个也减去为了更好计算
        int canUsed = (int) (widthSize - paddingLeft - paddingRight - mHorizontalSpacing);
        // 每行的高度即每个childView的高度
        int lineHeight = -1;

        int childCount = getChildCount();
        for (int i = 0;i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // 注: 这里我看到很多自定义控件不仅没有使用这么方便的方法而且【多余的把整个方法贴到这里】,且没有把child的margin去掉.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                lineHeight = childHeight;

                currentUsed += childWidth + mHorizontalSpacing;
                if (currentUsed > canUsed) {
                    currentUsed = (int) (childWidth + mHorizontalSpacing);
                    x_index = 1;
                    y_index +=1;
                }else{
                    x_index +=1;
                }
                tag = new ViewTag();
                tag.position = i;
                tag.x_index = x_index;
                tag.y_index = y_index;
                viewTags.add(tag);
            }
        }
        // 计算FlowLayout的高度
        float wannaHeight = paddingTop + paddingBottom + lineHeight * y_index + mVerticalSpacing * (y_index + 1);
        setMeasuredDimension(widthSize, (int) wannaHeight);
    }

    /**
     * resetAll 重置标志参数
     *
     * 由于onMeasure和onLayout可能调用多次,使用这种逻辑方式需要注意重置,否则可能和理想效果差距很大.
     */
    private void resetAll() {
        viewTags.clear();
        x_index = 0;
        y_index = 1;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        float childCount = getChildCount();
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();

        // 这里有点小瑕疵,虽然上面计算了childView的margin,但是这里并没有根据childView的margin布局,为了逻辑简单,暂时就不支持childView的margin属性了.
        for (int i = 0; i < childCount; i++){
            View childView = getChildAt(i);

            if (childView.getVisibility() != GONE) {
                MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                int childWidth = childView.getMeasuredWidth();
                int childHeight = childView.getMeasuredHeight();
                ViewTag tag = viewTags.get(i);

                int childLeft = (int) (paddingLeft + mHorizontalSpacing);
                int childTop = (int) ( paddingTop + mVerticalSpacing + (childHeight + mVerticalSpacing) * (tag.y_index - 1) );
                for(int j = 0;j < tag.position;j++) {
                    ViewTag temp = viewTags.get(j);
                    // 找到在当前view之前且在同一个Y坐标的所有子View
                    if (tag.y_index == temp.y_index) {
                        View beforeView = getChildAt(temp.position);
                        int beforeViewWidth = beforeView.getMeasuredWidth();
                        childLeft += beforeViewWidth + mHorizontalSpacing;
                    }
                }

                // 计算每个childView的左顶点坐标
                childView.layout(childLeft,childTop,childLeft + childWidth,childTop + childHeight);
            }
        }
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
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MyLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new MyLayoutParams(lp);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MyLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // dp2ptx
    public int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

class ViewTag {
    public int position;
    public int x_index;
    public int y_index;

    @Override
    public String toString() {
        return "ViewTag {" +
                "position=" + position +
                ", x_index=" + x_index +
                ", y_index=" + y_index +
                '}' + "\r\n";
    }
}