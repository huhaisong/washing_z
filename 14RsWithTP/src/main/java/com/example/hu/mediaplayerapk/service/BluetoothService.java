package com.example.hu.mediaplayerapk.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.hu.mediaplayerapk.application.MyApplication;
import com.example.hu.mediaplayerapk.bean.BeaconTag;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.ScheduleParse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.BLUETOOTH_BROADCAST_NAME;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.BLUETOOTH_INT_EXTRA_NAME;

/**
 * Created by huhaisong on 2017/8/31 9:59.
 * 蓝牙温度计设备service
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private static Context mContext = null;
    private BeaconTag beacon = null;  //记录本beacon文件夹内所有有关的beacon设备及其状态
    private static final int RestartScanInterval = 15*60*1000;   //测试发现7.1系统打开SCAN后30分钟会自动关闭SCAN

    public boolean initialize() {
        initializeBeaconDevice();
        if (getAdapter() == null)
            return false;
        return false;
    }

    private void initializeBeaconDevice() {
        beacon = null;

            if (FileUtils.getSize(Config.EXTERNAL_FILE_ROOT_PATH, mContext) > 0)
            beacon = ScheduleParse.parse_BEACON_NO_TXT(MyApplication.external_beacon_path + File.separator + Config.BEACON_DEVICE_FILE_NAME);
        else
            beacon = ScheduleParse.parse_BEACON_NO_TXT(MyApplication.internal_beacon_path + File.separator + Config.BEACON_DEVICE_FILE_NAME);

        if (beacon == null)
            Log.e(TAG, "initializeBeaconDevice: beacon == null");
        else
            Log.e(TAG, "initializeBeaconDevice: beacon = " + beacon.toString());
    }

    //如果设备支持BLE，那么就可以获取蓝牙适配器。
    private BluetoothAdapter getAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        super.onCreate();
        this.mContext = this;
        initialize();
        enableBluetooth();
        if (beacon != null)
            beacon.setBeaconData(-1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(SCAN_BLE);
        registerReceiver(myBroadcastReceiver, filter);
    }

    public static final String SCAN_BLE = "com.hu.scan.ble";

    //打开蓝牙
    public void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.enable();
                }
            }).start();
        }
    }

    //扫描蓝牙
    public void startScan() {
        Log.e(TAG, "startScan: ");
        stopScan();
        //closeBluetooth();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startLeScan(leScanHook);
        retartScanhandler.sendEmptyMessageDelayed(0, RestartScanInterval);
    }

    //停止扫描........
    public void stopScan() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.stopLeScan(leScanHook);
        retartScanhandler.removeCallbacksAndMessages(null);
    }

    //关闭设备
    public void closeBluetooth() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //定时关闭打开蓝牙SCAN
    private Handler retartScanhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            stopScan();
            startScan();
            //retartScanhandler.sendEmptyMessage(RestartScanInterval);
            retartScanhandler.sendEmptyMessageDelayed(0, RestartScanInterval);
            return false;
        }
    });
    public BluetoothAdapter.LeScanCallback leScanHook = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //Log.e(TAG, "onLeScan: device = " + device.getAddress());
            if (beacon == null)
                return;
            if (device.getAddress().equalsIgnoreCase(beacon.getBeaconAddr())) {
                //找到该设备了;
                //Log.d(TAG, "     curDev:" + beacon.toString());
                if ((scanRecord[11] == 0) && (beacon.getBeaconData() == 4)) {//4---->0
                    Intent intent2 = new Intent(BLUETOOTH_BROADCAST_NAME);
                    intent2.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_NO_PERSION);
                    sendBroadcast(intent2);
                    Log.i(TAG, " 4---->0 ");
                    //stopScan();//继续scan
                } else if ((scanRecord[11] == 4) && (beacon.getBeaconData() == 0)) {//0---->4 有人
                    Intent intent2 = new Intent(BLUETOOTH_BROADCAST_NAME);
                    intent2.putExtra(BLUETOOTH_INT_EXTRA_NAME, Config.BEACON_TAG_PERSION);
                    Log.i(TAG, " 0---->4 ");
                    sendBroadcast(intent2);
                }
                beacon.setBeaconData(scanRecord[11]);
            }
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
        stopScan();
        closeBluetooth();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SCAN_BLE)) {
                startScan();
                return;
            }
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                startScan();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        startScan();
        return super.onStartCommand(intent, flags, startId);
    }
}
