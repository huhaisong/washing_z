package com.example.hu.mediaplayerapk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.hu.mediaplayerapk.ui.widget.LoadingDialog;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.widget.CustomizeScrollView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
    private LinearLayout llContent;
    private TabAdapter mTabAdapter;  //标题栏的title
    private StockAdapter mStockAdapter; //face id list
    private TextView emptyView;
    private TextView monthTextView;
    private TextView backTextView;
    private ImageView leftImg;
    private ImageView rightImg;
    LoadingDialog loadingDialog;
    ArrayList stockBeans = new ArrayList<StockBean>(); //每个ID下的3个统计数据
    private int curMonth;

    private static final int DATA_PREPARE = 1;
    private static final int LOADING_FINISH = 2;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == DATA_PREPARE) {//1111表示熄灭
                mStockAdapter.setStockBeans(stockBeans);
                /*for (int i = 0; i < stockBeans.size(); i++) {
                    Log.e(TAG, "initStock: " + stockBeans.get(i).toString());
                }*/
                if (stockBeans == null || stockBeans.size() == 0) {
                    llContent.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    llContent.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                //mHandler.sendEmptyMessageDelayed(LOADING_FINISH, 1000+stockBeans.size()*50);
                mHandler.sendEmptyMessage(LOADING_FINISH);
            }
            else if(msg.what == LOADING_FINISH)
            {
                if (loadingDialog != null)
                    loadingDialog.dismiss();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail_list);
        mHeadRecyclerView = findViewById(R.id.headRecyclerView);
        mContentRecyclerView = findViewById(R.id.contentRecyclerView);
        llContent = findViewById(R.id.ll_content);
        headHorizontalScrollView = findViewById(R.id.headScrollView);
        emptyView = findViewById(R.id.empty_view);
        leftImg = findViewById(R.id.iv_left);
        backTextView = findViewById(R.id.tv_back);
        rightImg = findViewById(R.id.iv_right);
        monthTextView = findViewById(R.id.tv_month);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mHeadRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        mTabAdapter = new TabAdapter(this);
        mHeadRecyclerView.setNestedScrollingEnabled(false);
        mHeadRecyclerView.setAdapter(mTabAdapter);

        mContentRecyclerView.setNestedScrollingEnabled(false);
        mContentRecyclerView.setItemViewCacheSize(100);
        mContentRecyclerView.setDrawingCacheEnabled(true);

        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mContentRecyclerView.getLayoutManager().setItemPrefetchEnabled(false);

        mStockAdapter = new StockAdapter(this, new StockAdapter.StockItemListener() {
            @Override
            public void onItemClick(StockBean item, int day) {
                Bundle bundle = new Bundle();
                bundle.putInt(WashingReportDetailItemActivity.DAY, day);
                bundle.putInt(WashingReportDetailItemActivity.MONTH, curMonth);
                bundle.putString(WashingReportDetailItemActivity.FACE_ID, item.getFaceId());
                Intent intent = new Intent(WashingReportDetailListActivity.this, WashingReportDetailItemActivity.class);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
            }
        });
        mContentRecyclerView.setAdapter(mStockAdapter);
        mStockAdapter.setOnTabScrollViewListener(new StockAdapter.OnTabScrollViewListener() {
            @Override
            public void scrollTo(int l, int t) {
                if (headHorizontalScrollView != null) {
                    headHorizontalScrollView.scrollTo(l, 0);
                }
            }
        });

        LoadingDialog.Builder builder1 = new LoadingDialog.Builder(this)
                .setMessage("Loading...")
                .setCancelable(false);
        loadingDialog = builder1.create();
        curMonth = TimeUtil.getDate()[1] + 1;

        leftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curMonth <= TimeUtil.getDate()[1] - 5)
                    return;
                curMonth--;
                initData();
            }
        });
        rightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curMonth >= TimeUtil.getDate()[1] + 1)
                    return;
                curMonth++;
                initData();
            }
        });
        initData();
        initListener();
    }

    private void initData() {
        if (loadingDialog != null)
            loadingDialog.show();
        initTab(curMonth);

        if (curMonth < 0) {
            monthTextView.setText("" + (curMonth + 12));
        } else if (curMonth > 12) {
            monthTextView.setText("" + (curMonth - 12));
        } else {
            monthTextView.setText("" + curMonth);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                initStock(curMonth);
            }
        }).start();
    }

    private static final String TAG = "WashingReportDetailList";

    private void initStock(int month) {
        stockBeans.clear();
        List<FaceIDBean> faceIDBeans = FaceManagerUtil.getFaceIDList();
        Log.e(TAG, "initStock:1 " + faceIDBeans.size());
        for (int i = 0; i < faceIDBeans.size(); i++) {
            StockBean stockBean = new StockBean();
            stockBean.setFaceId(faceIDBeans.get(i).getFaceID());
            ArrayList<StockBean.Date> dateArrayList = new ArrayList<>();
            int days;
            if (month < 0) {
                days = TimeUtil.getLastYearMonthLastDay(month);
            } else if (month > 12) {
                days = TimeUtil.getNextYearMonthLastDay(month);
            } else {
                days = TimeUtil.getMonthLastDay(month);
            }
            for (int j = 1; j <= days; j++) {

                StockBean.Date date = new StockBean.Date();
                Calendar a = Calendar.getInstance();
                if (month < 0) {
                    a.set(Calendar.YEAR, a.get(Calendar.YEAR) - 1);
                    a.set(Calendar.MONTH, month + 11);
                } else if (month > 12) {
                    a.set(Calendar.YEAR, a.get(Calendar.YEAR) + 1);
                    a.set(Calendar.MONTH, month - 13);
                } else {
                    a.set(Calendar.MONTH, month - 1);
                }
                a.setTimeZone(TimeZone.getDefault());
                a.set(Calendar.DATE, j);//把日期设置为当月第一天
                a.set(Calendar.HOUR_OF_DAY, 0);
                a.set(Calendar.MINUTE, 0);
                a.set(Calendar.SECOND, 0);
                int startTime = (int) (a.getTimeInMillis() / 1000);
                List<WashingReportItem> washingReportItems = WashingReportManager.getInstance(this)
                        .searchByFaceIdAndDate(stockBean.getFaceId(), startTime);
                //Log.e(TAG, "initStock: " + startTime + "stockBean.getStockName() " + stockBean.getFaceId() + ",initStock:2 " + washingReportItems.size());
                int totalWashing = washingReportItems.size();
                int totalInterrupt = 0;
                int totalLongtime = 0;
                if (washingReportItems != null && washingReportItems.size() != 0) {
                    stockBean.setIsLadyOrMen(washingReportItems.get(0).getIsLadyOrMen());
                    for (int k = 0; k < washingReportItems.size(); k++) {
                        if (washingReportItems.get(k).getIsPlayInterrupt() == 1)
                            totalInterrupt++;

                        if (washingReportItems.get(k).getIsLongInterval() == 1)
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
        mHandler.sendEmptyMessageDelayed(DATA_PREPARE, 10);
    }

    private void initTab(int month) {
        ArrayList days = new ArrayList<String>();
        if (month < 1) {
            for (int i = 0; i < TimeUtil.getLastYearMonthLastDay(month); i++) {
                days.add((month + 12) + "/" + (i + 1));
            }
        } else if (month > 12) {
            for (int i = 0; i < TimeUtil.getNextYearMonthLastDay(month); i++) {
                days.add((month - 12) + "/" + (i + 1));
            }
        } else {
            for (int i = 0; i < TimeUtil.getMonthLastDay(month); i++) {
                days.add(month + "/" + (i + 1));
            }
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
