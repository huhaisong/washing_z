package com.example.hu.mediaplayerapk.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.application.MyApplication;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.broadcast.HumanReceive;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dao.WashingReportManager;
import com.example.hu.mediaplayerapk.dialog.ChooseDialog;
import com.example.hu.mediaplayerapk.dialog.WashingChooseDialog;
import com.example.hu.mediaplayerapk.emailUtil.mailSenderUtil;
import com.example.hu.mediaplayerapk.model.DrawInfo;
import com.example.hu.mediaplayerapk.model.FaceTemper;
import com.example.hu.mediaplayerapk.model.MainActivityPlayModel;
import com.example.hu.mediaplayerapk.model.MainGestureListener;
import com.example.hu.mediaplayerapk.model.TouchModel;
import com.example.hu.mediaplayerapk.receiver.PistaEyesReceiver;
import com.example.hu.mediaplayerapk.service.BluetoothService;

import com.example.hu.mediaplayerapk.service.WorkTimerService;
import com.example.hu.mediaplayerapk.ui.popupWindow.VolumeAndLightPop;
import com.example.hu.mediaplayerapk.usb_copy.USBCopyActivity;
import com.example.hu.mediaplayerapk.usb_copy.USBCopyTask;
import com.example.hu.mediaplayerapk.usb_copy.USBReceive;
import com.example.hu.mediaplayerapk.util.DrawHelper;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.Logger;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.example.hu.mediaplayerapk.util.TimeUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.util.runtimepermissions.PermissionsManager;
import com.example.hu.mediaplayerapk.util.runtimepermissions.PermissionsResultAction;
import com.example.hu.mediaplayerapk.widget.FaceRectView;
import com.rockchip.Gpio;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.hu.mediaplayerapk.model.MainActivityPlayModel.BeaconEventNo;
import static com.example.hu.mediaplayerapk.model.MainActivityPlayModel.eventNo;
import static com.example.hu.mediaplayerapk.ui.activity.WashingChooseActivity.ToneFullPath;
import static com.example.hu.mediaplayerapk.util.FileUtils.checkHaveFile;
import static com.example.hu.mediaplayerapk.util.GoToHome.goToHome;
import static com.example.hu.mediaplayerapk.util.WorkTimeUtil.checkIsWorkTime;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MainActivity extends com.example.hu.mediaplayerapk.ui.activity.BaseActivity {

    public static final int WASHING_SELECTED_T40 = 1;
    public static final int WASHING_SELECTED_T70 = 2;
    public static final int WASHING_SELECTED_NONE = 0;
    public static final int EVENT_T70_FILE = -1;  //First one is T70
    public static final int EVENT_T40_FILE = 0;  //Second one is T40

    public static final int MESSAGE_WHAT_ALARM = 5555;

    private HumanReceive receiver = null;
    public static boolean isStartMotionCheck = false;
    public static boolean isPlayingBeaconEvent = false;  //???????????????beacon?????????
    public static boolean isECO;
    public static int beaconTagNo;
    public static int prepareBeaconTagNo;  //?????????????????????????????????????????????
    public static boolean enableWashingSelect = true;  //????????????washing????????????
    public static int WashingSelected = WASHING_SELECTED_NONE;  //  ?????????WASHING_SELECTED_T70?????????openService???????????????beacon
    private static Context mContext;
    private WashingChooseDialog ChooseDialog;
    private FaceRectView faceRectView;
    private View tempWhiteView;
    private FaceTemper mFaceTemper;

    private void setScreenBrightnessOff() {
        Log.e(TAG, "setScreenBrightnessOff: ");
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.0f;//Float.valueOf(0.0f);
        getWindow().setAttributes(lp);
    }

    private void setScreenBrightnessOn() {
        Log.e(TAG, "setScreenBrightnessOn: ");
        //????????????activity???????????????
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //0???1,????????????????????????
        lp.screenBrightness = getSystemBrightness() / 255.0f;
        getWindow().setAttributes(lp);
    }

    private int getSystemBrightness() {
        //??????????????????,?????????????????????255
        return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }

    public static boolean isPlaying() {
        if (mainActivityPlayModel != null) {
            return mainActivityPlayModel.isPlaying();
        }
        return false;
    }

    public int getCurPlayNumber() {
        if (mainActivityPlayModel != null) {
            return mainActivityPlayModel.getCurrentNum();
        }
        return 0;
    }

    private static final String TAG = "MainActivity";
    private static MainActivityPlayModel mainActivityPlayModel;
    public static boolean isCanTimeStart = true;   //??????eco==true ??????????????????????????????
    //public static int OPEN_OPENCV = 3333;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1111) {//1111????????????
                setScreenBrightnessOff();
                try {
                    Gpio.setLedRed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mainActivityPlayModel != null) {
                    //  Log.e(TAG, "handleMessage: screenOff");
                    mainActivityPlayModel.close();
                }
            } else if (msg.what == 2222 && isCanTimeStart) {//2222?????????
                setScreenBrightnessOn();
                try {
                    Gpio.setLedGreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mainActivityPlayModel != null && !mainActivityPlayModel.isPlaying()) {
                    //  Log.e(TAG, "handleMessage: screenOn");
                    Log.e(TAG, "handleMessage: ---------mainActivityPlayModel.startPlay()" + mainActivityPlayModel.startPlay());
                }
           /* } else if (msg.what == OPEN_OPENCV) {
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, MainActivity.this, mLoaderCallback);
                } else {
                    Log.d(TAG, "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }*/
            } else if (msg.what == 3333 && isCanTimeStart) {//3333????????????
                Log.e(TAG, "handleMessage: what == 3333");
                setScreenBrightnessOn();
                try {
                    Gpio.setLedGreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mainActivityPlayModel != null) {
                    //  Log.e(TAG, "handleMessage: screenOn");
                    Log.e(TAG, "handleMessage: ---------mainActivityPlayModel.startPlay()" + mainActivityPlayModel.startPlay());
                }
            } else if (msg.what == MESSAGE_WHAT_ALARM) {
                if (mainActivityPlayModel != null) {
                    Log.d(TAG, "MESSAGE_WHAT_ALARM");
                    mainActivityPlayModel.noticeAlarm();
                }
            }
            return false;
        }


    });

    private ServiceConnection workTimeServiceConnection = new WorkTimeServiceConnection();


    private class WorkTimeServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.e(TAG, "WorkTimeServiceConnection: ");
            WorkTimerService.WorkTimerBinder workTimerBinder = (WorkTimerService.WorkTimerBinder) binder;
            WorkTimerService mWorkTimerService = workTimerBinder.getWorkTimerService();
            mWorkTimerService.setHandler(mHandler);
            mWorkTimerService.startCheckTime();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: ");
        }
    }

    private VolumeAndLightPop volumeAndLightPop;
    MainGestureListener mainGestureListener;
    GestureDetector mGestureDetector;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        mContext = this;
        mainActivityPlayModel = new MainActivityPlayModel(this, mHandler);
        volumeAndLightPop = new VolumeAndLightPop(this, mainActivityPlayModel);
        mainGestureListener = new MainGestureListener(this, volumeAndLightPop);
        mGestureDetector = new GestureDetector(this, mainGestureListener);
        faceRectView = (FaceRectView) findViewById(R.id.face_rect_view);
        tempWhiteView = findViewById(R.id.temp_stroke);
        touchModel = new TouchModel(this);
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BLUETOOTH_BROADCAST_NAME);
        registerReceiver(bluetoothActBroadcastReceiver, bluetoothFilter);
        ((MyApplication) this.getApplicationContext()).setOpen(true);  //
        // ????????????BroiadcastReceiver??????
        receiver = new HumanReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HumanReceive.MSG);
        registerReceiver(receiver, filter);
        receiver.setHumanSensorListener(new HumanReceive.HRInterface() {
            @Override
            public void humanSensorCbk() {
                //????????????
                if (isStartMotionCheck) {
                    human_sensing_hook(-1, false);
                }
            }
        });

        if (SPUtils.getInt(this, Config.CFGHasCopyAssetFile) != 1) {
            FileUtils.copyAssets(MainActivity.this, "choosing.wav", ToneFullPath);
            SPUtils.putInt(this, Config.CFGHasCopyAssetFile, 1);
        }


      /*  Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandlerTest.sendEmptyMessage(1);
            }
        }, 5000, 400);//??????????????????handler??????????????????,?????????????????????????????????,??????????????????
        random = new Random();*/
    }

  /*  Random random;
    private Handler mHandlerTest = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int x1 = random.nextInt(100);
            int x2 = random.nextInt(100);
            Rect rect = new Rect(x1, x1, 100+x2, 100+x2);
            DrawInfo drawinfo = new DrawInfo(rect, 1, 2, 3, "ID");
            drawFace(drawinfo);
        }
    };*/

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus == true) {
            if (mFaceTemper != null) {
                mFaceTemper.close();
            }
            mFaceTemper = new FaceTemper(mContext, faceRectView, tempWhiteView);
        }
        return;
    }

    private TouchModel touchModel;
    private USBReceive usbBroadCastReceive;

    private boolean isOnResume = false;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity OnResume\n\n");
        openService();
        isOnResume = true;
        mainActivityPlayModel.onResume();

        receiver = new HumanReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HumanReceive.MSG);
        registerReceiver(receiver, filter);
        receiver.setHumanSensorListener(new HumanReceive.HRInterface() {
            @Override
            public void humanSensorCbk() {
                //????????????
                if (isStartMotionCheck) {
                    human_sensing_hook(-1, false);
                }
            }
        });
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BLUETOOTH_BROADCAST_NAME);
        bluetoothFilter.addAction(WASHING_SELECT_BROADCAST_NAME);
        registerReceiver(bluetoothActBroadcastReceiver, bluetoothFilter);

        registerHumanDetectingBroadcast();
    }

    public boolean isOnResume() {
        return isOnResume;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "MainActivity onPause\n\n");
        if (mFaceTemper != null) {
            mFaceTemper.close();
        }

        unregisterReceiver(receiver);
        unregisterReceiver(bluetoothActBroadcastReceiver);
        //bluetoothActBroadcastReceiver = null;
        receiver = null;

        unregisterHumanDetectingBroadcast();
        super.onPause();
        closeService();
        isOnResume = false;
    }

    Intent beaconIntent;

    private void openService() {
        isCanTimeStart = true;
        //??????U???????????????
        usbBroadCastReceive = new USBReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addDataScheme("file");
        registerReceiver(usbBroadCastReceive, filter);

        //??????????????????????????? ??????????????????
        Intent intent = new Intent(MainActivity.this, WorkTimerService.class);
        this.bindService(intent, workTimeServiceConnection, Context.BIND_AUTO_CREATE);

        beaconIntent = new Intent(MainActivity.this, BluetoothService.class);
        //??????beaconservice???
        if (SPUtils.getInt(MainActivity.this, Config.BEACON_MODE_STATE) < 0) {
            startService(beaconIntent);
        }

    }

    private Timer alarmTimer;

    private void closeService() {
        Log.e(TAG, "closeService: ");
        //????????????????????????????????????????????????
        mainActivityPlayModel.onPause();
        unbindService(workTimeServiceConnection);

        //beaconservice???
        if (beaconIntent != null)
            stopService(beaconIntent);

        //u???????????????
        unregisterReceiver(usbBroadCastReceive);
        //??????????????????
        mainActivityPlayModel.close();
        mainActivityPlayModel.setEVENT(false);
        mainActivityPlayModel.setPlayingBeacon(false);

        setScreenBrightnessOn();
        Gpio.setLedGreen();
        if (volumeAndLightPop != null) {
            volumeAndLightPop.onPause();
        }
    }

    private void human_sensing_hook(int number, boolean isButton) {
        Log.e(TAG, "human_sensing_hook\n\n\n ");
        eventNo = number;
        if (checkIsWorkTime()) {
            isStartMotionCheck = false;
            if (mainActivityPlayModel != null) {
                if (SPUtils.getInt(MainActivity.this, Config.ECO_MODE_STATE) >= 0) {
                    isECO = true;
                    isCanTimeStart = false;
                } else {
                    isCanTimeStart = true;
                }

                long timeSpan = System.currentTimeMillis() - MainActivityPlayModel.reStartTime;
                if (timeSpan > SPUtils.getInt(MainActivity.this, Config.ECO_MODE_STATE) * 1000) {
                    setScreenBrightnessOn();
                    try {
                        Gpio.setLedGreen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isPlayingBeaconEvent) {
                        mainActivityPlayModel.startPlay(true, isButton);
                    }
                    Log.e(TAG, "onKeyDown: start play ");
                }
            }
        }
    }

    //IR usbTag ??????
    private void UsbTagHook(int number) {
        if (number == 0) {
            //??????
            Intent intent = new Intent(BLUETOOTH_BROADCAST_NAME);
            intent.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_NO_PERSION);
            sendBroadcast(intent);
        } else if (number == 1) {
            //??????
            Intent intent = new Intent(BLUETOOTH_BROADCAST_NAME);
            intent.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_PERSION);
            sendBroadcast(intent);
        } else {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            //????????????
            //if (isStartMotionCheck) {
            if (keyCode == KeyEvent.KEYCODE_F1) {
                human_sensing_hook(-1, true);
            }
            if (keyCode == KeyEvent.KEYCODE_F2) {
                human_sensing_hook(0, true);
            }
            if (keyCode == KeyEvent.KEYCODE_F3) {
                human_sensing_hook(1, true);
            }

            if (keyCode == KeyEvent.KEYCODE_0) {
                UsbTagHook(0);
            }

            if (keyCode == KeyEvent.KEYCODE_1) {
                UsbTagHook(1);
            }
            //}
            goToHome(keyCode, event, this);

            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getRepeatCount() > 30) {
                    //mainActivityPlayModel.startToOSD();
                }
            }
            //Log.d(TAG, "key:"+ keyCode);
            volumeAndLightPop.onkeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchModel.onTouchEvent(event);

        if (mainActivityPlayModel != null) {
            mainActivityPlayModel.cancelCurAlarm();
        }
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed: ");
        volumeAndLightPop.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        if (mFaceTemper != null) {
            mFaceTemper.close();
        }
        if (bluetoothActBroadcastReceiver != null) {
            unregisterReceiver(bluetoothActBroadcastReceiver);
        }
        bluetoothActBroadcastReceiver = null;
        receiver = null;
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        mainActivityPlayModel.onDestroy();
    }


    //*******************************************************
    PistaEyesReceiver humanDetectingReceiver;

    private void registerHumanDetectingBroadcast() {
        IntentFilter intentFilter;
        humanDetectingReceiver = new PistaEyesReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(PistaEyesReceiver.ACTION);
        intentFilter.addAction(PistaEyesReceiver.ACTION_NO_PERSON);
        registerReceiver(humanDetectingReceiver, intentFilter);

        humanDetectingReceiver.setFaceDetectListener((intent, ID, gender, rect) -> {
                    Log.d(TAG, "FaceDetectCallback " + intent);
                    if (intent == Config.BEACON_TAG_PERSION) {
                        FaceDetectPlayingStateMachine(Config.BEACON_TAG_PERSION, ID, gender, rect);
                    } else if (intent == Config.BEACON_TAG_NO_PERSION) {
                        FaceDetectPlayingStateMachine(Config.BEACON_TAG_NO_PERSION, ID, gender, rect);
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

    private WashingReportItem mWashingReportItem = new WashingReportItem();

    private void FaceDetectPlayingStateMachine(int intentNo, String ID, String gender, Rect rect) {

        if (isPlayingBeaconEvent) {//??????????????????beacon?????????????????????beacon?????????5?????????????????????beacon???
            if (intentNo == Config.BEACON_TAG_NO_PERSION) {//??????
                if (mFaceTemper != null) {
                    mFaceTemper.close();
                }
                if (beaconTagNo == Config.BEACON_TAG_NO_PERSION && intentNo == Config.BEACON_TAG_NO_PERSION) {  //?????????????????????????????????
                    return;
                }

                if (SPUtils.getInt(mContext, Config.CFGInterrupptingFinishEN, Config.DefInterruptingFinishEN) == 1) {
                    int curPosition = 0;
                    int FinishThreshold = 0;
                    curPosition = mainActivityPlayModel.getCurrentPosition();
                    curPosition = curPosition / 1000;
                    if (mainActivityPlayModel.getCurrentNum() == EVENT_T70_FILE) {
                        FinishThreshold = SPUtils.getInt(mContext, Config.CFGLongWashingFinishTime, Config.DefLongWashingFinishTime);
                    } else if (mainActivityPlayModel.getCurrentNum() == EVENT_T40_FILE) {
                        FinishThreshold = SPUtils.getInt(mContext, Config.CFGShortWashingFinishTime, Config.DefShortWashingFinishTime);
                    }

                    Log.d(TAG, "curPosition: " + curPosition + "  FinishThreshold:" + FinishThreshold + " getCurrentNum " + mainActivityPlayModel.getCurrentNum());
                    if ((curPosition < 0) || (curPosition >= FinishThreshold)) {
                        intentNo = Config.BEACON_TAG_PERSION;  //Defined as finishing
                    }
                }

                if (intentNo == Config.BEACON_TAG_PERSION) {
                    //success washing
                    FaceManagerUtil.savePlayRecord(ID, -1, -1);
                    //Logger.WashingLoggerAppend(ID, FaceManagerUtil.getGenderStr(ID), 1, 0, 0, 0);
                    MainActivity.pushNewPlayEvent(ID,
                            FaceManagerUtil.getGenderStr(ID), 0, false);
                } else {
                    if (mainActivityPlayModel != null) {
                        FaceManagerUtil.savePlayRecord(ID, mainActivityPlayModel.getCurrentNum(), mainActivityPlayModel.getCurrentPosition());
                    }

                    //??????????????????
                    //Logger.WashingLoggerAppend(ID, gender, 0, 1, 0, 0);  //???????????????1
                    pushNewPlayEvent(ID, gender, 1, false);

                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon(false);

                }
            } else if (intentNo == Config.BEACON_TAG_PERSION && beaconTagNo == Config.BEACON_TAG_NO_PERSION) {//?????????????????????????????????
                /*if ((FaceManagerUtil.FaceRecordGetLastTime(ID) < SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime))
                        && ((FaceManagerUtil.getPlayTimeRecord(ID) != -1))) {
                    //????????????????????????????????????
                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon(true, FaceManagerUtil.getPlayNumRecord(ID), FaceManagerUtil.getPlayTimeRecord(ID));
                } else */
                {
                    PopTempCaptureActivity(intentNo, ID, gender);

                    //Logger.WashingLoggerAppend(ID, gender, 1, 0, 0, 0);
                    //pushNewPlayEvent(ID, gender,0);
                }
            }
        } else {
            if (intentNo == Config.BEACON_TAG_PERSION) {//??????
                /*if ((FaceManagerUtil.FaceRecordGetLastTime(ID) < SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime))
                        && ((FaceManagerUtil.getPlayTimeRecord(ID) != -1))) {
                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon(true, FaceManagerUtil.getPlayNumRecord(ID), FaceManagerUtil.getPlayTimeRecord(ID));
                } else */
                {
                    PopTempCaptureActivity(intentNo, ID, gender);
                }
                //Logger.WashingLoggerAppend(ID, gender, 1, 0, 0, 0);
                //pushNewPlayEvent(ID, gender,0);
            } else {
                if (mFaceTemper != null) {
                    mFaceTemper.close();
                }
            }
        }
    }

    public static void pushNewPlayEvent(String ID, String gender, int isInterrupt, boolean callFromFinish) {
        int curPlayNum = -1;
        boolean isFirstMovie = false;
        boolean isInternalError = false;
        if (ID == null) {
            return;
        }
        FaceManagerUtil.setPlayEvent(ID);

        if (mainActivityPlayModel != null) {
            curPlayNum = mainActivityPlayModel.getCurrentNum();
        }

        if (callFromFinish == true) {
            curPlayNum -= 1;  //??????????????????????????????????????????1
        }

        //??????????????????
        WashingReportItem mWashingReportItem = new WashingReportItem();
        mWashingReportItem.setIsPlayInterrupt(isInterrupt);
        mWashingReportItem.setFaceID(ID);
        if (gender.equalsIgnoreCase("1.0")) {
            mWashingReportItem.setIsLadyOrMen(1);
        } else if (gender.equalsIgnoreCase("-1.0")) {
            mWashingReportItem.setIsLadyOrMen(0);
        }

        if (SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {
            mWashingReportItem.setLastTemp((double) MyApplication.getCurUseFulTemp());
        }

        if (FaceManagerUtil.FaceRecordLastTimeIntervalError(ID) == true) {
            mWashingReportItem.setIsLongInterval(1);
            isInternalError = true;
        } else {
            mWashingReportItem.setIsLongInterval(0);
            isInternalError = false;
        }
        Log.d(TAG, "pushNewPlayEvent " + ID + " curPlayNum " + curPlayNum);

        if (curPlayNum == EVENT_T70_FILE) {
            mWashingReportItem.setPlayNum(1);
            isFirstMovie = true;
        } else if (curPlayNum == EVENT_T40_FILE) {
            mWashingReportItem.setPlayNum(2);
        }
        WashingReportManager.getInstance(mContext).insertOrReplace(mWashingReportItem);

        Logger.WashingITVLoggerAppend(ID, isFirstMovie, (isInterrupt == 1) ? false : true, MyApplication.getCurUseFulTemp() + "", isInternalError);
    }

    //**************************************************************


    public static final String BLUETOOTH_BROADCAST_NAME = "com.hu.bluetooth.BLUETOOTH_ACTION";
    public static final String WASHING_SELECT_BROADCAST_NAME = "com.hu.WASHING_SELECT_BROADCAST_NAME";

    public static final String BLUETOOTH_INT_EXTRA_NAME = "BLUETOOTH_ACTION_STRING_NAME";

    private BluetoothActBroadcastReceiver bluetoothActBroadcastReceiver = new BluetoothActBroadcastReceiver();

    private class BluetoothActBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int intentNo = intent.getIntExtra(BLUETOOTH_INT_EXTRA_NAME, -1);
            Log.e(TAG, "onReceive --------------1 onReceive: intentNo = " + intentNo + ",isPlayingBeaconEvent = " + isPlayingBeaconEvent);
           /* if (System.currentTimeMillis() - oldTime <= 2 * 1000) {
                Log.e(TAG, "onReceive: time is too close");
                return;
            }*/
            if (intent.getAction().equalsIgnoreCase(BLUETOOTH_BROADCAST_NAME)) {
                if (isPlayingBeaconEvent) {//??????????????????beacon?????????????????????beacon?????????5?????????????????????beacon???
                    if (intentNo == Config.BEACON_TAG_NO_PERSION) {//??????
                        if (beaconTagNo == Config.BEACON_TAG_NO_PERSION && intentNo == Config.BEACON_TAG_NO_PERSION) {  //?????????????????????????????????
                            return;
                        }
                        //???????????????????????????
                        String parentPath = Config.INTERNAL_FILE_ROOT_PATH + File.separator
                                + Config.PICKTURE_TEMP_FOLDER;

                        beaconTagNo = intentNo;
                        mainActivityPlayModel.startPlayBeacon();
                    } else if (intentNo == Config.BEACON_TAG_PERSION && beaconTagNo == Config.BEACON_TAG_NO_PERSION) {//?????????????????????????????????
                        if (enableWashingSelect == false) {
                            beaconTagNo = intentNo;
                            mainActivityPlayModel.startPlayBeacon();
                        } else {
                            PopWashingChooseDialog(intentNo);
                        }
                    }
                } else {
                    if (intentNo == Config.BEACON_TAG_PERSION) {//??????
                        if (enableWashingSelect == false) {
                            beaconTagNo = intentNo;
                            mainActivityPlayModel.startPlayBeacon();
                        } else {
                            PopWashingChooseDialog(intentNo);
                        }
                    }
                }
            }

        }
    }

    private void PopTempCaptureActivity(int intentNo, String ID, String gender) {
        if (SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {
            prepareBeaconTagNo = intentNo;
            //open faceTemper
            if (mFaceTemper != null) {
                mFaceTemper.open();
            }
        }
        //????????????????????????
        if ((FaceManagerUtil.FaceRecordGetLastTime(ID) < SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime))
                && ((FaceManagerUtil.getPlayTimeRecord(ID) != -1))) {
            beaconTagNo = intentNo;
            mainActivityPlayModel.startPlayBeacon(true, FaceManagerUtil.getPlayNumRecord(ID), FaceManagerUtil.getPlayTimeRecord(ID));
        } else {
            //???????????????????????????????????????????????????
            if (FaceManagerUtil.FaceRecordNeedLongWashing(ID) == true) {
                WashingSelected = WASHING_SELECTED_T70;
            } else {
                WashingSelected = WASHING_SELECTED_T40;
            }
            beaconTagNo = intentNo;
            BeaconEventNo = (WashingSelected == WASHING_SELECTED_T70) ? -1 : 0;  //??????????????????T70
            WashingSelected = WASHING_SELECTED_NONE;
            mainActivityPlayModel.startPlayBeacon(false);
        }
    }

    private void PopWashingChooseDialog(int intentNo) {

        prepareBeaconTagNo = intentNo;
        Intent myIntent = new Intent(MainActivity.this, WashingChooseActivity.class);
        startActivity(myIntent);


       /* prepareBeaconTagNo = intentNo;
        ChooseDialog = new WashingChooseDialog(MainActivity.this);
        WashingChooseDialog.ClickListenerInterface listen = new WashingChooseDialog.ClickListenerInterface() {
            @Override
            public void select(int i) {
                switch (i) {
                    case R.id.iv_washing_choose_yes:
                        beaconTagNo = prepareBeaconTagNo;
                        BeaconEventNo =-1;
                        mainActivityPlayModel.startPlayBeacon();
                        if (motionDetectorService != null) {
                            motionDetectorService.startDetect();
                        }
                        else
                        {
                            Log.e(TAG, "motionDetectorService == null");
                        }
                        WashingSelected = WASHING_SELECTED_NONE;
                        ChooseDialog.dismiss();
                        break;
                    case R.id.iv_washing_choose_no:
                        beaconTagNo = prepareBeaconTagNo;
                        BeaconEventNo = 0;  //??????????????????T70
                        mainActivityPlayModel.startPlayBeacon();
                        if (motionDetectorService != null) {
                            motionDetectorService.startDetect();
                        }
                        else
                        {
                            Log.e(TAG, "motionDetectorService == null");
                        }
                        WashingSelected = WASHING_SELECTED_NONE;
                        ChooseDialog.dismiss();
                        //finish();
                        break;
                    case 0:
                        ChooseDialog.dismiss();
                        break;
                }
            }
        };
        ChooseDialog.setClickListen(listen);
        ChooseDialog.show();*/

    }
}
