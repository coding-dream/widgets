package com.less.uis;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.less.common.UiUtil;

public class TitleBar extends FrameLayout {
    private TextView mTitle;
    private ImageView mIcon;


    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.lay_title_bar, this, true);

        mTitle = (TextView) findViewById(R.id.tv_title);
        mIcon = (ImageView) findViewById(R.id.iv_icon);


        if (attrs != null) {
            // Load attributes
            final TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.TitleBar);

            String title = a.getString(R.styleable.TitleBar_aTitle);
            Drawable drawable = a.getDrawable(R.styleable.TitleBar_aIcon);
            a.recycle();

            mTitle.setText(title);
            mIcon.setImageDrawable(drawable);
        } else {
            mIcon.setVisibility(GONE);
        }

        // Set Background
        setBackgroundColor(getResources().getColor(R.color.main_green));

        // Init padding
        setPadding(getLeft(), getTop() + UiUtil.getStatusBarHeight(getContext()), getRight(), getBottom());
    }

    public void setTitle(int titleRes) {
        if (titleRes <= 0) {
            return;
        }
        mTitle.setText(titleRes);
    }

    public void setIcon(int iconRes) {
        if (iconRes <= 0) {
            mIcon.setVisibility(GONE);
            return;
        }
        mIcon.setImageResource(iconRes);
        mIcon.setVisibility(VISIBLE);
    }

    public void setIconOnClickListener(OnClickListener listener) {
        if (listener != null) {
            mIcon.setOnClickListener(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        // minH = 36dp + statusBar 大约56左右
        int minH = (int) (density * 36 + UiUtil.getStatusBarHeight(getContext()));

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(minH, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
