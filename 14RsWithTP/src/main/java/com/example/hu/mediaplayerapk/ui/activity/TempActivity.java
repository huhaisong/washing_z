package com.example.hu.mediaplayerapk.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.application.MyApplication;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.emailUtil.mailSenderUtil;
import com.example.hu.mediaplayerapk.model.DrawInfo;
import com.example.hu.mediaplayerapk.receiver.PistaEyesReceiver;
import com.example.hu.mediaplayerapk.util.DrawHelper;
import com.example.hu.mediaplayerapk.util.Logger;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.widget.FaceRectView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_NONE;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T40;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T70;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WashingSelected;

public class TempActivity extends com.example.hu.mediaplayerapk.ui.activity.BaseActivity {
    PistaEyesReceiver humanDetectingReceiver;

    private FaceRectView faceRectView;
    private DrawHelper drawHelper;
    private static final int NO_HUMAN_TIMEOUT = 0x1100;
    private static final int TEMP_READY_PLAY = 0x1101;
    private String CurID = "0";
    private String CurGender = "0";
    private float CurTemp = 0;
    private String CurTempStr = "0.0";
    private int CurTempIsError = 0;
    private Context mContext;
    private String TempTitle = "Temp: ";
    private static String TAG = "TempActivity";

    private Handler timeoutHandller = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NO_HUMAN_TIMEOUT:
                    drawFace(null);
                    break;

                case TEMP_READY_PLAY:
                    finishAndSelectPlay(CurID);
                    break;
            }
            return false;
        }
    });

    private void finishAndSelectPlay(String ID) {
        if (FaceManagerUtil.FaceRecordNeedLongWashing(ID) == true) {
            WashingSelected = WASHING_SELECTED_T70;
        } else {
            WashingSelected = WASHING_SELECTED_T40;
        }

        Logger.WashingLoggerAppend(ID, CurGender, 1, 0, CurTempIsError, CurTempStr);  //
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        faceRectView = (FaceRectView) findViewById(R.id.face_rect_view);
        mContext = this;
        // FaceRectInit(); //

        //registerHumanDetectingBroadcast();
        timeoutHandller.sendEmptyMessageDelayed(TEMP_READY_PLAY, 3 * 1000);
    }

    protected void onResume() {
        super.onResume();
        registerHumanDetectingBroadcast();
    }

    protected void onPause() {
        super.onPause();
        unregisterHumanDetectingBroadcast();
    }

    /*public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("TEST", "onKeyDown: keyCode = " + keyCode);

        return true;
    }*/

    private void registerHumanDetectingBroadcast() {
        IntentFilter intentFilter;
        humanDetectingReceiver = new PistaEyesReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(PistaEyesReceiver.ACTION);
        intentFilter.addAction(PistaEyesReceiver.ACTION_NO_PERSON);
        registerReceiver(humanDetectingReceiver, intentFilter);

        humanDetectingReceiver.setFaceDetectListener((intent, ID, gender, rect) -> {
            DrawInfo drawinfo = null;
            if (intent == Config.BEACON_TAG_NO_PERSION) {
                WashingSelected = WASHING_SELECTED_NONE;  //
                finish();
            } else {
                if (rect != null) {
                    CurID = ID;
                    if (CurTemp == 0) {
                        CurTemp = ((MyApplication) mContext.getApplicationContext()).getCurTemp();
                        if (CurTemp >= SPUtils.getFloat(mContext, Config.CFGErrorTempCFG, Config.DefErrorTempValue)) {
                            CurTempIsError = 1;
                            try {
                                Log.d(TAG, "raw " + TempTitle);
                                Log.d(TAG, "utf8 " + new String(TempTitle.getBytes("utf-8"), "UTF-8"));
                                Log.d(TAG, "gbk " + new String(TempTitle.getBytes("utf-8"), "gbk"));
                                Log.d(TAG, "utf16 " + new String(TempTitle.getBytes("utf-8"), "UTF-16"));
                                Log.d(TAG, "unicode " + new String(TempTitle.getBytes("utf-8"), "unicode"));
                                Log.d(TAG, "gb2312 " + new String(TempTitle.getBytes("utf-8"), "gb2312"));

                            } catch (UnsupportedEncodingException e) {

                            }
                            mailSenderUtil.sendErrorReport(mContext, "\nDateTime: " + TimeUtil.getCurrentFormatTime() + "\nFace ID: " + CurID + "\n" + TempTitle + CurTemp, null);
                            timeoutHandller.removeMessages(TEMP_READY_PLAY);
                            Toast.makeText(mContext, "Exception Temperature Data detected, Please contact manager!\n", Toast.LENGTH_LONG).show();
                            timeoutHandller.sendEmptyMessageDelayed(TEMP_READY_PLAY, 5 * 1000);
                        }

                    }

                    if (CurTemp != 0) {
                        java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.0");

                        CurTempStr = myformat.format(CurTemp);
                        drawinfo = new DrawInfo(rect, 1, 2, 3, myformat.format(CurTemp) + "Degree \n" + ID);
                    } else {
                        drawinfo = new DrawInfo(rect, 1, 2, 3, ID);
                    }

                    if (gender != null) {
                        CurGender = gender;
                    }
                    drawFace(drawinfo);
                } else {
                    drawFace(null);
                }
            }
        }
        );
    }

    private void unregisterHumanDetectingBroadcast() {
        if (humanDetectingReceiver != null) {
            unregisterReceiver(humanDetectingReceiver);
            humanDetectingReceiver = null;
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus == true) {
            FaceRectInit();
        }
        return;
    }

    public void FaceRectInit() {
        if (drawHelper == null) {
            drawHelper = new DrawHelper(640, 480, faceRectView.getWidth(), faceRectView.getHeight(), 0, Camera.CameraInfo.CAMERA_FACING_FRONT, false);
        }
    }

    public void drawFace(DrawInfo info) {
        if ((drawHelper == null) || (faceRectView == null)) {
            return;
        }

        if (faceRectView != null) {
            faceRectView.clearFaceInfo();
        }
        List<DrawInfo> drawInfoList = new ArrayList<>();
        if (info != null) {
            for (int i = 0; i < 1; i++) {
                drawInfoList.add(info);
            }

        }

        drawHelper.draw(faceRectView, drawInfoList);
    }
}
