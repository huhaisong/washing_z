package com.example.hu.mediaplayerapk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.hu.mediaplayerapk.application.MyApplication;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "AlarmBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Alarm Received");
        //App.restart();
        ((MyApplication) context.getApplicationContext()).restartApp();
    }
}
