package com.example.hu.mediaplayerapk.ui.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.adapter.StockAdapter;
import com.example.hu.mediaplayerapk.adapter.TabAdapter;
import com.example.hu.mediaplayerapk.bean.FaceIDBean;
import com.example.hu.mediaplayerapk.bean.StockBean;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.dao.WashingReportManager;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.widget.CustomizeScrollView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ===================================
 * Author: Eric
 * Date: 2021/7/13 13:16
 * Description:
 * ===================================
 */
public class WashingReportDetailListActivity extends BaseActivity {

    private CustomizeScrollView headHorizontalScrollView;
    private RecyclerView mHeadRecyclerView;
    private RecyclerView mContentRecyclerView;
    private TabAdapter mTabAdapter;
    private StockAdapter mStockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail_list);
        mHeadRecyclerView = findViewById(R.id.headRecyclerView);
        mContentRecyclerView = findViewById(R.id.contentRecyclerView);
        headHorizontalScrollView = findViewById(R.id.headScrollView);

        // TODO:Tab栏RecycleView
        // 设置RecyclerView水平显示
        mHeadRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mTabAdapter = new TabAdapter(this);
        // 设置ListView禁止滑动，这样使得ScrollView滑动更流畅
        mHeadRecyclerView.setNestedScrollingEnabled(false);
        mHeadRecyclerView.setAdapter(mTabAdapter);

        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mStockAdapter = new StockAdapter(this);
        mContentRecyclerView.setAdapter(mStockAdapter);
        mStockAdapter.setOnTabScrollViewListener(new StockAdapter.OnTabScrollViewListener() {
            @Override
            public void scrollTo(int l, int t) {
                if (headHorizontalScrollView != null) {
                    headHorizontalScrollView.scrollTo(l, 0);
                }
            }
        });

        initData();
        initListener();
    }

    private void initData() {
        initTab(TimeUtil.getDate()[1] + 1);
        initStock(TimeUtil.getDate()[1] + 1);
    }

    private void initStock(int month) {
        ArrayList stockBeans = new ArrayList<StockBean>();
        List<FaceIDBean> faceIDBeans = FaceManagerUtil.getFaceIDList();
        for (int i = 0; i < faceIDBeans.size(); i++) {
            StockBean stockBean = new StockBean();
            stockBean.setStockName(faceIDBeans.get(i).getFaceID());
            ArrayList<StockBean.Date> dateArrayList = new ArrayList<>();
            for (int j = 0; j < TimeUtil.getMonthLastDay(month); j++) {
                StockBean.Date date = new StockBean.Date();
                Calendar a = Calendar.getInstance();
                a.set(Calendar.MONTH, month - 1);
                a.set(Calendar.DATE, i + 1);//把日期设置为当月第一天
                int startTime = (int) (a.getTimeInMillis() / 1000);

                List<WashingReportItem> washingReportItems = WashingReportManager.getInstance(this)
                        .searchByFaceIdAndDate(stockBean.getStockName(), startTime);
                int totalWashing = washingReportItems.size();
                int totalInterrupt = 0;
                int totalLongtime = 0;
                if (washingReportItems!= null && washingReportItems.size()!= 0){
                    stockBean.setIsLadyOrMen(washingReportItems.get(i).getIsLadyOrMen());
                    for (int k = 0; k < washingReportItems.size(); k++) {
                        if (washingReportItems.get(i).getIsPlayInterrupt() == 1)
                            totalInterrupt++;

                        if (washingReportItems.get(i).getIsPlayInterrupt() == 1)
                            totalLongtime++;
                    }
                }
                date.setTotalInterrupt(totalInterrupt);
                date.setTotalLongtime(totalLongtime);
                date.setTotalWashing(totalWashing);
                dateArrayList.add(date);
            }
            stockBean.setDetail(dateArrayList);
            stockBeans.add(stockBean);
        }
        mStockAdapter.setStockBeans(stockBeans);
    }

    private void initTab(int month) {
        ArrayList days = new ArrayList<String>();
        for (int i = 0; i < TimeUtil.getMonthLastDay(month); i++) {
            days.add(month + "/" + (i + 1));
        }
        mTabAdapter.setTabData(days);
    }

    private void initListener() {
        headHorizontalScrollView.setViewListener(new CustomizeScrollView.OnScrollViewListener() {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt) {
                List<StockAdapter.ViewHolder> viewHolders = mStockAdapter.getRecyclerViewHolder();
                for (StockAdapter.ViewHolder viewHolder : viewHolders) {
                    viewHolder.mStockScrollView.scrollTo(l, 0);
                }
            }
        });
        mContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                List<StockAdapter.ViewHolder> viewHolders = mStockAdapter.getRecyclerViewHolder();
                for (StockAdapter.ViewHolder viewHolder : viewHolders) {
                    viewHolder.mStockScrollView.scrollTo(mStockAdapter.getOffestX(), 0);
                }
            }
        });
    }
}
