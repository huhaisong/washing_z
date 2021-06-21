package com.example.hu.mediaplayerapk.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.hu.mediaplayerapk.broadcast.AlarmBroadcastReceiver;

import java.util.Calendar;

import static java.security.AccessController.getContext;

/**
 * アプリをリスタートさせるアラーム
 * 30秒先に再起動するタイマー
 * 10秒ごとにアラームを再生成
 */
public class RestartAlarmWatcher {

    private static int restartAlarmIdentifier0 = 56732;
    private static int restartAlarmIdentifier1 = 56733;
    private static int restartAlarmIdentifier2 = 56734;
    private static int restartAlarmIdentifier3 = 56735;
    private static Context mContext;
    private final static String TAG = "RestartAlarmWatcher";

    public static void updateRestartAlarm(Context context) {
        mContext = context;
        Log.d(TAG, "updateRestartAlarm");
        cancelAlarm(restartAlarmIdentifier0);
        cancelAlarm(restartAlarmIdentifier1);
        pushAlarm(restartAlarmIdentifier0, 60);
        pushAlarm(restartAlarmIdentifier1, 120);
       /* pushAlarm(restartAlarmIdentifier2, 60);
        pushAlarm(restartAlarmIdentifier3, 60);*/
    }

    private static void pushAlarm(int identifier, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, seconds);

        Intent intent = new Intent(mContext, AlarmBroadcastReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(
                mContext, identifier, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pending);
        }
        else
        {
            Log.e(TAG, "getSystemService(Context.ALARM_SERVICE) = null");
        }
    }

    public static void cancelAlarms() {
        cancelAlarm(restartAlarmIdentifier0);
        Log.d(TAG, "cancelAlarms");
        cancelAlarm(restartAlarmIdentifier1);
        /*cancelAlarm(restartAlarmIdentifier2);
        cancelAlarm(restartAlarmIdentifier3);*/
    }

    private static void cancelAlarm(int identifier) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, identifier, intent, PendingIntent.FLAG_ONE_SHOT);


        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }
}
