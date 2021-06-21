package com.example.hu.mediaplayerapk.emailUtil;

import android.content.Context;
import android.util.Log;

import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.io.UnsupportedEncodingException;

public class mailSenderUtil {
    private static String TAG = "mailSenderUtil";
    private static String sendEmail = "impacttvdev@gmail.com";//"malloc004@126.com";//发送方邮件
    private static String sendEmaiPassword = "hlgnbnybqurvulgi";//"YSDHNGXKYLKEXOPM";//发送方邮箱密码(或授权码)

    private static String errorReportSubject = "体温異常通知";
    public static void sendErrorReport(Context context, String emailContent, String attachFile)
    {
        Log.d(TAG, "\nSendError Report,Content: \n" + emailContent + " \nattachedFile: "+attachFile);
        SenderRunnable senderRunnable = new SenderRunnable(sendEmail, sendEmaiPassword);
        String Content;//
        try {
            Content = new String (emailContent.getBytes("UTF-8"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage().toString());
            Content = emailContent;
        }

        senderRunnable.setMail(errorReportSubject, Content,
                SPUtils.getString(context, Config.CFGWashingZEmailAddr, Config.DefWashingZEmailRXAddr), attachFile);
        new Thread(senderRunnable).start();
    }

    private static class SenderRunnable implements Runnable {

        private String user;
        private String password;
        private String subject;
        private String body;
        private String receiver;
        private MailSender sender;
        private String attachment;

        public SenderRunnable(String user, String password) {
            this.user = user;
            this.password = password;
            sender = new MailSender(user, password);
            String mailhost = user.substring(user.lastIndexOf("@") + 1,
                    user.lastIndexOf("."));
            if (!mailhost.equals("gmail")) {
                mailhost = "SMTP." + mailhost + ".com";
                Log.i(TAG, mailhost);
                sender.setMailhost(mailhost);
            }
        }

        public void setMail(String subject, String body, String receiver,
                            String attachment) {
            this.subject = subject;
            this.body = body;
            this.receiver = receiver;
            this.attachment = attachment;
        }

        public void run() {
            // TODO Auto-generated method stub
            try {
                sender.sendMail(subject, body, user, receiver, attachment);
                Log.d(TAG, "send email sucess");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                if (e.getMessage() != null)
                {
                    Log.e(TAG, "sending email failed " + e.getMessage().toString());
                }
                    //ToastUtils.show(MainActivity.this, e.getMessage().toString());
                e.printStackTrace();
            }
        }
    }
}
