package com.example.hu.mediaplayerapk.ui.activity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.adapter.WashingReportDetailAdapter;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dao.WashingReportManager;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.RestartAlarmWatcher;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WashingReportDetailItemActivity extends BaseActivity {


    public static final String DAY = "DAY";
    public static final String MONTH = "MONTH";
    public static final String FACE_ID = "FACE_ID";
    private Context mContext;
    private int day;
    private int month;
    private String faceId;

    private TextView totalWashingTextView;
    private TextView totalInterruptTextView;
    private TextView totalFirstPlayTextView;
    private TextView totalLongTimeTextView;
    private TextView totalSecondPlayTextView;
    private TextView temperatureTextView;
    private TextView dateTextView;
    private TextView faceTextView;
    private TextView backTextView;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private WashingReportDetailAdapter washingReportDetailAdapter;

    private int totalInterrupt, totalFirstPlay, totalLongTime, totalSecondTime, totalTemperatureTimes;
    private double totalTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washing_report_detail_item);
        mContext = this;
        Intent intent = getIntent();
        day = intent.getIntExtra(DAY, 0);
        month = intent.getIntExtra(MONTH, 0);
        faceId = intent.getStringExtra(FACE_ID);

        totalWashingTextView = findViewById(R.id.tv_total_washing);
        totalInterruptTextView = findViewById(R.id.tv_total_interrupt);
        totalFirstPlayTextView = findViewById(R.id.tv_total_first_play);
        totalLongTimeTextView = findViewById(R.id.tv_total_long_time);
        totalSecondPlayTextView = findViewById(R.id.tv_total_second_play);
        temperatureTextView = findViewById(R.id.tv_temperature);
        backTextView = findViewById(R.id.tv_back);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        dateTextView = findViewById(R.id.tv_date);
        faceTextView = findViewById(R.id.faceIDValue);
        recyclerView = findViewById(R.id.rv_detail);
        imageView = findViewById(R.id.photoView);
        faceTextView.setText(faceId);
        initData();
    }

    private static final String TAG = "WashingReportDetailItem";

    private void initData() {
        double averageTemp;
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
        a.set(Calendar.DATE, day);//
        a.set(Calendar.HOUR_OF_DAY, 0);
        a.set(Calendar.MINUTE, 0);
        a.set(Calendar.SECOND, 0);

        int startTime = (int) (a.getTimeInMillis() / 1000);
        List<WashingReportItem> washingReportItems = WashingReportManager.getInstance(this)
                .searchByFaceIdAndDate(faceId, startTime);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(a.getTimeInMillis());
        dateTextView.setText("?????????????????? " + simpleDateFormat.format(date));

        totalInterrupt = totalFirstPlay = totalLongTime = totalSecondTime =  totalTemperatureTimes = 0;
        totalTemperature = 0;

        //if (washingReportItems.size() > 0) {
            //display thumbnail
            {
                String picPath = Config.FACEME_PIC_PATH + faceId + ".jpg";
                Uri picUri= Uri.parse(picPath);

                if(FileUtils.fileState(picPath) == true) {
                    imageView.setImageURI(picUri);
                }
                else {
                    if (FaceManagerUtil.IsLadyID(faceId) == true) {
                        imageView.setImageResource(R.drawable.lady);
                    } else {
                        imageView.setImageResource(R.drawable.gentleman);
                    }
                }
            }
        //}

        for (int i = 0; i < washingReportItems.size(); i++) {
            WashingReportItem item = washingReportItems.get(i);
            if (item.getIsLongInterval() == 1) {
                totalLongTime++;
            }
            if (item.getPlayNum() == 1) {
                totalFirstPlay++;
            } else if (item.getPlayNum() == 2) {
                totalSecondTime++;
            }
            if (item.getIsPlayInterrupt() == 1) {
                totalInterrupt++;
            }

            if (item.getLastTemp() != 0) {
                totalTemperatureTimes++;
                totalTemperature += item.getLastTemp();
            }
        }

        totalWashingTextView.setText("??????????????????" + washingReportItems.size() + "???");
        totalInterruptTextView.setText("???????????????" + totalInterrupt + "???");
        totalFirstPlayTextView.setText("???????????????" + totalFirstPlay + "???");
        totalSecondPlayTextView.setText("????????????????????????" + totalSecondTime + "???");
        totalLongTimeTextView.setText("????????????" + totalLongTime + "???");
        if (totalTemperatureTimes != 0) {
            averageTemp = (double)totalTemperature / totalTemperatureTimes;
            temperatureTextView.setText("???????????????" + (double) ((int)( averageTemp * 10) / 10d) + "???");
        }
        washingReportDetailAdapter = new WashingReportDetailAdapter(this, washingReportItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(washingReportDetailAdapter);
    }
}
