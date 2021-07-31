package com.example.hu.mediaplayerapk.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.example.hu.mediaplayerapk.SerialUtil.TempUtil;
import com.example.hu.mediaplayerapk.SerialUtil.tempData;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.ui.activity.MainActivity;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.Logger;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.example.hu.mediaplayerapk.util.StorageUtil;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;
import com.example.hu.mediaplayerapk.util.washingLogUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static com.example.hu.mediaplayerapk.config.Config.USB_STORAGE_ROOT_PATH;
import static com.example.hu.mediaplayerapk.util.APKUtils.getSerialNumber;

/**
 * Created by Administrator on 2017/4/12.
 */

public class MyApplication extends Application {
    public static String external_impactv_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTV_FILE_NAME;
    public static String external_impacttv_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTTV_FILE_NAME;
    public static String external_event_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.EVENT_FILE_NAME;
    public static String external_beacon_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.BEACON_FILE_NAME;
    public static String external_washing_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.WASHING_FILE_NAME;
    public static String external_warning_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.WARNING_FILE_NAME;

    public static String internal_event_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.EVENT_FILE_NAME;
    public static String internal_beacon_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.BEACON_FILE_NAME;
    public static String internal_impactv_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTV_FILE_NAME;
    public static String internal_system_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.SYSTEM_FILE_NAME;
    public static String internal_washing_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.WASHING_FILE_NAME;
    public static String internal_warning_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.WARNING_FILE_NAME;
    public static String internal_ring_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.RING_FILE_NAME;

    public static String usb_system_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_SYSTEM_FILE_NAME;
    public static String usb_impactv_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTV_FILE_NAME;
    public static String usb_impacttv_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTTV_FILE_NAME;
    public static String usb_event_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_EVENT_FILE_NAME;
    public static String usb_beacon_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_BEACON_EVENT_FILE_NAME;
    public static String usb_washing_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WASHING_FILE_NAME;
    public static String usb_warning_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WARNING_FILE_NAME;
    public static String usb_ring_path = USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_RING_FILE_NAME;
    public static boolean existExternalSDCard;
    private boolean isOpen = false;
    protected static MyApplication instance;
    private static final String TAG = "MyApplication";
    public static float screenWidthRatio;
    public static float screenHeightRatio;
    public static int ScreenWidth;
    public static int ScreenHeight;
    private static Context mContext;
    private static float curTemp = 0;
    private static double curUsefulTemp = 0;  //经过temper界面获取确认人脸后记录下的温度

    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //Log.d(TAG, "handle message " + msg.what);

            if (msg.what == TempUtil.NewTempReport) {
                //if(textViewTemp!= null)
                {

                    tempData msgData = (tempData)msg.obj;
                    if(msgData != null) {
                        String msgPath = (String)msgData.portPath;
                        float tempVal = msgData.tempValue;
                        java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.0");

                        if(tempReader !=null)
                        {
                            if(tempReader.getTempPath().equalsIgnoreCase(msgPath))
                            {
                                //textViewTemp.setText(myformat.format(tempVal) + " " + msgPath+"\n");
                                Log.d(TAG, myformat.format(tempVal) + " " + msgPath+"\n");
                                curTemp = (float)(((int)(tempVal *10))/10f);
                            }
                        }
                    }
                }
            }
            else if (msg.what == TempUtil.RXReport) {
                //if(textViewTemp!= null)
                {
                    tempData msgData = (tempData)msg.obj;
                    if(msgData != null) {
                        String msgPath = (String)msgData.portPath;
                        String dispString = msgData.DispString;
                        java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.0");
                        if(tempReader !=null)
                        {
                            if(tempReader.getTempPath().equalsIgnoreCase(msgPath))
                            {
                                //textViewTemp.setText(dispString + " " + msgPath+"\n");
                                Log.d(TAG, dispString + " " + msgPath+"\n");
                            }
                        }
                    }
                }
            }

            return true;
        }

    });

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mContext = this.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
        alterPath();
        initScreenDisplayMetrics();
        initFilePath();

        FaceManagerUtil.FaceRecordUtilInit(this);
        existExternalSDCard = FileUtils.getSize(Config.EXTERNAL_FILE_ROOT_PATH, mContext) > 0;
        FileUtils.CheckAndCreatePlayLogfile(FileUtils.getLogPath()); //创建playlog file
		Logger.CheckAndDeleteOldLog();
		washingLogUtil.buildAllReport();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteMoreOneMonthImage(Config.INTERNAL_FILE_ROOT_PATH + File.separator
                        + Config.PICKTURE_OK_FOLDER);
                FileUtils.deleteMoreOneMonthImage(Config.INTERNAL_FILE_ROOT_PATH + File.separator
                        + Config.PICKTURE_NG_FOLDER);
            }
        }).start();

        tempReaderInitialize();
    }

    // 创建服务用于捕获崩溃异常
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, "uncaughtException: ", new Exception());
            saveCatchInfo2File(ex);
            restartApp();//发生崩溃异常时,重启应用
        }
    };

    private String saveCatchInfo2File(Throwable ex) {

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        Log.e(TAG, "saveCatchInfo2File: " + result);
        return null;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    private boolean killMyself()
    {
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE); //获取应用程序管理器
        String curForeApp = getApplicationContext().getPackageName();
        //manager.killBackgroundProcesses(getForegroundActivityName(getApplicationContext()));
        //manager.forceStopPackageAsUser(getForegroundActivityName(getApplicationContext()), 1);

        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(manager, curForeApp);  //packageName是需要强制停止的应用程序包名
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, "Kill " + curForeApp);
        return true;

    }

    private void reboot3(){
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void restartApp() {
        Log.e(TAG, "restartApp: ", new Exception());
        //killMyself();
        //reboot3();
        Intent intent = new Intent(instance, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instance.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前

    }

    public void rebootDevice(){
        PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
        pManager.reboot("PlayerReboot");
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void initFilePath() {
//        Log.e(TAG, " FileUtils.getStoragePath = " + FileUtils.getStoragePath(this));
//        Log.e(TAG, " FileUtils.GetAllSDPath = " + FileUtils.GetAllSDPath());
        alterRootPath();
        external_impactv_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTV_FILE_NAME);
        external_impacttv_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTTV_FILE_NAME);
        external_event_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.EVENT_FILE_NAME);
        external_beacon_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.BEACON_FILE_NAME);
        internal_beacon_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.BEACON_FILE_NAME);
        internal_event_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.EVENT_FILE_NAME);
        internal_impactv_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.IMPACTV_FILE_NAME);
        internal_system_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.SYSTEM_FILE_NAME);
        internal_warning_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.WARNING_FILE_NAME);
        internal_washing_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.WASHING_FILE_NAME);
        internal_ring_path = Config.INTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.INTERNAL_FILE_ROOT_PATH + File.separator + Config.RING_FILE_NAME);

        external_warning_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.WARNING_FILE_NAME);
        external_washing_path = Config.EXTERNAL_FILE_ROOT_PATH + File.separator +
                getFileName(Config.EXTERNAL_FILE_ROOT_PATH + File.separator + Config.WASHING_FILE_NAME);


        usb_impactv_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTV_FILE_NAME);
        usb_impacttv_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTTV_FILE_NAME);
        usb_event_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_EVENT_FILE_NAME);
        usb_system_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_SYSTEM_FILE_NAME);
        usb_beacon_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_BEACON_EVENT_FILE_NAME);
        usb_warning_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WARNING_FILE_NAME);
        usb_washing_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WASHING_FILE_NAME);
        usb_ring_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_RING_FILE_NAME);
        Log.e(TAG, "initFilePath: " +
                "\nexternal_impactv_path = " + external_impactv_path +
                "\nexternal_impacttv_path = " + external_impacttv_path +
                "\nexternal_event_path = " + external_event_path +
                "\nexternal_beacon_path = " + external_beacon_path +
                "\ninternal_beacon_path = " + internal_beacon_path +
                "\ninternal_event_path = " + internal_event_path +
                "\ninternal_warning_path = " + internal_warning_path +
                "\ninternal_washing_path = " + internal_washing_path +
                "\ninternal_impactv_path = " + internal_impactv_path +
                "\ninternal_system_path = " + internal_system_path +
                "\nusb_impactv_path = " + usb_impactv_path +
                "\nusb_impacttv_path = " + usb_impacttv_path +
                "\nusb_system_path = " + usb_system_path +
                "\nusb_event_path = " + usb_event_path +
                "\nusb_warning_path = " + usb_warning_path +
                "\nusb_washing_path = " + usb_washing_path +
                "\nusb_beacon_path = " + usb_beacon_path);
    }

    public void initUSBPath(String dataPath) {
        List<String> paths2 =  FileUtils.getMountPathList();
        Log.e(TAG, " initUSBPath FileUtils.getMountPathList = " + FileUtils.getMountPathList() + "dataPath = " + dataPath);
        boolean containPath = false;
        for (String path : paths2) {
            if (path.contains(dataPath)) {
                USB_STORAGE_ROOT_PATH = path;
                containPath = true;
            }
        }
        if (!containPath) {
            for (String path : paths2) {
                if (path.contains("usb")) {
                    USB_STORAGE_ROOT_PATH = path;
                }
            }
        }
        usb_impactv_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTV_FILE_NAME);
        usb_impacttv_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_IMPACTTV_FILE_NAME);
        usb_event_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_EVENT_FILE_NAME);
        usb_system_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_SYSTEM_FILE_NAME);
        usb_beacon_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_BEACON_EVENT_FILE_NAME);
        usb_warning_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WARNING_FILE_NAME);
        usb_washing_path = USB_STORAGE_ROOT_PATH + File.separator +
                getFileName(USB_STORAGE_ROOT_PATH + File.separator + Config.USB_STORAGE_WASHING_FILE_NAME);
        Log.e(TAG, "initUSBPath: \n" + USB_STORAGE_ROOT_PATH);
    }

    private String getFileName(String path) {
        String string = SPUtils.getString(this, path);
        string = string.equals("null") ? path.split(File.separator)[path.split(File.separator).length - 1] : string;
        return string;
    }

    private void alterRootPath() {
        Config.INTERNAL_FILE_ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
        List<String> paths1 = FileUtils.getStoragePath(this);
        for (String path : paths1) {
            if (path.contains("ext")) {
                Config.EXTERNAL_FILE_ROOT_PATH = path;
            } else if (path.contains("usb")) {
                USB_STORAGE_ROOT_PATH = path;
            }
        }
        List<String> paths2 = StorageUtil.getAllExternalSdcardPath();
        for (String path : paths2) {
            if (path.contains("ext")) {
                Config.EXTERNAL_FILE_ROOT_PATH = path;
            } else if (path.contains("usb")) {
                USB_STORAGE_ROOT_PATH = path;
            }
        }
        List<String> paths3 = FileUtils.getMountPathList();
        for (String path : paths3) {
            if (path.contains("ext")) {
                Config.EXTERNAL_FILE_ROOT_PATH = path;
            } else if (path.contains("usb")) {
                USB_STORAGE_ROOT_PATH = path;
            }
        }
    }

    private void alterPath() {
        String serialNumber = getSerialNumber();
        String serialNumberTemp = "AAAA";
        String serialNumberDefault = "14";
        if (serialNumber != null && serialNumber.length() > 5) {
            serialNumberTemp = serialNumber.substring(0, 4);
            serialNumberDefault = serialNumber.substring(3, 5);
        } else {
            return;
        }
        try {
            switch (serialNumberTemp) {
                case "PZ07":
                case "PW07":
                case "PA07":
                case "PB07":
                    //case "IS70":
                    //case "ISS0":
                    //case "IST0":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv7";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv7";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event7";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system7";
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacon7";
                    break;
                case "PZ10":
                case "PW10":
                case "PB10":
                case "PA10":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv10";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv10";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event10";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system10";
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacon10";
                    break;
                case "PW19":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv19";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv19";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event19";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system19";
                    break;
                case "PB19":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv19";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv19";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event19";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system19";
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacon19";
                    break;
                case "PW14":
                case "RS14":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv14";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv14";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event14";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system14";
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacon14";
                    break;
                case "IS70":
                case "ISS0":
                case "IST0":
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttvs7";
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impacs7";
                    Config.USB_STORAGE_EVENT_FILE_NAME = "events7";
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "systems7";
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacons7";
                    break;
                default:
                    if (serialNumberDefault.substring(0, 1).equals("0"))
                        serialNumberDefault = serialNumberDefault.substring(1, 2);
                    Config.USB_STORAGE_IMPACTTV_FILE_NAME = "impacttv" + serialNumberDefault;
                    Config.USB_STORAGE_IMPACTV_FILE_NAME = "impactv" + serialNumberDefault;
                    Config.USB_STORAGE_EVENT_FILE_NAME = "event" + serialNumberDefault;
                    Config.USB_STORAGE_SYSTEM_FILE_NAME = "system" + serialNumberDefault;
                    Config.USB_STORAGE_BEACON_EVENT_FILE_NAME = "beacon" + serialNumberDefault;
                    Config.USB_STORAGE_WARNING_FILE_NAME = "warning" + serialNumberDefault;
                    Config.USB_STORAGE_WASHING_FILE_NAME = "washing" + serialNumberDefault;
                    Config.USB_STORAGE_RING_FILE_NAME = "ring" + serialNumberDefault;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initScreenDisplayMetrics() {
        //获得屏幕的真实宽高getRealMetrics 获得屏幕的显示宽高getMetrics
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getRealMetrics(metric);
        ScreenWidth = metric.widthPixels; // 屏幕宽度（像素）
        ScreenHeight = metric.heightPixels; // 屏幕宽度（像素）
        Log.e(TAG, "initScreenDisplayMetrics: ScreenWidth = " + ScreenWidth + ",ScreenHeight = " + ScreenHeight);
        screenHeightRatio = ScreenHeight / 1080.0f;
        screenWidthRatio = ScreenWidth / 1920.0f;

        Log.e(TAG, "initScreenDisplayMetrics: ScreenHeight = " + ScreenHeight + ",ScreenWidth = " + ScreenWidth);

        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        // 通过WindowManager对象的getDefaultDisplay()方法获取Display对象
        // 通过Activity类中的getWindowManager()方法获取窗口管理，再调用getDefaultDisplay()方法获取获取Display对象
        Display display = windowManager.getDefaultDisplay();

        // 方法一(推荐使用)使用Point来保存屏幕宽、高两个数据
        Point outSize = new Point();
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display.getSize(outSize);
        // 从Point对象中获取宽、高
        int x = outSize.x;
        int y = outSize.y;
        // 通过吐司显示屏幕宽、高数据
        Log.e(TAG, "initScreenDisplayMetrics: " + "1手机像素为：" + x + "x" + y);

        // 方法二(不推荐使用)直接通过Display对象获取屏幕宽、高数据
        int width = display.getWidth();
        int height = display.getHeight();
        // 通过吐司显示屏幕宽、高数据
        Log.e(TAG, "initScreenDisplayMetrics: " + "2手机像素为：" + width + "x" + height);


        // 通过WindowManager对象的getDefaultDisplay()方法获取Display对象
        Display display2 = windowManager.getDefaultDisplay();
        // 使用Point来保存屏幕宽、高两个数据
        Point outSize2 = new Point();
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display2.getSize(outSize2);

        Log.e(TAG, "initScreenDisplayMetrics: " + "3手机像素为：" + outSize.x + "x" + outSize.y);
    }

    private List<Activity> activityList = new LinkedList<Activity>();

    public void addActivity(Activity activity) {
        Log.e(TAG, "addActivity: " + activity.getLocalClassName());
        activityList.add(activity);
    }

    public void exit() {
        for (Activity activity : activityList) {
            Log.e(TAG, "exit: " + activity.getLocalClassName());
            activity.finish();
        }
    }

    //temp reader
    private static temp.cv.com.tempreader.broadcast.USBDeviceReceive usbDeviceReceiver = null;
    private static TempUtil tempReader = null;

    public static float getCurTemp()
    {
        if(tempReader != null)
        {
            return curTemp;
        }
        return 0;
    }

    public static double getCurUseFulTemp()
    {
        Log.d(TAG, curUsefulTemp+"");
        return curUsefulTemp;
    }

    public static void setCurUsefulTemp(float newValue)
    {
        curUsefulTemp = (double)((int)(newValue*10))/10d;
    }

    private void tempReaderInitialize()
    {

        String path = "/dev/ttyACM";
        int j = -1;
        for(int i = 0; i < 10; i++)
        {
            if(new File(path+i).exists() == true)
            {
                j++;
                Log.d(TAG, path+i+" exits");
                if(j == 0)
                {
                    tempReader = new TempUtil();
                    if(tempReader != null)
                    {
                        if(tempReader.TempInit(this, mHandler, path+i) == false)
                        { //textViewTemp.setText("Cannot open Temp sensor");
                            tempReader = null;
                        }
                        else
                        {
                            return;
                        }

                    }
                    else
                    {
                       // textViewTemp.setText("Cannot open Temp sensor");
                    }
                }


            }
        }

        registerUSBDeviceReceiver(mContext);

    }

    private static boolean checkPluggIn(String path)
    {
        boolean result = false;

        if(tempReader == null)
        {
            tempReader = new TempUtil();
            if(tempReader != null)
            {
                if(tempReader.TempInit(mContext, mHandler, path) == false)
                {
                    Log.d(TAG, "Cannot open Temp sensor");
                    tempReader = null;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                Log.d(TAG, "Cannot open Temp sensor");
            }
        }

        return result;
    }

    //检查已经初始化的tempReader有没有被拔出来的
    private static boolean checkPlugOut()
    {
        boolean result = false;
        if(tempReader != null)
        {
            if(tempReader.isSerialPortAvalible() == false)
            {
                tempReader.TempDeinit(mContext);
                tempReader = null;
                result = true;
                return result;
            }
        }

        return result;
    }
    private static String hasNewTempReader()
    {

        String path = "/dev/ttyACM";

        for(int i = 0; i < 10; i++)
        {
            if(new File(path+i).exists() == true)
            {
                if(tempReader != null)
                {
                    if(tempReader.getTempPath().equalsIgnoreCase(path+i) == true)
                    {
                        continue;
                    }
                }

                return path+i;
            }
        }
        return null;
    }
    private static void registerUSBDeviceReceiver(Context context)
    {
        usbDeviceReceiver = new temp.cv.com.tempreader.broadcast.USBDeviceReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);

        context.registerReceiver(usbDeviceReceiver, filter);
        usbDeviceReceiver.setUsbDeviceListener(new temp.cv.com.tempreader.broadcast.USBDeviceReceive.USBReceiverInterface() {
            @Override
            public void USBAttateched(UsbDevice newDevice) {
                boolean isArduinoDevice = false;
                String newPath = null;
                Log.d(TAG, "USBAttateched");
                if((newDevice.getVendorId() == 9025)&&(newDevice.getProductId() == 32822))
                {
                    newPath = hasNewTempReader();
                    if((newPath != null))
                    {
                        isArduinoDevice = true;
                    }
                }

                if(isArduinoDevice == false)
                {
                    return;
                }
                Log.e(TAG, "Temp dongle plug in "+newPath);
                checkPluggIn(newPath);
            }

            @Override
            public void USBDetached(UsbDevice removedDevice) {
                Log.d(TAG, "USBDetached");
                if((removedDevice.getVendorId() == 9025)&&(removedDevice.getProductId() == 32822))
                {
                    checkPlugOut();
                }
                System.gc();

            }
        });
    }

    private static void unregisterUSBDeviceReceiver(Context context)
    {
        if(usbDeviceReceiver != null) {
            context.unregisterReceiver(usbDeviceReceiver);
            usbDeviceReceiver = null;
        }
    }
}
