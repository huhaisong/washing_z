package com.example.hu.mediaplayerapk.util;

import android.util.Log;

import com.example.hu.mediaplayerapk.bean.WashingReportItem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class washingLogUtil {

    private static String TAG = "washingLogUtil";

    public static ArrayList<WashingReportItem> buildAllReport() {
        String FolderPath;
        ArrayList<WashingReportItem> allReportList = new ArrayList<>();
        WashingReportItem oneIDReport;
        //轮询文件
        FolderPath = Logger.getWashingRawLogFolderPath();
        try {
            File oldfile = new File(FolderPath);
            if (!oldfile.exists()) {
                return null;
            }
            if (!oldfile.canRead()) {
                return null;
            }
            String[] file = oldfile.list();
            File temp = null;

            if (file == null || file.length < 1)
                return null;

            for (int i = 0; i < file.length; i++) {
                Log.d(TAG, "buildAllReport: " + file[i]);
                if (FolderPath.endsWith(File.separator)) {
                    temp = new File(FolderPath + file[i]);
                } else {
                    temp = new File(FolderPath + File.separator + file[i]);
                }
                if (temp.isDirectory()) {
                    oneIDReport = buildSingleReport(file[i]);
                    if (oneIDReport != null) {
                        allReportList.add(oneIDReport);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
        return allReportList;
    }

    //reBuild washing Report Log
    //1.read all raw log files and build new report files
    //single report format: statis and raw log
    private static WashingReportItem buildSingleReport(String ID) {
        boolean result = true;

        WashingReportItem totalReport = new WashingReportItem();
        totalReport.setFaceID(ID);
        WashingReportItem singleDayReport;//= new WashingReportItem(ID);
        String FolderPath;

        Log.d(TAG, "buildSingleReport " + ID);
        //轮询文件
        FolderPath = Logger.getWashingRawLogIDFolderPath(ID);
        try {
            File oldfile = new File(FolderPath);
            if (!oldfile.exists()) {
                Log.e(TAG, oldfile.getAbsolutePath() + " not exist");
                return null;
            }
            if (!oldfile.canRead()) {
                Log.e(TAG, oldfile.getAbsolutePath() + " cannot read");
                return null;
            }
            String[] file = oldfile.list();
            File temp = null;

            if (file == null || file.length < 1)
                return null;

            for (int i = 0; i < file.length; i++) {
                Log.d(TAG, "buildSingleReport: " + file[i]);
                if (FolderPath.endsWith(File.separator)) {
                    temp = new File(FolderPath + file[i]);
                } else {
                    temp = new File(FolderPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    singleDayReport = readSingleWashingLog(ID, temp.getPath());
                    if (singleDayReport != null) {
                        if ((totalReport.getTempValidCnt() + singleDayReport.getTempValidCnt()) > 0) {
                            totalReport.setAverageTemp((totalReport.getTempValidCnt() * totalReport.getAverageTemp() + singleDayReport.getTempValidCnt() * singleDayReport.getAverageTemp()) / (totalReport.getTempValidCnt() + singleDayReport.getTempValidCnt()));
                        }
                        totalReport.setTempValidCnt(totalReport.getTempValidCnt() + singleDayReport.getTempValidCnt());
                        totalReport.setWashingEventCnt(totalReport.getWashingEventCnt() + singleDayReport.getWashingEventCnt());
                        totalReport.setMoveAwayCnt(totalReport.getMoveAwayCnt() + singleDayReport.getMoveAwayCnt());
                        totalReport.setTempErrorCnt(totalReport.getTempErrorCnt() + singleDayReport.getTempErrorCnt());
                        if ((totalReport.getIsLadyOrMen() == -1) && (singleDayReport.getIsLadyOrMen() != -1)) {
                            totalReport.setIsLadyOrMen(singleDayReport.getIsLadyOrMen());
                        }

                        if ((totalReport.getLastTemp() == 0) && (singleDayReport.getLastTemp() != 0)) {
                            totalReport.setLastTemp(singleDayReport.getLastTemp());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }

        Log.d(TAG, totalReport.toString());


        //TBD: build report
        return totalReport;
    }

    public static WashingReportItem readSingleWashingLog(String ID, String path) {
        File file = new File(path);
        BufferedReader reader;
        WashingReportItem item = new WashingReportItem();
        item.setFaceID(ID);
        int tmp;
        double tmpDouble;
        double totalTempValue = 0;
        double lastTempValue = 0;
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {// utf-8
//                Log.e(TAG, "readImpactvTXT: utf-8");
                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
//                Log.e(TAG, "readImpactvTXT: unicode");
                reader = new BufferedReader(new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
//                Log.e(TAG, "readImpactvTXT: utf-16be");
                reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
//                Log.e(TAG, "readImpactvTXT: utf-16le");
                reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
            } else if (first3bytes[0] == 91 && first3bytes[1] == 80 && first3bytes[2] == 114) {
//                Log.e(TAG, "readImpactvTXT: Shift_JIS");
                reader = new BufferedReader(new InputStreamReader(in, "Shift_JIS"));
            } else {
//                Log.e(TAG, "readImpactvTXT: GBK");
                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();
            while (str != null) {
                //日期，时间，性别,是否洗手事件，是否中途离开事件，是否体温异常事件,体温值（换行）
                String[] strArray = str.split(",");
                if (strArray.length == 7) {
                    try {
                        if (strArray[2].equalsIgnoreCase("1.0")) {
                            item.setIsLadyOrMen(1);
                        } else if (strArray[2].equalsIgnoreCase("-1.0")) {
                            item.setIsLadyOrMen(0);
                        }
                        tmp = Integer.valueOf(strArray[3]);
                        if (tmp == 1) {
                            item.setWashingEventCnt(item.getWashingEventCnt() + 1);
                        }

                        tmp = Integer.valueOf(strArray[4]);
                        if (tmp == 1) {
                            item.setMoveAwayCnt(item.getMoveAwayCnt() + 1);
                        }

                        tmp = Integer.valueOf(strArray[5]);
                        if (tmp == 1) {
                            item.setTempErrorCnt(item.getTempErrorCnt() + 1);
                        }

                        tmpDouble = Double.valueOf(strArray[6]);
                        if (tmpDouble != 0) {
                            item.setTempValidCnt(item.getTempValidCnt() + 1);
                            totalTempValue += tmpDouble;
                            //if(lastTempValue == 0)
                            {
                                lastTempValue = tmpDouble;
                            }
                        }
                    } catch (java.lang.NumberFormatException e) {
                        Log.e(TAG, "Parse Temp error " + e.toString());
                    }
                } else {
                    Log.e(TAG, "Read Log error " + str);
                }
                str = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (item.getTempValidCnt() != 0) {
            item.setAverageTemp(totalTempValue / item.getTempValidCnt());
            item.setLastTemp(lastTempValue);
        }

        Log.d(TAG, item.toString());
        return item;
    }

}
