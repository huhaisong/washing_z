package com.example.hu.mediaplayerapk.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.ui.activity.MainActivity;
import com.example.hu.mediaplayerapk.ui.activity.SimpleSettingActivity;
import com.example.hu.mediaplayerapk.ui.widget.PayDialog;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/4/6.
 * 用于界面的手势退出
 */

public class TouchModel {

    private Context mContext;
    private int xTouch;
    private int yTouch;
    private String tapValue;

    public TouchModel(Context mContext) {
        this.mContext = mContext;
        tapValue = "null";
    }

    private void checkSecretTap() {
        Object localObject = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point localPoint = new Point();
        ((Display) localObject).getSize(localPoint);
        int i = localPoint.x / 2;
        int j = localPoint.y / 2;

        if ((this.xTouch < i) && (this.yTouch < j))
            this.tapValue = this.tapValue.concat("1");
        else if ((this.xTouch >= i) && (this.yTouch < j))
            this.tapValue = this.tapValue.concat("2");
        else if ((this.xTouch < i) && (this.yTouch >= j))
            this.tapValue = this.tapValue.concat("3");
        else if ((this.xTouch >= i) && (this.yTouch >= j))
            this.tapValue = this.tapValue.concat("4");

        if ("4321".equals(this.tapValue)) {
            /*localObject = new Intent("android.intent.action.MAIN");
            ((Intent) localObject).addCategory("android.intent.category.HOME");
            mContext.startActivity((Intent) localObject);*/
            PayDialog payDialog = new PayDialog(mContext, new PayDialog.FinishedPass() {
                @Override
                public void finishedpass(String payPass) {
                    if (payPass.equals(SPUtils.getString(mContext, Config.ADMIN_PASS, "1234"))) {
                        Intent intent = new Intent(mContext, SimpleSettingActivity.class);
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "間違ったパスワード", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            payDialog.show();
        }
    }

    private static final String TAG = "TouchModel";

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        switch (paramMotionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.xTouch = (int) paramMotionEvent.getX();
                this.yTouch = (int) paramMotionEvent.getY();
                return true;
            case 1:
                float f2 = paramMotionEvent.getX();
                if (this.xTouch > 200.0F + f2) {
                    this.tapValue = "";
                    return true;
                }
                checkSecretTap();
                return true;
            default:
                return false;
        }
    }
}
