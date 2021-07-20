package com.example.hu.mediaplayerapk.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.example.hu.mediaplayerapk.R;



/**
 * Created by ywl on 2017/2/28.
 */

public class PayDialog extends BaseDialog {

    private PayPwdEditText payPwdEditText;
    private TextView   closeTextView;

    private FinishedPass finishedPass;

    public PayDialog(Context context, FinishedPass finishedPass) {
        super(context);
        this.finishedPass = finishedPass;
    }

    public PayPwdEditText getPayPwdEditText() {
        return payPwdEditText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_dialog_lyaout);
        payPwdEditText = (PayPwdEditText) findViewById(R.id.ppet);
        closeTextView = findViewById(R.id.tv_close);
        closeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        payPwdEditText.initStyle(R.drawable.edit_num_bg_red, 4, 0.33f, R.color.colorAccent, R.color.colorAccent, 20);
        payPwdEditText.setOnTextFinishListener(new PayPwdEditText.OnTextFinishListener() {
            @Override
            public void onFinish(String str) {//密码输入完后的回调
                finishedPass.finishedpass(str);
                dismiss();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                payPwdEditText.setFocus();
                Log.e(TAG, "run: " + payPwdEditText.isFocused());
            }
        }, 100);
    }

    private static final String TAG = "PayDialog";


    public interface FinishedPass {
        public void finishedpass(String pass);
    }
}
