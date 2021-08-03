package com.example.hu.mediaplayerapk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by 码农专栏
 * on 2020-06-04.
 */
public class CustomizeHorizontalScrollView extends HorizontalScrollView {

    private OnScrollViewListener viewListener;

    public interface OnScrollViewListener {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    public void setViewListener(OnScrollViewListener viewListener) {
        this.viewListener = viewListener;
    }

    public CustomizeHorizontalScrollView(Context context) {
        this(context,null);
    }

    public CustomizeHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomizeHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (viewListener != null) {
            viewListener.onScroll(l, t, oldl, oldt);
        }
    }

}
