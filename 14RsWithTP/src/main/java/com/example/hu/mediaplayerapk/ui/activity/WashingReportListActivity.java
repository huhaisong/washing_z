package com.example.hu.mediaplayerapk.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.adapter.washingReportAdapter;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.util.Logger;
import com.example.hu.mediaplayerapk.util.washingLogUtil;

import java.io.File;
import java.util.ArrayList;

public class WashingReportListActivity extends ListActivity {
    private TextView titleTextview = null;

    private ArrayList<WashingReportItem> allReportList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washing_report_list);

        titleTextview = (TextView)findViewById(R.id.titleTextview);


        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.ECLAIR)
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        showLogDir();
    }


    /**
     * 扫描显示日志系统
     *
     * @param
     */
    private void showLogDir() {

        allReportList = washingLogUtil.buildAllReport();

        this.setListAdapter(new washingReportAdapter(this, allReportList));
    }
}
