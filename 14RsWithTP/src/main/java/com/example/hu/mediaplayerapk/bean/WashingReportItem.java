package com.example.hu.mediaplayerapk.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class WashingReportItem {

    int washingEventCnt = 0; //洗手总次数
    int moveAwayCnt = 0;     //终端次数
    int tempErrorCnt = 0;    //错误测温
    int tempValidCnt = 0;    //有效测温
    double averageTemp;      //平均体温
    double lastTemp;  //最后一次体温
    String FaceID;
    int isLadyOrMen;  //-1:unknown: 1:lady, 0: men

    public WashingReportItem()
    {
        washingEventCnt = moveAwayCnt = tempErrorCnt = tempValidCnt =  0;
        averageTemp = 0;
        lastTemp = 0;
        FaceID = "null";
        isLadyOrMen = -1;
        return;
    }

    public WashingReportItem(String ID)
    {
        washingEventCnt = moveAwayCnt = tempErrorCnt = tempValidCnt = 0;
        averageTemp = 0;
        lastTemp = 0;
        FaceID = ID;
        isLadyOrMen = -1;
        return;
    }

    @Generated(hash = 1877529764)
    public WashingReportItem(int washingEventCnt, int moveAwayCnt, int tempErrorCnt, int tempValidCnt, double averageTemp, double lastTemp, String FaceID,
            int isLadyOrMen) {
        this.washingEventCnt = washingEventCnt;
        this.moveAwayCnt = moveAwayCnt;
        this.tempErrorCnt = tempErrorCnt;
        this.tempValidCnt = tempValidCnt;
        this.averageTemp = averageTemp;
        this.lastTemp = lastTemp;
        this.FaceID = FaceID;
        this.isLadyOrMen = isLadyOrMen;
    }

    public void setFaceID(String ID)
    {
        FaceID = ID;
        return;
    }

    public void setWashingEventCnt(int cnt)
    {
        washingEventCnt = cnt;
    }

    public void setMoveAwayCnt(int cnt)
    {
        moveAwayCnt = cnt;
    }

    public void setTempErrorCnt(int cnt)
    {
        tempErrorCnt = cnt;
    }

    public void setTempValidCnt(int cnt)
    {
        tempValidCnt = cnt;
    }

    public void setAverageTemp(double temp)
    {
        int tmp = (int)Math.round(temp*100.0);

        averageTemp = tmp/100.0;
    }

    public void setLastTemp(double temp)
    {
        int tmp = (int)Math.round(temp*100.0);

        lastTemp = tmp/100.0;
    }

    public int getWashingEventCnt()
    {
        return washingEventCnt;
    }

    public int getMoveAwayCnt()
    {
        return moveAwayCnt;
    }

    public int getTempErrorCnt()
    {
        return tempErrorCnt;
    }

    public int getTempValidCnt()
    {
        return tempValidCnt;
    }

    public String getFaceID()
    {
        return FaceID;
    }

    public double getAverageTemp()
    {
        return averageTemp;
    }

    public double getLastTemp() {return lastTemp;}
    public int getGender(){return isLadyOrMen;}
    public void setGender(int isLady){ isLadyOrMen = isLady;}

    public String toString()
    {
        String ret = "Face:" +FaceID+" WashingEvent:"+washingEventCnt+ " moveAway:"+ moveAwayCnt+ " tempError:"+ tempErrorCnt +" averageTemp: "+ averageTemp;

        return ret;
    }

    public int getIsLadyOrMen() {
        return this.isLadyOrMen;
    }

    public void setIsLadyOrMen(int isLadyOrMen) {
        this.isLadyOrMen = isLadyOrMen;
    }

}
