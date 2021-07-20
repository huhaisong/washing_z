package com.example.hu.mediaplayerapk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.adapter.WashingReportDetailAdapter;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.dao.WashingReportManager;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChangeAdminPassActivity extends BaseActivity {


    private Context mContext;
    private EditText oldPassEditText, newPassEditText, newPassConfirmEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_admin_pass);
        mContext = this;

        oldPassEditText = findViewById(R.id.et_old_password);
        newPassEditText = findViewById(R.id.et_new_password);
        newPassConfirmEditText = findViewById(R.id.et_confirm_new_password);
        confirmButton = findViewById(R.id.button_confirm);
        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        oldPassEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        newPassEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        newPassConfirmEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPasswordString = oldPassEditText.getText().toString();
                String newPasswordString = newPassEditText.getText().toString();
                String confirmNewPasswordString = newPassConfirmEditText.getText().toString();
                if (TextUtils.isEmpty(oldPasswordString)) {
                    Toast.makeText(mContext, "古いパスワードを空にすることはできません", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(newPasswordString)) {
                    Toast.makeText(mContext, "新しいパスワードを空にすることはできません", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmNewPasswordString)) {
                    Toast.makeText(mContext, "パスワードを空白にすることはできません", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!confirmNewPasswordString.equals(newPasswordString)) {
                    Toast.makeText(mContext, "新しいパスワードと確認済みのパスワードに一貫性がありません", Toast.LENGTH_LONG).show();
                    return;
                }
                if (oldPasswordString.equals(SPUtils.getString(mContext, Config.ADMIN_PASS, "1234"))) {
                    SPUtils.putString(mContext, Config.ADMIN_PASS, newPasswordString);
                    Toast.makeText(mContext, "パスワードのリセットが完了しました", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(mContext, "古いパスワードエラー", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
