package com.example.hu.mediaplayerapk.ui.activity;

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
import com.example.hu.mediaplayerapk.broadcast.HumanReceive;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dialog.ChooseDialog;
import com.example.hu.mediaplayerapk.dialog.WashingChooseDialog;
import com.example.hu.mediaplayerapk.emailUtil.mailSenderUtil;
import com.example.hu.mediaplayerapk.model.DrawInfo;
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
    public static final int MESSAGE_WHAT_ALARM = 5555;

    private HumanReceive receiver = null;
    public static boolean isStartMotionCheck = false;
    public static boolean isPlayingBeaconEvent = false;  //是否在播放beacon的视频
    public static boolean isECO;
    public static int beaconTagNo;
    public static int prepareBeaconTagNo;  //在触发人感后弹出对话框是否需要
    public static boolean enableWashingSelect = true;  //是否使能washing弹框选择
    public static int WashingSelected = WASHING_SELECTED_NONE;  //  如果是WASHING_SELECTED_T70，则在openService的时候播放beacon
    private Context mContext;
    private WashingChooseDialog ChooseDialog;
    private FaceRectView faceRectView;
    private View tempWhiteView;

    private void setScreenBrightnessOff() {
        Log.e(TAG, "setScreenBrightnessOff: ");
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.0f;//Float.valueOf(0.0f);
        getWindow().setAttributes(lp);
    }

    private void setScreenBrightnessOn() {
        Log.e(TAG, "setScreenBrightnessOn: ");
        //设置当前activity的屏幕亮度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //0到1,调整亮度暗到全亮
        lp.screenBrightness = getSystemBrightness() / 255.0f;
        getWindow().setAttributes(lp);
    }

    private int getSystemBrightness() {
        //获取当前亮度,获取失败则返回255
        return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }

    public static boolean isPlaying() {
        if (mainActivityPlayModel != null) {
            return mainActivityPlayModel.isPlaying();
        }
        return false;
    }

    private static final String TAG = "MainActivity";
    private static MainActivityPlayModel mainActivityPlayModel;
    public static boolean isCanTimeStart = true;   //如果eco==true 那么久不能够唤醒屏幕
    //public static int OPEN_OPENCV = 3333;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1111) {//1111表示熄灭
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
            } else if (msg.what == 2222 && isCanTimeStart) {//2222表示亮
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
            } else if (msg.what == 3333 && isCanTimeStart) {//3333表示更新
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
        // 生成一个BroiadcastReceiver对象
        receiver = new HumanReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HumanReceive.MSG);
        registerReceiver(receiver, filter);
        receiver.setHumanSensorListener(new HumanReceive.HRInterface() {
            @Override
            public void humanSensorCbk() {
                //移动检测
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
        }, 5000, 400);//每隔一秒使用handler发送一下消息,也就是每隔一秒执行一次,一直重复执行
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

        if (tempWhiteView != null)
            tempWhiteView.setVisibility(View.GONE);
        receiver = new HumanReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HumanReceive.MSG);
        registerReceiver(receiver, filter);
        receiver.setHumanSensorListener(new HumanReceive.HRInterface() {
            @Override
            public void humanSensorCbk() {
                //移动检测
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
        //注册U盘更新广播
        usbBroadCastReceive = new USBReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addDataScheme("file");
        registerReceiver(usbBroadCastReceive, filter);

        //检查是否为工作时间 检测是否重启
        Intent intent = new Intent(MainActivity.this, WorkTimerService.class);
        this.bindService(intent, workTimeServiceConnection, Context.BIND_AUTO_CREATE);

        beaconIntent = new Intent(MainActivity.this, BluetoothService.class);
        //打开beaconservice；
        if (SPUtils.getInt(MainActivity.this, Config.BEACON_MODE_STATE) < 0) {
            startService(beaconIntent);
        }

    }

    private Timer alarmTimer;

    private void closeService() {
        Log.e(TAG, "closeService: ");
        //检测是否是工作时间，检测是否重启
        mainActivityPlayModel.onPause();
        unbindService(workTimeServiceConnection);

        //beaconservice；
        if (beaconIntent != null)
            stopService(beaconIntent);

        //u盘拷贝注销
        unregisterReceiver(usbBroadCastReceive);
        //关闭播放广告
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

    //IR usbTag 消息
    private void UsbTagHook(int number) {
        if (number == 0) {
            //无人
            Intent intent = new Intent(BLUETOOTH_BROADCAST_NAME);
            intent.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_NO_PERSION);
            sendBroadcast(intent);
        } else if (number == 1) {
            //有人
            Intent intent = new Intent(BLUETOOTH_BROADCAST_NAME);
            intent.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_PERSION);
            sendBroadcast(intent);
        } else {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            //移动检测
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

    private void FaceDetectPlayingStateMachine(int intentNo, String ID, String gender, Rect rect) {
        if (isPlayingBeaconEvent) {//如果正在播放beacon，检测到另外的beacon设备在5之内不播放新的beacon。
            if (intentNo == Config.BEACON_TAG_NO_PERSION) {//没人
                if (beaconTagNo == Config.BEACON_TAG_NO_PERSION && intentNo == Config.BEACON_TAG_NO_PERSION) {  //原来没人，现在再次没人
                    return;
                }
                //原来有人，现在没人
                String parentPath = Config.INTERNAL_FILE_ROOT_PATH + File.separator
                        + Config.PICKTURE_TEMP_FOLDER;

                if (mainActivityPlayModel != null) {
                    FaceManagerUtil.savePlayRecord(ID, mainActivityPlayModel.getCurrentNum(), mainActivityPlayModel.getCurrentPosition());
                }

                beaconTagNo = intentNo;
                mainActivityPlayModel.startPlayBeacon(false);

                Logger.WashingLoggerAppend(ID, gender, 0, 1, 0, 0);  //中途离开加1
            } else if (intentNo == Config.BEACON_TAG_PERSION && beaconTagNo == Config.BEACON_TAG_NO_PERSION) {//原来没人，现在又有人了
                if (rect != null) {
                    DrawInfo drawinfo = new DrawInfo(rect, 1, 2, 3, ID);
                    drawFace(drawinfo);
                }
                /*if (enableWashingSelect == false) {

                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon();
                } else {
                    PopWashingChooseDialog(intentNo);
                }*/
                if ((FaceManagerUtil.FaceRecordGetLastTime(ID) < SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime))
                        && ((FaceManagerUtil.getPlayTimeRecord(ID) != -1))) {
                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon(true, FaceManagerUtil.getPlayNumRecord(ID), FaceManagerUtil.getPlayTimeRecord(ID));
                } else {
                    PopTempCaptureActivity(intentNo, ID, gender);
                }
            } else {
                if (rect != null) {
                    DrawInfo drawinfo = new DrawInfo(rect, 1, 2, 3, ID);
                    drawFace(drawinfo);
                }
            }
        } else {
            if (intentNo == Config.BEACON_TAG_PERSION) {//有人
                /*if (enableWashingSelect == false) {
                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon();

                } else {
                    PopWashingChooseDialog(intentNo);
                }*/
                if ((FaceManagerUtil.FaceRecordGetLastTime(ID) < SPUtils.getLong(mContext, Config.CFGFaceResumeTime, Config.DefFaceResumeTime))
                        && ((FaceManagerUtil.getPlayTimeRecord(ID) != -1))) {
                    beaconTagNo = intentNo;
                    mainActivityPlayModel.startPlayBeacon(true, FaceManagerUtil.getPlayNumRecord(ID), FaceManagerUtil.getPlayTimeRecord(ID));
                } else {
                    PopTempCaptureActivity(intentNo, ID, gender);

                }
                if (rect != null) {
                    DrawInfo drawinfo = new DrawInfo(rect, 1, 2, 3, ID);
                    drawFace(drawinfo);
                }
            }
        }
    }

    private DrawHelper drawHelper;

    public void drawFace(DrawInfo info) {
        Log.e(TAG, "drawFace: " + info.getRect().toString());
        if (SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {//&&正中间的条件             tempWhiteView.setVisibility(View.GONE);
            float CurTemp = ((MyApplication) mContext.getApplicationContext()).getCurTemp();
            if (CurTemp >= SPUtils.getFloat(mContext, Config.CFGErrorTempCFG, Config.DefErrorTempValue)) {
                mailSenderUtil.sendErrorReport(mContext, "\nDateTime: " + TimeUtil.getCurrentFormatTime() + "\nFace ID: " + info.getName() + "\nTemp: " + CurTemp, null);
                Toast.makeText(mContext, "Exception Temperature Data detected, Please contact manager!\n", Toast.LENGTH_LONG).show();
            }

            // TODO: 2021/6/22 显示体温 一秒后消失  并设置体温，以便保存进数据库
        }
        if (drawHelper == null) {
            drawHelper = new DrawHelper(640, 480, faceRectView.getWidth(), faceRectView.getHeight(), 0, Camera.CameraInfo.CAMERA_FACING_FRONT, false);
        }
        removeFaceViewHandler.removeMessages(1);
        if ((faceRectView == null)) {
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
        removeFaceViewHandler.sendEmptyMessageDelayed(1, 500);
    }

    private Handler removeFaceViewHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (faceRectView != null) {
                faceRectView.clearFaceInfo();
            }
            return false;
        }
    });


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
                if (isPlayingBeaconEvent) {//如果正在播放beacon，检测到另外的beacon设备在5之内不播放新的beacon。
                    if (intentNo == Config.BEACON_TAG_NO_PERSION) {//没人
                        if (beaconTagNo == Config.BEACON_TAG_NO_PERSION && intentNo == Config.BEACON_TAG_NO_PERSION) {  //原来没人，现在再次没人
                            return;
                        }
                        //原来有人，现在没人
                        String parentPath = Config.INTERNAL_FILE_ROOT_PATH + File.separator
                                + Config.PICKTURE_TEMP_FOLDER;

                        beaconTagNo = intentNo;
                        mainActivityPlayModel.startPlayBeacon();
                    } else if (intentNo == Config.BEACON_TAG_PERSION && beaconTagNo == Config.BEACON_TAG_NO_PERSION) {//原来没人，现在又有人了
                        if (enableWashingSelect == false) {
                            beaconTagNo = intentNo;
                            mainActivityPlayModel.startPlayBeacon();
                        } else {
                            PopWashingChooseDialog(intentNo);
                        }
                    }
                } else {
                    if (intentNo == Config.BEACON_TAG_PERSION) {//有人
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
        float CurTemp = 0;
        if (SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {
            prepareBeaconTagNo = intentNo;
            tempWhiteView.setVisibility(View.VISIBLE);
        }
        //直接在此判断是播放长视频还是短视频
        if (FaceManagerUtil.FaceRecordNeedLongWashing(ID) == true) {
            WashingSelected = WASHING_SELECTED_T70;
        } else {
            WashingSelected = WASHING_SELECTED_T40;
        }
        beaconTagNo = intentNo;
        BeaconEventNo = (WashingSelected == WASHING_SELECTED_T70) ? -1 : 0;  //第一个文件是T70
        WashingSelected = WASHING_SELECTED_NONE;
        mainActivityPlayModel.startPlayBeacon(false);
        Logger.WashingLoggerAppend(ID, gender, 1, 0, 0, CurTemp);
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
                        BeaconEventNo = 0;  //第一个文件是T70
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
