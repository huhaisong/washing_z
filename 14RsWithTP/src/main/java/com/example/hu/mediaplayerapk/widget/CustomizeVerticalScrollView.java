package com.example.hu.mediaplayerapk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by 码农专栏
 * on 2020-06-04.
 */
public class CustomizeVerticalScrollView extends ScrollView {

    private OnScrollViewListener viewListener;

    public interface OnScrollViewListener {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    public void setViewListener(OnScrollViewListener viewListener) {
        this.viewListener = viewListener;
    }

    public void removeViewListener() {
        this.viewListener = null;
    }

    public CustomizeVerticalScrollView(Context context) {
        this(context, null);
    }

    public CustomizeVerticalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomizeVerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
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
