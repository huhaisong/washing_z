package com.example.hu.mediaplayerapk.bean;

import android.util.Log;

import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.util.SPUtils;

public class FaceIDBean implements Comparable<FaceIDBean>{
    private static final String TAG = "FaceManagerUtil";
    private String FaceID;
    private long refreshTime;  //in milliseconds
    private long lastTimeInterval;  //在setTime的时候跟之前的值之差
    private boolean autoUpdateLastInterval;  //在上层检测到人离开后，就设定下次所有的ID都自动更新lastTimeInterval
    private long lastEventEndTime;  //记录上次Event人脸最后出现的时间
    private long curEventStartTime;  //记录当前Event人脸最开始出现的时间
    private boolean isActiveNow;
    private boolean genderIsLady; //true:lady; false
    private int lastPlayNum;
    private int lastPlayTime;
    private boolean hasPlayEvent = false;

    public FaceIDBean(String newID, long newTime, boolean isLady)
    {
        FaceID = newID;
        refreshTime = newTime;
        lastTimeInterval = -1;
        autoUpdateLastInterval = false;
        curEventStartTime = newTime;
        isActiveNow = false;
        genderIsLady = isLady;
        lastPlayNum = -1;
        lastPlayTime = -1;
        hasPlayEvent = false;
        Log.d(TAG, "newFaceID " + newID+ " "+newTime);
    }

    public void setFaceID(String newID)
    {
        FaceID = newID;
    }

    public void setLastPlayNum(int newPlayNum)
    {
        lastPlayNum = newPlayNum;
    }
    public int getLastPlayNum() {return lastPlayNum;}

    public void setLastPlayTime(int newPlayTime)
    {
        lastPlayTime = newPlayTime;
    }
    public int getLastPlayTime() {return lastPlayTime;}

    public boolean getGenderIsLady() {return genderIsLady;}

    public void setHasPlayEvent(boolean input)
    {
        hasPlayEvent = input;
    }
    public boolean getHasPlayEvent()
    {
        return hasPlayEvent;
    }

    public void faceIdleHook()
    {
        autoUpdateLastInterval = true;
        lastEventEndTime = refreshTime;
        curEventStartTime = -1;  //清空
        isActiveNow = false;
        //Log.d(TAG, "faceIdleHook " + FaceID+" LastEventEd "+lastEventEndTime);
    }

    public void setRefreshTime(long curTime)
    {
        if(autoUpdateLastInterval == true) {
            if(lastEventEndTime != -1) {
                lastTimeInterval = curTime - lastEventEndTime;
            }
            else
            {
                lastTimeInterval = -1;
            }

            curEventStartTime = curTime;
            autoUpdateLastInterval = false;
        }
        refreshTime = curTime;
        Log.d(TAG, "setRefreshTime " + FaceID+ " "+refreshTime +" "+lastTimeInterval);
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    //获取当前Event持续的时间
    public long getCurEventKeepTime()
    {
        return (refreshTime - curEventStartTime);
    }

    public String getFaceID()
    {
        return FaceID;
    }

    public long getLastTimeInterval()
    {
        return lastTimeInterval;
    }

    public boolean isActiveNow()
    {
        return isActiveNow;
    }

    public void setActive(boolean yes)
    {
        isActiveNow = yes;
    }

    @Override
    public int compareTo(FaceIDBean o) {
        //如何重写方法
        //按face最近出现的时间降序排序比较
        //this(当前调用方法的对象)   o(参数传入)
        //所有比较最底层的逻辑都是发生两两比较逻辑的,返回比较结果
        //只关心结果结果三种:
        //正数:   this.refreshTime - o.refreshTime    >
        //负数:   this.refreshTime - o.refreshTime    <
        //0       this   ==


//        return this.refreshTime-o.refreshTime; 升序排序
//        return o.refreshTime-this.refreshTime; 降序排序
        //按照最近人脸出现的时间降序排列
        return (int)((o.refreshTime-this.refreshTime)/100);
    }
}
