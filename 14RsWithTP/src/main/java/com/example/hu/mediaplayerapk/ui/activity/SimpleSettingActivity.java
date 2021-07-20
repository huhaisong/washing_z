package com.example.hu.mediaplayerapk.ui.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.util.RestartAlarmWatcher;
import com.example.hu.mediaplayerapk.util.SPUtils;

public class SimpleSettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SimpleSettingActivity";
    private Button btn_return;
    private Button btn_log;
    private Button btn_detail_log;
    private Button btn_change_password;
    private EditText editTextEmailAddr;  //邮件地址
    private EditText editTextErrorTemp;  //高温体温值
    private EditText editTextFaceDetectedTime;  //人脸检测稳定
    private EditText editTextFaceUnDetectedTime; //人脸消失过滤
    private EditText editTextFaceResumeTime;    //断点续播超时时间
    private EditText editTextShortVideoTimeOut;  //短视频超时时间
    private LinearLayout alarmDetailConstraintLayout;
    private LinearLayout alarmStartTimeLL;
    private LinearLayout alarmEndTimeLL;
    private EditText alarmIntervalEdit;
    private EditText alarmValidTimeEdit;
    private TextView alarmStartTextView;
    private TextView alarmEndTextView;
    private Switch switchTempOnOff;
    private Switch alarmSwitch;

    private EditText editTextFirstFinish;    //断点续播超时时间
    private EditText editText2ndFinish;  //短视频超时时间
    NumberPicker numberFirstFinish;
    NumberPicker number2ndFinish;

    private Context mContext;
    private TextView textViewVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_setting);
        mContext = this;
        RestartAlarmWatcher.cancelAlarms();  //

        alarmDetailConstraintLayout = findViewById(R.id.cl_alarm_detail);
        alarmIntervalEdit = findViewById(R.id.et_alarm_interval);
        alarmValidTimeEdit = findViewById(R.id.et_alarm_valid_time);
        alarmStartTextView = findViewById(R.id.et_alarm_start_time);
        alarmEndTextView = findViewById(R.id.et_alarm_end_time);
        alarmEndTimeLL = findViewById(R.id.ll_alarm_end_time);
        btn_change_password = findViewById(R.id.btn_change_password);
        alarmStartTimeLL = findViewById(R.id.ll_alarm_start_time);
        alarmSwitch = findViewById(R.id.switch_alarm);
        alarmStartTextView.setText(SPUtils.getInt(mContext, Config.ALARM_NOTICE_START_TIME_HOUR, 0) + ":" + SPUtils.getInt(mContext, Config.ALARM_NOTICE_START_TIME_MINUTE, 0));
        alarmEndTextView.setText(SPUtils.getInt(mContext, Config.ALARM_NOTICE_END_TIME_HOUR, 0) + ":" + SPUtils.getInt(mContext, Config.ALARM_NOTICE_END_TIME_MINUTE, 0));
        alarmStartTextView.setOnClickListener(this);
        alarmEndTextView.setOnClickListener(this);
        alarmStartTimeLL.setOnClickListener(this);
        alarmEndTimeLL.setOnClickListener(this);
        btn_change_password.setOnClickListener(this);
        alarmValidTimeEdit.setText(SPUtils.getFloat(mContext, Config.ALARM_NOTICE_VALID_TIME, 1f) + "");
        alarmIntervalEdit.setText(SPUtils.getFloat(mContext, Config.ALARM_NOTICE_INTERVAL, 1f) + "");

        btn_return = (Button) findViewById(R.id.btn_return);
        btn_log = (Button) findViewById(R.id.btn_logView);
        btn_detail_log = (Button) findViewById(R.id.btn_detail_logView);
        editTextEmailAddr = (EditText) findViewById(R.id.editTextEmailAddress);
        editTextErrorTemp = (EditText) findViewById(R.id.editErrorTemp);
        editTextFaceDetectedTime = (EditText) findViewById(R.id.editFaceDetected);
        editTextFaceUnDetectedTime = (EditText) findViewById(R.id.editFaceDissapper);
        editTextFaceResumeTime = (EditText) findViewById(R.id.editResumeTime);

        editTextShortVideoTimeOut = (EditText) findViewById(R.id.editShortVideoTimeout);

        switchTempOnOff = (Switch) findViewById(R.id.switch_tempEn);
        textViewVer = (TextView) findViewById(R.id.apkVerion);

        editTextFirstFinish = (EditText) findViewById(R.id.editFirstFinish);
        editText2ndFinish = (EditText) findViewById(R.id.edit2ndFinished);
        //numberFirstFinish = (NumberPicker) findViewById(R.idgn.firstFinishNumber);
        //number2ndFinish = (NumberPicker) findViewById(R.id.secondFinishNumber);


        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WashingReportListActivity.class);
                mContext.startActivity(intent);
            }
        });

        btn_detail_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WashingReportDetailListActivity.class);
                mContext.startActivity(intent);
            }
        });

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        switchTempOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    SPUtils.putInt(mContext, Config.CFGTempFunctionEn, 1);
                } else {
                    SPUtils.putInt(mContext, Config.CFGTempFunctionEn, 0);
                }
            }
        });

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SPUtils.putInt(mContext, Config.IS_OPEN_ALARM_NOTICE, 1);
                    alarmDetailConstraintLayout.setVisibility(View.VISIBLE);
                } else {
                    SPUtils.putInt(mContext, Config.IS_OPEN_ALARM_NOTICE, 0);
                    alarmDetailConstraintLayout.setVisibility(View.GONE);
                }
            }
        });

        if (SPUtils.getInt(mContext, Config.IS_OPEN_ALARM_NOTICE, Config.DefTempFunctionEn) > 0) {
            alarmDetailConstraintLayout.setVisibility(View.VISIBLE);
            alarmSwitch.setChecked(true);
        } else {
            alarmDetailConstraintLayout.setVisibility(View.GONE);
            alarmSwitch.setChecked(false);
        }

        initView();

        try {
            PackageManager pm = this.getPackageManager();

            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            if (textViewVer != null) {
                textViewVer.setText(pi.versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initView() {
        if (SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {
            switchTempOnOff.setChecked(true);
        } else {
            switchTempOnOff.setChecked(false);
        }

        editTextEmailAddr.setText(SPUtils.getString(mContext, Config.CFGWashingZEmailAddr, Config.DefWashingZEmailRXAddr));
        editTextErrorTemp.setText(String.valueOf(SPUtils.getFloat(mContext, Config.CFGErrorTempCFG, Config.DefErrorTempValue)));

        editTextFaceDetectedTime.setText(String.valueOf((float) (SPUtils.getLong(mContext, Config.CFGFaceNewEventTime, Config.DefFaceNewEventTime) / (float) 1000L)));
        editTextFaceUnDetectedTime.setText(String.valueOf((float) (SPUtils.getLong(mContext, Config.CFGFaceDisappearEventTime, Config.DefFaceDisappearEventTime) / (float) 1000L)));
        editTextShortVideoTimeOut.setText(String.valueOf((float) (SPUtils.getLong(mContext, Config.CFGFaceShortVIDEOTime, Config.DefFaceShortVideoTime) / (float) (60 * 60 * 1000L))));
        editTextFaceResumeTime.setText(String.valueOf((float) (SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime) / (float) (60 * 1000L))));

        Log.d(TAG, SPUtils.getInt(mContext, Config.CFGLongWashingFinishTime, Config.DefLongWashingFinishTime) + " " +
                SPUtils.getInt(mContext, Config.CFGShortWashingFinishTime, Config.DefShortWashingFinishTime));
        if (editTextFirstFinish != null) {
            editTextFirstFinish.setText(String.valueOf(SPUtils.getInt(mContext, Config.CFGLongWashingFinishTime, Config.DefLongWashingFinishTime)));
            editText2ndFinish.setText(String.valueOf(SPUtils.getInt(mContext, Config.CFGShortWashingFinishTime, Config.DefShortWashingFinishTime)));
        }

        if (numberFirstFinish != null) {
            numberFirstFinish.setMinValue(Config.MinLongWashingFinishTime);
            numberFirstFinish.setMaxValue(Config.MaxLongWashingFinishTime);
            numberFirstFinish.setValue(SPUtils.getInt(mContext, Config.CFGLongWashingFinishTime, Config.DefLongWashingFinishTime));
        }
        if (number2ndFinish != null) {
            number2ndFinish.setMinValue(Config.MinShortWashingFinishTime);
            number2ndFinish.setMaxValue(Config.MaxShortWashingFinishTime);
            number2ndFinish.setValue(SPUtils.getInt(mContext, Config.CFGShortWashingFinishTime, Config.DefShortWashingFinishTime));
        }
    }

    protected void onDestroy() {
        SPUtils.putString(mContext, Config.CFGWashingZEmailAddr, editTextEmailAddr.getText().toString());
        {
            Float errorTempConfig = Float.valueOf(editTextErrorTemp.getText().toString());
            if (errorTempConfig > 0) {
                SPUtils.putFloat(mContext, Config.CFGErrorTempCFG, errorTempConfig);
            } else {

            }
        }
        {
            Long faceDectedTime = (long) (Float.valueOf(editTextFaceDetectedTime.getText().toString()) * (long) 1000);
            if (faceDectedTime >= Config.MinFaceNewEventTime) {
                SPUtils.putLong(mContext, Config.CFGFaceNewEventTime, faceDectedTime);
            }
        }
        {
            Long faceUnDectedTime = (long) (Float.valueOf(editTextFaceUnDetectedTime.getText().toString()) * 1000);
            if (faceUnDectedTime >= Config.MinFaceDisappearEventTime) {
                SPUtils.putLong(mContext, Config.CFGFaceDisappearEventTime, faceUnDectedTime);
            }
        }
        {
            Long faceUnDectedTime = (long) (Float.valueOf(editTextFaceResumeTime.getText().toString()) * 60 * 1000);
            if (faceUnDectedTime >= Config.MinFaceResumeTime) {
                SPUtils.putLong(mContext, Config.CFGFaceResumeTime, faceUnDectedTime);
            }
        }
        {
            Long shortVideoTimeout = (long) (Float.valueOf(editTextShortVideoTimeOut.getText().toString()) * 60 * 60 * 1000);

            if (shortVideoTimeout >= Config.MinFaceShortVideoTime) {
                SPUtils.putLong(mContext, Config.CFGFaceShortVIDEOTime, shortVideoTimeout);
            }
        }

        if (editTextFirstFinish != null) {
            int temp = (int) (Integer.valueOf(editTextFirstFinish.getText().toString()));
            if (temp >= 1) {
                SPUtils.putInt(mContext, Config.CFGLongWashingFinishTime, temp);
            }
        }

        if (editText2ndFinish != null) {
            int temp = (int) (Integer.valueOf(editText2ndFinish.getText().toString()));
            if (temp >= 1) {
                SPUtils.putInt(mContext, Config.CFGShortWashingFinishTime, temp);
            }
        }

        if (numberFirstFinish != null) {
            SPUtils.putInt(mContext, Config.CFGLongWashingFinishTime, numberFirstFinish.getValue());
            SPUtils.putInt(mContext, Config.CFGShortWashingFinishTime, number2ndFinish.getValue());
        }

        Float alarmInterval = Float.valueOf(alarmIntervalEdit.getText().toString());
        Float alarmValid = Float.valueOf(alarmValidTimeEdit.getText().toString());
        if (alarmInterval > 1)
            SPUtils.putFloat(mContext, Config.ALARM_NOTICE_INTERVAL, alarmInterval);
        if (alarmValid > 1)
            SPUtils.putFloat(mContext, Config.ALARM_NOTICE_VALID_TIME, alarmValid);

        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_password:
                Intent intent = new Intent(mContext, ChangeAdminPassActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.et_alarm_start_time:
            case R.id.ll_alarm_start_time:
                TimePickerDialog tp = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SPUtils.putInt(mContext, Config.ALARM_NOTICE_START_TIME_HOUR, hourOfDay);
                        SPUtils.putInt(mContext, Config.ALARM_NOTICE_START_TIME_MINUTE, minute);
                        alarmStartTextView.setText(hourOfDay + ":" + minute);
                    }
                }, SPUtils.getInt(mContext, Config.ALARM_NOTICE_START_TIME_HOUR, 0), SPUtils.getInt(mContext, Config.ALARM_NOTICE_START_TIME_MINUTE, 0), true);//12：钟表初始小时数，0：钟表初始分钟数
                tp.show();
                break;
            case R.id.et_alarm_end_time:
            case R.id.ll_alarm_end_time:
                TimePickerDialog tp1 = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SPUtils.putInt(mContext, Config.ALARM_NOTICE_END_TIME_HOUR, hourOfDay);
                        SPUtils.putInt(mContext, Config.ALARM_NOTICE_END_TIME_MINUTE, minute);
                        alarmEndTextView.setText(hourOfDay + ":" + minute);
                    }
                }, SPUtils.getInt(mContext, Config.ALARM_NOTICE_END_TIME_HOUR, 0), SPUtils.getInt(mContext, Config.ALARM_NOTICE_END_TIME_MINUTE, 0), true);//12：钟表初始小时数，0：钟表初始分钟数
                tp1.show();
                break;
        }
    }
}
