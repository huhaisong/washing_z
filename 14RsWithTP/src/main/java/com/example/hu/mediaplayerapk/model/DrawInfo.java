package com.example.hu.mediaplayerapk.model;

import android.graphics.Rect;

public class DrawInfo {
    private Rect rect;
    private int sex;
    private int age;
    private int liveness;
    private String name = null;
    private boolean hideRect;

    public DrawInfo(Rect rect, int sex, int age,int liveness,String name) {
        this.rect = rect;
        this.sex = sex;
        this.age = age;
        this.liveness = liveness;
        this.name = name;
        this.hideRect = false;
    }

    public DrawInfo(Rect rect, int sex, int age,int liveness,String name, boolean onlyStr) {
        this.rect = rect;
        this.sex = sex;
        this.age = age;
        this.liveness = liveness;
        this.name = name;
        this.hideRect = onlyStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }

    public boolean getHideRect(){
        return this.hideRect;
    }
}
