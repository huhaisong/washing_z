package com.example.hu.mediaplayerapk.util;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.hu.mediaplayerapk.config.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2020-08-15.
 */

public class Logger {

    public static final String LogCMDBootup = "Bootup";
    public static final String LogCMDWIFIConnected = "WifiConnected";
    public static final String LogCMDWIFIDiscConnected = "WifiDisconnected";
    public static final String LogCMDPlayFile = "PlayFile";
    public static final String LogCMDHumanDetected = "HumanDetected";
    public static final String LogCMDStartDownloadContent = "DownloadContentStarted";
    public static final String LogCMDFinishDownloadContent = "DownloadContentFinshed";
    public static final String LogCMDFinishUDISKUpdateContent = "UdiskDownloadContentFinshed";
    public static final String LogCMDError = "Error";
    public static final String LogCMDInfor = "Infor";

    private static final String TAG = "Logger";

    private static String getSerialNumber() {
        String serial = "null";
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
        {
            //serial = Build.getSerial();
        }
        else
        {
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return serial;
        //return "PW8010A000001";
    }
    private static String getCurrentFormatDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    private static String getCurrentFormatTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    private static String getCurrentFormatTimeOnly(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String getLogROOTPATH() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder;
    }

    public static String getLogPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder
                + File.separator + Build.MODEL + "_" +getSerialNumber()+"_"+  Config.SysLogName + "_"+ getCurrentFormatDate() + ".log";
    }

    public static String getCSVLogPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder
                + File.separator + Build.MODEL + "_" +getSerialNumber()+"_"+  Config.PlayLogName+"_"+ getCurrentFormatDate() + ".csv";
    }

    public static String getFaceLogPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder
                + File.separator + Build.MODEL + "_" +getSerialNumber()+"_"+  Config.FaceLogName+"_"+ getCurrentFormatDate() + ".csv";
    }

    public static String getWashingRawLogFilePath(String ID)
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder+File.separator+Config.WashingRawFolderName
                + File.separator + ID+File.separator+getSerialNumber()+"_"+  ID+"_"+ getCurrentFormatDate() + ".csv";
    }

    public static String getWashingRawLogIDFolderPath(String ID)
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder+File.separator+Config.WashingRawFolderName
                + File.separator + ID+File.separator ;
    }

    public static String getWashingRawLogFolderPath()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Config.LOGFolder+File.separator+Config.WashingRawFolderName
                + File.separator  ;
    }

    private static File creatFileIfNotExist(String path) {

        File file = new File(path);
        if (!file.exists()) {  //check whether file exists
            try {
                new File(path.substring(0, path.lastIndexOf(File.separator))).mkdirs();
                file.createNewFile();
                file.setWritable(Boolean.TRUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean CheckAndDeleteOldLog()
    {
        checkAndDeleteOldLog(getLogROOTPATH(), 7 ,true);
        //checkAndDeleteOldLog(getLogPath(), 90, false);
        //checkAndDeleteOldLog(getCSVLogPath(), 90, false);
        return true;
    }

    private static boolean checkAndDeleteOldLog(String FolderPath, int days, boolean includeSubDir) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        long nowTime = c.getTimeInMillis();
        Log.d(TAG, "CheckAndDeleteOldLog: " + FolderPath +" "+ c.getTime());
        long targetTime = nowTime - (long)days*24*60*60*1000;
        c.setTimeInMillis(targetTime);
        Log.d(TAG, "Target time "+ c.getTime());
        try {
            File oldfile = new File(FolderPath);
            if (!oldfile.exists()) {
                return false;
            }
            if (!oldfile.canRead()) {
                return false;
            }
            String[] file = oldfile.list();
            File temp = null;

            if (file == null || file.length <= 0)
                return false;

            for (int i = 0; i < file.length; i++) {
                Log.d(TAG, "Check Log file: " + file[i]);
                if (FolderPath.endsWith(File.separator)) {
                    temp = new File(FolderPath + file[i]);
                } else {
                    temp = new File(FolderPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    c.setTimeInMillis(temp.lastModified());
                    Log.d(TAG, file[i] +" Modified time " +c.getTime() );
                    if(temp.lastModified() < targetTime)
                    {
                        Log.e(TAG,"Delete Log file "+ file[i]);
                        temp.delete();
                    }
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    if(includeSubDir == true) {
                        checkAndDeleteOldLog(temp.getPath(), days, includeSubDir);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
        return true;
    }
    /**
     * 保存字符串
     */
    private static void saveTxtFile(String filePath, String text) {
        try {
            creatFileIfNotExist(filePath);
            FileOutputStream out = new FileOutputStream(filePath, true);
            // create a file writer
            OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
            writer.write(text);
            writer.close();
            out.close();
        } catch (Exception e) {
            String ext = e.getLocalizedMessage();
            Log.e(TAG, "saveTxtFile: ext = " + ext);
            e.printStackTrace();
        }
    }

    public static void TextLoggerAppend(String textLine)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly() +"," +textLine +"\n";
        Log.d(TAG, "LoggerAppend: " + tower);
        saveTxtFile(getLogPath(), tower);
    }

    public static void TextLoggerAppend(String header, String textLine)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly() +","+ header+","+textLine +"\n";
        Log.d(TAG, "LoggerAppend: " + tower);
        saveTxtFile(getLogPath(), tower);
    }

    //
    //统计相关的log
    //
    public static void CSVLoggerAppend(String header)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly()+","+ header +"\n";
        Log.d(TAG, "CSVLoggerAppend: " + tower);
        saveTxtFile(getCSVLogPath(), tower);
    }

    public static void CSVLoggerAppend(String header, String para1)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly() +","+ header+","+para1 +"\n";
        Log.d(TAG, "CSVLoggerAppend: " + tower);
        saveTxtFile(getCSVLogPath(), tower);
    }

    public static void CSVLoggerAppend(String header, String para1, String para2)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly() +","+ header+","+para1+","+para2 +"\n";
        Log.d(TAG, "CSVLoggerAppend: " + tower);
        saveTxtFile(getCSVLogPath(), tower);
    }

    public static void FaceLoggerAppend(String ID, String sex, String age, String focusTime, String focusCount)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly() +","+ ID+","+sex+","+age+","+focusTime +","+focusCount+"\n";
        Log.d(TAG, "FaceLoggerAppend: " + tower);
        saveTxtFile(getFaceLogPath(), tower);
    }

    public static void WashingLoggerAppend(String ID,String gender,  int washingEventCode, int moveAwayEventCode, int tempErrorEventCode,double tempValue)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly()+","+gender +","+ washingEventCode+","+moveAwayEventCode+","+tempErrorEventCode+","+tempValue +"\n";
        Log.d(TAG, "WashingLoggerAppend: " + tower);
        saveTxtFile(getWashingRawLogFilePath(ID), tower);
    }

    public static void WashingLoggerAppend(String ID,String gender,  int washingEventCode, int moveAwayEventCode, int tempErrorEventCode,String tempValue)
    {
        String tower = getCurrentFormatDate()+","+getCurrentFormatTimeOnly()+","+gender +","+ washingEventCode+","+moveAwayEventCode+","+tempErrorEventCode+","+tempValue +"\n";
        Log.d(TAG, "WashingLoggerAppend: " + tower);
        saveTxtFile(getWashingRawLogFilePath(ID), tower);
    }
}
