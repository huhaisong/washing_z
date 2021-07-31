package com.example.hu.mediaplayerapk.util.face;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.example.hu.mediaplayerapk.bean.FaceIDBean;
import com.example.hu.mediaplayerapk.bean.WorkTimer;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.receiver.PistaEyesReceiver;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.JsonUtils;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FaceManagerUtil {
    private static final String TAG = "FaceManagerUtil";
    private static Context mContext = null;
    private static final int NO_HUMAN_TIMEOUT = 0x1100;
    private static final int HUMAN_LIST_SAVE_MSG = 0x1101;

    private static final int HUMAN_LIST_SAVE_INTERVAL = 10*1000;   //人离开后10秒保存文件
    //private static final int NO_FACE_TIMEOUT_INTERVAL = 3*1000;  //连续2秒检测不到人脸视为走开了
    private static boolean humanDetected = false;
    private static List<FaceIDBean> FaceIDList;

    private static Handler timeoutHandller = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what)
            {
                case NO_HUMAN_TIMEOUT:
                {
                    if(mContext != null) {
                        //广播一个无人的消息
                        Intent intent = new Intent(PistaEyesReceiver.ACTION_NO_PERSON);
                        intent.putExtra("ID", getCurActiveID());
                        mContext.sendBroadcast(intent);

                        allFaceReadyForNewEvent();
                        humanDetected = false;
                    }
                    timeoutHandller.removeMessages(NO_HUMAN_TIMEOUT);
                    timeoutHandller.sendEmptyMessageDelayed(HUMAN_LIST_SAVE_MSG, HUMAN_LIST_SAVE_INTERVAL);
                }
                    break;

                case HUMAN_LIST_SAVE_MSG:
                    timeoutHandller.removeMessages(HUMAN_LIST_SAVE_MSG);
                    saveFaceIDList();
                    break;

            }
            return false;
        }
    });

    public static void FaceRecordUtilInit(Context context)
    {
        Log.d(TAG, "FaceRecordUtilInit");
        mContext = context;

        humanDetected = false;

        FaceIDList = getFaceIDList();//new ArrayList<>();
    }

    //刷新人脸存在，更新人脸出现的时间
    public static boolean FaceRecordDetecting(String ID, String gender)
    {
        boolean ret = false;

        ret = refreshFaceID(ID, gender);
        if(ret == true)  //满足条件启动新的face event后才算是有效
        {
            humanDetected = true;
        }

        if(humanDetected == true)
        {
            timeoutHandller.removeMessages(NO_HUMAN_TIMEOUT);
            //timeoutHandller.sendEmptyMessageDelayed(NO_HUMAN_TIMEOUT, NO_FACE_TIMEOUT_INTERVAL);
            timeoutHandller.sendEmptyMessageDelayed(NO_HUMAN_TIMEOUT, SPUtils.getLong(mContext, Config.CFGFaceDisappearEventTime, Config.DefFaceDisappearEventTime));
        }
        return ret;
    }

    public static long FaceRecordGetLastTime(String ID)
    {
        long lastTimeInterval = 1000*60*60*1000;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                lastTimeInterval = curFace.getLastTimeInterval();
                break;
            }
        }

        return lastTimeInterval;
    }

    public static boolean FaceRecordLastTimeIntervalError(String ID)
    {
        long lastWashingInterval = FaceManagerUtil.FaceRecordGetLastTime(ID);
        if(lastWashingInterval != 0)
        {
            lastWashingInterval = lastWashingInterval/1000/60;  //分钟
        }
        if ((lastWashingInterval > SPUtils.getLong(mContext, Config.CFGFaceLongNoWashMinTime, Config.DefFaceLongNoWashMinTime))&&
                (lastWashingInterval < SPUtils.getLong(mContext, Config.CFGFaceLongNoWashMaxTime, Config.DefFaceLongNoWashMaxTime)))
        {
            return true;
        }

        return false;
    }

    //判断该ID是否刚刚离开
    public static boolean FaceRecordIsJustLeave(String ID)
    {
        return false;
    }

    //判断该ID是否需要播放长视频
    //判断该ID上一次出现的时间是否超过设定的值，如果超过就是博放长视频，如果没超就是播放短视频
    public static boolean FaceRecordNeedLongWashing(String ID)
    {
        long curTime = System.currentTimeMillis();
        long lastTimeInterval = -1;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                lastTimeInterval = curFace.getLastTimeInterval();
                break;
            }
        }

        if((lastTimeInterval == -1)||(lastTimeInterval > SPUtils.getLong(mContext, Config.CFGFaceShortVIDEOTime, Config.DefFaceShortVideoTime)))
        {
            Log.d(TAG, ID+ " "+lastTimeInterval+" play T70");
            return true;
        }
        Log.d(TAG, ID+ " "+lastTimeInterval+" play T40");
        return false;
    }


    //*****************************************************************************
    //返回true表示当前有效的新event开始了
    public static boolean refreshFaceID(String ID, String gender)
    {
        boolean ret = false;
        boolean existFaceID = false;
        long curTime = System.currentTimeMillis();

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                existFaceID = true;
                curFace.setRefreshTime(curTime);
                if((curFace.isActiveNow() == false)&&(curFace.getCurEventKeepTime() >= SPUtils.getLong(mContext, Config.CFGFaceNewEventTime, Config.DefFaceNewEventTime)))
                {
                    curFace.setActive(true);
                    ret = true;
                }
                break;
            }
        }

        if(existFaceID == false)
        {
            FaceIDBean newFace = new FaceIDBean(ID, curTime ,(gender.equalsIgnoreCase("1.0"))?true:false);
            FaceIDList.add(newFace);
        }

        return ret;
    }

    //*****************************************************************************
    //push ID Play record
    public static void savePlayRecord(String ID, int playNum, int playTime)
    {
        if(ID == null)
            return;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                Log.d(TAG, "savePlayRecord " + ID + " "+playNum+ " "+playTime);
                curFace.setLastPlayNum(playNum);
                curFace.setLastPlayTime(playTime);
                break;
            }
        }

        return;
    }

    public static int getPlayNumRecord(String ID)
    {
        int playNum = -1;
        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                playNum = curFace.getLastPlayNum();
                break;
            }
        }
        return playNum;
    }

    public static int getPlayTimeRecord(String ID)
    {
        int playTime = -1;
        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                playTime = curFace.getLastPlayTime();
                break;
            }
        }
        return playTime;
    }

    public static boolean IsLadyID(String ID)
    {
        boolean ret = false;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                ret = curFace.getGenderIsLady();
                break;
            }
        }
        return ret;
    }

    public static String getGenderStr(String ID)
    {
        boolean ret = false;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                ret = curFace.getGenderIsLady();
                break;
            }
        }

        return (ret == true)?"1.0":"-1.0";
    }

    public static void setPlayEvent(String ID)
    {
        boolean ret = false;

        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.getFaceID().equalsIgnoreCase(ID))
            {
                curFace.setHasPlayEvent(true);
                break;
            }
        }
        return ;
    }
    public static String getCurActiveID()
    {
        for(FaceIDBean curFace:FaceIDList)
        {
            if(curFace.isActiveNow())
            {
                return curFace.getFaceID();
            }
        }

        return null;
    }

    //刷新所有的ID，都在下次收到refresh时更新last time
    private static void allFaceReadyForNewEvent()
    {
        for(FaceIDBean curFace:FaceIDList)
        {
            curFace.faceIdleHook();
        }
    }

    //获得已经保存的FaceIDList
    public static List<FaceIDBean> getFaceIDList() {
        List<FaceIDBean> newOne;
        String json = FileUtils.readTextLine(Environment.getExternalStorageDirectory() + File.separator + Config.FACEID_STORE_FILE_PATH);
        Log.e(TAG, "getFaceIDList: "+json );
        if(json.length() <= 1)
        {
            json = FileUtils.readTextLine(Environment.getExternalStorageDirectory() + File.separator + Config.FACEID_STORE_BAK_FILE_PATH);
            if(json.length() <= 1) {
                return new ArrayList<FaceIDBean>();
            }
        }
        newOne =  JsonUtils.jsonToList(json, FaceIDBean.class);

        newOne = checkAndDeleteOldFace(newOne);  //排序并且删掉超了的

        return newOne;
    }


    //保存FaceIDList
    public static void saveFaceIDList() {
        if(FaceIDList != null) {
            FaceIDList = checkAndDeleteOldFace(FaceIDList);
            String json = JsonUtils.listToJson(FaceIDList);
            FileUtils.saveTxtFile(Environment.getExternalStorageDirectory() + File.separator + Config.FACEID_STORE_FILE_PATH, json);
            FileUtils.saveTxtFile(Environment.getExternalStorageDirectory() + File.separator + Config.FACEID_STORE_BAK_FILE_PATH, json);
        }
    }

    //如果list的个数超限制，则根据刷新时间排序，把最早出现的face删掉
    private static List<FaceIDBean>  checkAndDeleteOldFace(List<FaceIDBean> list)
    {
        //排序
        Collections.sort(list);

        //判断是否有没有播放事件的，没有就删掉
        /*for(int i = list.size()-1; i >= 0; i--)
        {
            if((list.get(i) == null)||(list.get(i).getHasPlayEvent() == false))
            {
                list.remove(i);
            }
        }*/
        //printAll(list);
        Log.d(TAG, "FaceID number = " + list.size());
        //判断个数是否超
        if(list.size() <= Config.DefMaxFaceIDNum)
        {
            return list;
        }

        for(int i = list.size()-1; i >= Config.DefMaxFaceIDNum; i--)
        {
            list.remove(i);
        }

        //printAll(list);
        return list;
    }

    private static void printAll(List<FaceIDBean> list)
    {
        Log.d(TAG, "******************************************\n");
        for(int i = 0; i < list.size(); i++)
        {
            Log.d(TAG, i+" "+ list.get(i).getFaceID()+ " "+list.get(i).getRefreshTime());
        }
        return ;
    }
}
