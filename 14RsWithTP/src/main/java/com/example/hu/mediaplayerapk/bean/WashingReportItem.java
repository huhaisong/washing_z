package com.example.hu.mediaplayerapk.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class WashingReportItem {

    int washingEventCnt = 0; //洗手总次数
    int moveAwayCnt = 0;     //是否中断
    int tempErrorCnt = 0;    //错误测温
    int tempValidCnt = 0;    //有效测温
    double averageTemp;      //平均体温
    double lastTemp;  //最后一次体温
    String FaceID;
    int isLadyOrMen;  //男女 -1:unknown: 1:lady, 0: men
    private int lastPlayNum;
    private int lastPlayTime;
    int time;

    @Generated(hash = 1594989526)
    public WashingReportItem(int washingEventCnt, int moveAwayCnt, int tempErrorCnt,
            int tempValidCnt, double averageTemp, double lastTemp, String FaceID,
            int isLadyOrMen, int lastPlayNum, int lastPlayTime, int time) {
        this.washingEventCnt = washingEventCnt;
        this.moveAwayCnt = moveAwayCnt;
        this.tempErrorCnt = tempErrorCnt;
        this.tempValidCnt = tempValidCnt;
        this.averageTemp = averageTemp;
        this.lastTemp = lastTemp;
        this.FaceID = FaceID;
        this.isLadyOrMen = isLadyOrMen;
        this.lastPlayNum = lastPlayNum;
        this.lastPlayTime = lastPlayTime;
        this.time = time;
    }

    @Generated(hash = 1803537257)
    public WashingReportItem() {
    }

    public int getWashingEventCnt() {
        return this.washingEventCnt;
    }

    public void setWashingEventCnt(int washingEventCnt) {
        this.washingEventCnt = washingEventCnt;
    }

    public int getMoveAwayCnt() {
        return this.moveAwayCnt;
    }

    public void setMoveAwayCnt(int moveAwayCnt) {
        this.moveAwayCnt = moveAwayCnt;
    }

    public int getTempErrorCnt() {
        return this.tempErrorCnt;
    }

    public void setTempErrorCnt(int tempErrorCnt) {
        this.tempErrorCnt = tempErrorCnt;
    }

    public int getTempValidCnt() {
        return this.tempValidCnt;
    }

    public void setTempValidCnt(int tempValidCnt) {
        this.tempValidCnt = tempValidCnt;
    }

    public double getAverageTemp() {
        return this.averageTemp;
    }

    public void setAverageTemp(double averageTemp) {
        this.averageTemp = averageTemp;
    }

    public double getLastTemp() {
        return this.lastTemp;
    }

    public void setLastTemp(double lastTemp) {
        this.lastTemp = lastTemp;
    }

    public String getFaceID() {
        return this.FaceID;
    }

    public void setFaceID(String FaceID) {
        this.FaceID = FaceID;
    }

    public int getIsLadyOrMen() {
        return this.isLadyOrMen;
    }

    public void setIsLadyOrMen(int isLadyOrMen) {
        this.isLadyOrMen = isLadyOrMen;
    }

    public int getLastPlayNum() {
        return this.lastPlayNum;
    }

    public void setLastPlayNum(int lastPlayNum) {
        this.lastPlayNum = lastPlayNum;
    }

    public int getLastPlayTime() {
        return this.lastPlayTime;
    }

    public void setLastPlayTime(int lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
