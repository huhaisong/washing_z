package com.example.hu.mediaplayerapk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import com.example.hu.mediaplayerapk.broadcast.HumanReceive;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.model.DrawInfo;
import com.example.hu.mediaplayerapk.util.face.FaceManagerUtil;


public class PistaEyesReceiver extends BroadcastReceiver {
    private final static String TAG ="PistaEyesReceiver";

    public final static String ACTION = "BeeSight_PeopleData";
    public final static String ACTION_NO_PERSON = "BeeSight_NoPeople";
    public final static String ACTION_PERSON_IN = "BeeSight_People_In";
    public final static String EXTRA  = "BeeSightString";
    private PistaEyesReceiver.FaceDetectInterface cbk = null;
    private String lastGender = "1.0";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        /* Broadcasts from PistaEyes can be received using the action name "BeeSight_PeopleData". */
        if (action.equals(ACTION)) {
            Bundle bundle = intent.getExtras();
            // The string can be retrieved from the received data using the key name "BeeSightString".
            String info = bundle.getString(EXTRA);
            Log.d(TAG, info);
            String [] temp = temp = info.split("\t");
            if(temp.length >= 24)
            {
                Log.d(TAG, "ID " + temp[5]);
                //[7] gender, -1:man; 1: lady
                Log.d(TAG, "Face position "+temp[19]+ " "+temp[20]+" "+ temp[21]+" "+temp[22]);
                Rect rect = new Rect(Integer.valueOf(temp[19]), Integer.valueOf(temp[20]),Integer.valueOf(temp[21]), Integer.valueOf(temp[22]));
                //DrawInfo drawinfo = new DrawInfo(rect, 1,2,3,temp[5]);

                //MainActivity.drawFace(drawinfo);
                if(FaceManagerUtil.FaceRecordDetecting(temp[5] ,temp[7]) == true)
                {
                    if(cbk != null) {
                        cbk.FaceDetectCallback(Config.BEACON_TAG_PERSION,temp[5],temp[7], rect);
                    }
                }
                else
                {
                    if(cbk != null) {
                        cbk.FaceDetectCallback(Config.BEACON_TAG_PERSION_REFRESH, temp[5],temp[7], rect);
                    }
                }
                lastGender = temp[7];
            }
            else
            {
                Log.d(TAG, temp.length+" "+ temp[0]);
            }
        }
        else if(action.equals(ACTION_NO_PERSON))
        {
            if(cbk != null) {
                String ID = intent.getStringExtra("ID");
                cbk.FaceDetectCallback(Config.BEACON_TAG_NO_PERSION,ID, lastGender,null);
            }
        }
    }

    public interface FaceDetectInterface {
        public void FaceDetectCallback(int intent,String ID, String gender, Rect rect);
    }

    public void setFaceDetectListener(PistaEyesReceiver.FaceDetectInterface brInteraction) {
        this.cbk = brInteraction;
    }
}
