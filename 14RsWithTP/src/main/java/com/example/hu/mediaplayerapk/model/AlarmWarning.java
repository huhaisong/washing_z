package com.example.hu.mediaplayerapk.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.ui.activity.MainActivity;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.io.IOException;

import androidx.annotation.RequiresApi;

import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.beaconTagNo;
import static com.example.hu.mediaplayerapk.ui.activity.MainActivity.isPlayingBeaconEvent;

public class AlarmWarning {
    private static final String TAG = "AlarmWarning";
    private static final int REMOVE_RED_STROKE = 2222;
    private static final int RED_STROKE_FLASHING = 1111;
    private static final int RED_STROKE_FLASHING_INTERVAL = 1000;  //一秒亮一秒灭
    private View redStrokeView;
    private Context mContext;
    private boolean isAlarming;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public boolean handleMessage(Message msg) {
            //Log.e(TAG, "handleMessage" + msg.what);

            switch (msg.what) {

                case REMOVE_RED_STROKE:
                    stopAlarmWarning();
                    break;

                case RED_STROKE_FLASHING:
                    if(isAlarming == true)
                    {
                        if (redStrokeView != null) {
                            redStrokeView.setVisibility((redStrokeView.getVisibility() == View.GONE)?View.VISIBLE:View.GONE);
                        }
                        mHandler.sendEmptyMessageDelayed(RED_STROKE_FLASHING,RED_STROKE_FLASHING_INTERVAL);
                    }
                    break;

            }
            return false;
        }
    });


    public AlarmWarning (Context thisContext, View red)
    {
        this.mContext = thisContext;
        redStrokeView = red;
        isAlarming = false;
    }

    public void startAlarmWarning()
    {
        isAlarming = true;
        if (redStrokeView != null) {
            Log.d(TAG, "redStrokeView.setVisibility(View.VISIBLE);");
            redStrokeView.setVisibility(View.VISIBLE);
        }

        play(ToneFullPath,false,false);
        mHandler.sendEmptyMessageDelayed(REMOVE_RED_STROKE,SPUtils.getInt(mContext, Config.ALARM_NOTICE_VALID_TIME, 1) * 60 * 1000);
        mHandler.sendEmptyMessageDelayed(RED_STROKE_FLASHING,RED_STROKE_FLASHING_INTERVAL);
    }

    public void stopAlarmWarning()
    {
        mediaplayerClose();
        isAlarming = false;
        mHandler.removeMessages(REMOVE_RED_STROKE);
        mHandler.removeMessages(RED_STROKE_FLASHING);
        if (redStrokeView != null ) {
            redStrokeView.setVisibility(View.GONE);
        }
    }

    /******************************************************************************************
     * *
     */
    private MediaPlayer mediaPlayer;
    public static final String ToneFullPath = "/mnt/sdcard/MUSIC/alarm.wav";

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

    private void play(String url, boolean isFD, boolean isLoop) {
        Log.e(TAG, "---------------playBGM");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                stop();
            mediaPlayer.reset();
            try {
                if (isFD) {
                    AssetFileDescriptor fd = mContext.getAssets().openFd(url);
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
                    AssetFileDescriptor fd = mContext.getAssets().openFd(url);
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
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
    }
}
