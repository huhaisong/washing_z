package com.example.hu.mediaplayerapk.model;


import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.hu.mediaplayerapk.application.MyApplication;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dao.WashingReportManager;
import com.example.hu.mediaplayerapk.emailUtil.mailSenderUtil;
import com.example.hu.mediaplayerapk.receiver.PistaEyesReceiver;
import com.example.hu.mediaplayerapk.ui.activity.MainActivity;
import com.example.hu.mediaplayerapk.util.DrawHelper;
import com.example.hu.mediaplayerapk.util.Logger;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.widget.FaceRectView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.EVENT_T40_FILE;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.EVENT_T70_FILE;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_NONE;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T40;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T70;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WashingSelected;

/*
包含了人脸框和测温功能
在正中间显示一个固定的白色框，检测到人脸后显示人脸框
当人脸框大小和位置姐姐固定的白色框后显示温度值，1秒后消失
 */
public class FaceTemper {
    PistaEyesReceiver humanDetectingReceiver;

    private FaceRectView faceRectView;
    private DrawHelper drawHelper;
    private View tempWhiteView;
    private String TAG = "FaceTemper";
    private Context mContext;
    private String CurID = "0";
    private String CurGender = "0";
    private float CurTemp = 0;
    private String CurTempStr = "0.0";
    private int CurTempIsError = 0;
    private String TempTitle = "Temp: ";
    private final int NO_HUMAN_TIMEOUT = 0x1100;
    private final int TEMP_READY_PLAY = 0x1101;

    private final int FACETEMP_DETECTING = 0;  //判断人脸是否居中
    private final int FACETEMP_DETECTED = 1;  //人脸居中后人脸框消失、白色框消失、显示温度信息1秒钟
    private final int FACETEMP_FINISHED = 2;  //之后所有都消失
    private int faceTemperState = FACETEMP_DETECTING;
    private boolean debug = false;
    private boolean visible = false;
    //注意:left和right因为有摄像头左右镜像的处理在，都是以右边的距离为准
    private final Rect standardRect = new Rect(196, 90, 440, 420);  //640*480的面积
    private final Rect standardRectMin = new Rect(230, 134, 400, 350);  //，不能小于这个框640*480的面积

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


    //必须确保view已经初始化成功
    public FaceTemper(Context thisContext, FaceRectView thisView, View thisWhiteRect) {
        mContext = thisContext;
        faceRectView = thisView;
        tempWhiteView = thisWhiteRect;
        FaceRectInit();
    }

    //开始显示人脸框和白色框
    public void open() {
        visible = true;
        CurTemp = 0;

        faceTemperState = FACETEMP_DETECTING;
        tempWhiteView.setVisibility(View.VISIBLE);
        registerHumanDetectingBroadcast();
    }

    //关闭人脸框和白色框
    public void close() {

        unregisterHumanDetectingBroadcast();
        tempWhiteView.setVisibility(View.GONE);
        drawFace(null);
        visible = false;
    }

    public void FaceRectInit() {
        if (drawHelper == null) {
            drawHelper = new DrawHelper(640, 480, faceRectView.getWidth(), faceRectView.getHeight(), 0, Camera.CameraInfo.CAMERA_FACING_FRONT, false);
        }
    }

    private void finishAndSelectPlay(String ID) {
        close();
        /*Logger.WashingLoggerAppend(ID, CurGender, 1, 0, CurTempIsError, CurTempStr);  //


        //保存进数据库
        WashingReportItem mWashingReportItem = new WashingReportItem();
        mWashingReportItem.setFaceID(ID);
        if (CurGender.equalsIgnoreCase("1.0")) {
            mWashingReportItem.setIsLadyOrMen(1);
        } else if (CurGender.equalsIgnoreCase("-1.0")) {
            mWashingReportItem.setIsLadyOrMen(0);
        }
        if (((MainActivity) mContext).getCurPlayNumber() == EVENT_T70_FILE) {
            mWashingReportItem.setPlayNum(1);
        } else if (((MainActivity) mContext).getCurPlayNumber() == EVENT_T40_FILE) {
            mWashingReportItem.setPlayNum(2);
        }
        mWashingReportItem.setLastTemp(Double.valueOf(CurTempStr));
        mWashingReportItem.setTempValidCnt(CurTempIsError);
        WashingReportManager.getInstance(mContext).insertOrReplace(mWashingReportItem);*/
        //finish();
    }

    private void FaceStateMachine(Rect rect) {
        if (faceTemperState == FACETEMP_DETECTING) {
            if ((standardRect.contains(rect))&&(rect.contains(standardRectMin))) {
            /*if((rect.width() >= standardRectMin.width())&&(rect.width() <= standardRect.width())&&
                    (rect.height() >= standardRectMin.height())&&(rect.height() <= standardRect.height())&&
                    (rect.left <= standardRectMin.left)&&(rect.left >= standardRect.left)&&
                    (rect.top <= standardRectMin.top)&&(rect.top >= standardRect.top)
            ){*/
                faceTemperState = FACETEMP_DETECTED;

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
                        Toast.makeText(mContext, "Exception Temperature Data detected, Please contact manager!\n", Toast.LENGTH_LONG).show();
                    }

                    ((MyApplication) mContext.getApplicationContext()).setCurUsefulTemp(CurTemp);
                    close();
                    timeoutHandller.sendEmptyMessageDelayed(TEMP_READY_PLAY, 3 * 1000);
                    drawTemp();
                }
            }
        }

    }

    private void registerHumanDetectingBroadcast() {
        IntentFilter intentFilter;
        humanDetectingReceiver = new PistaEyesReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(PistaEyesReceiver.ACTION);
        intentFilter.addAction(PistaEyesReceiver.ACTION_NO_PERSON);
        mContext.registerReceiver(humanDetectingReceiver, intentFilter);

        humanDetectingReceiver.setFaceDetectListener((intent, ID, gender, rect) -> {
                    if(visible == false)
                    {
                        return;
                    }
                    DrawInfo drawinfo = null;
                    if (intent == Config.BEACON_TAG_NO_PERSION) {
                        //timeoutHandller.sendEmptyMessageDelayed(NO_HUMAN_TIMEOUT, 10);
                    } else {
                        if (rect != null) {
                            CurID = ID;
                            if (gender != null) {
                                CurGender = gender;
                            }

                            if (debug == false) {
                                if (faceTemperState == FACETEMP_DETECTING) {
                                    drawinfo = new DrawInfo(rect, 1, 2, 3, " ");
                                }
                            } else {
                                drawinfo = new DrawInfo(rect, 1, 2, 3, rect.toString());
                            }
                            drawFace(drawinfo);

                            FaceStateMachine(rect);

                        } else {
                            drawFace(null);
                        }
                    }
                }
        );
    }

    private void unregisterHumanDetectingBroadcast() {
        if (humanDetectingReceiver != null) {
            mContext.unregisterReceiver(humanDetectingReceiver);
            humanDetectingReceiver = null;
        }
    }

    private void drawTemp() {

        {
            DrawInfo drawinfo = null;
            java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.0");
            CurTempStr = myformat.format(CurTemp);
            drawinfo = new DrawInfo(standardRect, 1, 2, 3, myformat.format(CurTemp) + "Degree \n", true);
            drawFace(drawinfo);
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
