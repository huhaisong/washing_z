package com.example.hu.mediaplayerapk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.hu.mediaplayerapk.R;

/**
 * Created by Administrator on 2016/11/7.
 */
public class WashingChooseDialog extends Dialog {

    private ClickListenerInterface clickListenerInterface;
    private Context mContext;
    private static final String TAG = "WashingChooseDialog";
    private static int WASHINGDIALOG_QUIT_TIME = 15*1000;  //15秒后退出

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "QUIT Dialog");
            clickListenerInterface.select(0);  //退出
            mHandler.removeCallbacksAndMessages(null);
            return false;
        }
    });
    public interface ClickListenerInterface {
        public void select(int i);
    }

    public WashingChooseDialog(Context context) {
        super(context);
        this.mContext = context;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mHandler.sendEmptyMessageDelayed(0, WASHINGDIALOG_QUIT_TIME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.washing_choose_dialog, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //去除title
        setCancelable(false); //设置点击白色不消失
        setContentView(view);

        ClickListen clickListen = new ClickListen();

        view.findViewById(R.id.iv_washing_choose_yes).setOnClickListener(clickListen);
        view.findViewById(R.id.iv_washing_choose_no).setOnClickListener(clickListen);
    }

    public void setClickListen(ClickListenerInterface clickListen) {
        this.clickListenerInterface = clickListen;
    }

    private class ClickListen implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mHandler.removeCallbacksAndMessages(null);
            clickListenerInterface.select(v.getId());
        }
    }
}
