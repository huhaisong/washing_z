package com.example.hu.mediaplayerapk.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class WashingReportItem {

    @Id(autoincrement = true)
    Long id;//数据库ID 自增长
    String FaceID;    //faceID
    int washingEventCnt = 0; //洗手总次数
    int moveAwayCnt = 0;     //是否中断
    int tempErrorCnt = 0;    //错误测温   //0为正确测温  1为错误测温
    int tempValidCnt = 0;    //有效测温
    double averageTemp;      //平均体温
    double lastTemp;  //最后一次体温
    int isLadyOrMen;  //男女 -1:unknown: 1:lady, 0: men
    private int playNum;  //判断是初回动画还是2回动画  1初回 2 2回动画
    private int isPlayInterrupt;  //视频是否中断  1 打断 0非打断
    private int isLongInterval;  //是否是长间隔  1是长间隔 0非长间隔
    int time;  //洗手的时间   单位（s）

    @Generated(hash = 2057643641)
    public WashingReportItem(Long id, String FaceID, int washingEventCnt,
            int moveAwayCnt, int tempErrorCnt, int tempValidCnt, double averageTemp,
            double lastTemp, int isLadyOrMen, int playNum, int isPlayInterrupt,
            int isLongInterval, int time) {
        this.id = id;
        this.FaceID = FaceID;
        this.washingEventCnt = washingEventCnt;
        this.moveAwayCnt = moveAwayCnt;
        this.tempErrorCnt = tempErrorCnt;
        this.tempValidCnt = tempValidCnt;
        this.averageTemp = averageTemp;
        this.lastTemp = lastTemp;
        this.isLadyOrMen = isLadyOrMen;
        this.playNum = playNum;
        this.isPlayInterrupt = isPlayInterrupt;
        this.isLongInterval = isLongInterval;
        this.time = time;
    }
    @Generated(hash = 1803537257)
    public WashingReportItem() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFaceID() {
        return this.FaceID;
    }
    public void setFaceID(String FaceID) {
        this.FaceID = FaceID;
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
    public int getIsLadyOrMen() {
        return this.isLadyOrMen;
    }
    public void setIsLadyOrMen(int isLadyOrMen) {
        this.isLadyOrMen = isLadyOrMen;
    }
    public int getPlayNum() {
        return this.playNum;
    }
    public void setPlayNum(int playNum) {
        this.playNum = playNum;
    }
    public int getIsPlayInterrupt() {
        return this.isPlayInterrupt;
    }
    public void setIsPlayInterrupt(int isPlayInterrupt) {
        this.isPlayInterrupt = isPlayInterrupt;
    }
    public int getIsLongInterval() {
        return this.isLongInterval;
    }
    public void setIsLongInterval(int isLongInterval) {
        this.isLongInterval = isLongInterval;
    }
    public int getTime() {
        return this.time;
    }
    public void setTime(int time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "WashingReportItem{" +
                "id=" + id +
                ", FaceID='" + FaceID + '\'' +
                ", washingEventCnt=" + washingEventCnt +
                ", moveAwayCnt=" + moveAwayCnt +
                ", tempErrorCnt=" + tempErrorCnt +
                ", tempValidCnt=" + tempValidCnt +
                ", averageTemp=" + averageTemp +
                ", lastTemp=" + lastTemp +
                ", isLadyOrMen=" + isLadyOrMen +
                ", playNum=" + playNum +
                ", isPlayInterrupt=" + isPlayInterrupt +
                ", isLongInterval=" + isLongInterval +
                ", time=" + time +
                '}';
    }
}
