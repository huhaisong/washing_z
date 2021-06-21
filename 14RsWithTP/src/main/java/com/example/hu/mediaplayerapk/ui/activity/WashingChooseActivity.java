package com.example.hu.mediaplayerapk.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dialog.WashingChooseDialog;
import com.example.hu.mediaplayerapk.service.BluetoothService;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.SPUtils;
import com.rockchip.Gpio;

import java.io.File;
import java.io.IOException;

import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.BLUETOOTH_BROADCAST_NAME;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.BLUETOOTH_INT_EXTRA_NAME;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T40;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECTED_T70;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WASHING_SELECT_BROADCAST_NAME;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.WashingSelected;
import static com.example.hu.mediaplayerapk.util.FileUtils.checkHaveFile;
import static com.example.hu.mediaplayerapk.util.GoToHome.goToHome;

public class WashingChooseActivity extends BaseActivity  {

    private static final String TAG = "WashingChooseActivity";
    private static int WASHINGDIALOG_QUIT_TIME = 15*1000;  //15秒后退出
    public static final String ToneFullPath = "/mnt/sdcard/Android/data/choosing.wav";
    private static ImageView imageT70;
    private static ImageView imageT40;
    private boolean T70_Focused = true;

    private MediaPlayer mediaPlayer;
    private Intent beaconIntent;

    private void mediaplayerClose() {
        Log.d(TAG, "mediaplayerClose");
        //stop();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying())
                    stop();
            }
            catch (IllegalStateException e)
            {
                e.printStackTrace();
            }

            mediaPlayer.setDisplay(null);
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    private void finishChoosing()
    {
        mHandler.removeCallbacksAndMessages(null);
        mediaplayerClose();
        finish();
    }
    private void play(String url, boolean isFD, boolean isLoop) {
        Log.e("aaa", "---------------playBGM");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                stop();
            mediaPlayer.reset();
            try {
                if (isFD) {
                    AssetFileDescriptor fd = this.getAssets().openFd(url);
                    mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                } else {
                    mediaPlayer.setDataSource(url);
                }
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("aaaa", "----------setURLFailed");
            }
        } else {
            initMediaPlayer();
            mediaPlayer.reset();
            try {
                if (isFD) {
                    AssetFileDescriptor fd = this.getAssets().openFd(url);
                    mediaPlayer.setDataSource(fd.getFileDescriptor());
                } else {
                    mediaPlayer.setDataSource(url);
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (isLoop) {
            mediaPlayer.setLooping(true);
        }
    }
    private void stop() {
        if (mediaPlayer == null) return;
        mediaPlayer.stop();
    }
    private void initMediaPlayer() {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                stop();
            mediaPlayer.reset();
        } else
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "QUIT Dialog");
            WashingSelected = MainActivity.WASHING_SELECTED_NONE;
            //finish();
            finishChoosing();
            return false;
        }
    });

    private void openService()
    {
        beaconIntent = new Intent(WashingChooseActivity.this, BluetoothService.class);
        //打开beaconservice；
        if (SPUtils.getInt(WashingChooseActivity.this, Config.BEACON_MODE_STATE) < 0) {
            startService(beaconIntent);
        }

        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BLUETOOTH_BROADCAST_NAME);
        bluetoothFilter.addAction(WASHING_SELECT_BROADCAST_NAME);
        registerReceiver(bluetoothActBroadcastReceiver, bluetoothFilter);
    }

    private void closeService() {
        Log.e(TAG, "closeService: ");
        //beaconservice；
        if (beaconIntent != null)
            stopService(beaconIntent);

        if (bluetoothActBroadcastReceiver != null) {
            unregisterReceiver(bluetoothActBroadcastReceiver);
        }
        bluetoothActBroadcastReceiver = null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_washing_choose);

        imageT70 = (ImageView)findViewById(R.id.iv_washing_choose_yes);
        imageT40 = (ImageView)findViewById(R.id.iv_washing_choose_no);

        imageT40.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b == true) {

                    if(T70_Focused == true)
                    {
                        T70_Focused = false;
                        Log.d(TAG, "T40 Focused");
                        WashingSelected = WASHING_SELECTED_T40;
                        //finish();
                        finishChoosing();
                    }
                }
                else
                {

                }
            }
        });

        imageT40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "T40 clicked");
                /*Intent intent2 = new Intent(WASHING_SELECT_BROADCAST_NAME);
                intent2.putExtra(BLUETOOTH_INT_EXTRA_NAME, 1);
                sendBroadcast(intent2);*/
                WashingSelected = WASHING_SELECTED_T40;
                //finish();
                finishChoosing();
            }
        });

        imageT70.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "T70 clicked");
                /*Intent intent2 = new Intent(WASHING_SELECT_BROADCAST_NAME);
                intent2.putExtra(BLUETOOTH_INT_EXTRA_NAME, 0);
                sendBroadcast(intent2);*/
                WashingSelected = WASHING_SELECTED_T70;
                //finish();
                finishChoosing();
            }
        });

        imageT70.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if((T70_Focused == false)&&(b == true))
                {
                    T70_Focused = true;
                    Log.d(TAG, "T70 Focused");
                    WashingSelected = WASHING_SELECTED_T70;
                    //finish();
                    finishChoosing();
                }

            }
        });
        mHandler.sendEmptyMessageDelayed(0, WASHINGDIALOG_QUIT_TIME);
        play(ToneFullPath,false,false);

        //openService();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            if((keyCode == KeyEvent.KEYCODE_0)&&(event.getRepeatCount() == 0))
            {
                //IR usbtag无人消息
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(0, 1); //立刻退出
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onPause()
    {
        super.onPause();
        //closeService();
    }
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //mediaplayerClose();

    }

    private BluetoothActBroadcastReceiver bluetoothActBroadcastReceiver = new BluetoothActBroadcastReceiver();

    private class BluetoothActBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int intentNo = intent.getIntExtra(BLUETOOTH_INT_EXTRA_NAME, -1);
            Log.e(TAG, "onReceive --------------1 onReceive: intentNo = " + intentNo );
           /* if (System.currentTimeMillis() - oldTime <= 2 * 1000) {
                Log.e(TAG, "onReceive: time is too close");
                return;
            }*/
            if(intent.getAction().equalsIgnoreCase(BLUETOOTH_BROADCAST_NAME)) {
                if (intentNo == Config.BEACON_TAG_NO_PERSION) {//没人
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(0, 1); //立刻退出
                }
            }
        }
    }
}
