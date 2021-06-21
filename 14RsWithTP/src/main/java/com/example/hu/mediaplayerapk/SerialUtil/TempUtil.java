package com.example.hu.mediaplayerapk.SerialUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.serialport.SerialPort;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import temp.cv.com.tempreader.broadcast.USBDeviceReceive;


/**
 * Created by Administrator on 2020-08-07.
 */

public class TempUtil {
    private serialPortUtil mSerialPortUtil;
    private  SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private HandlerThread mHandlerThread;
    //private Semaphore mSemaphore = new Semaphore(1);
    public static final int NewTempReport = 0x50001;
    public static final int RXReport = 0x50002;

    private final String TAG = "TempUtil";
    private boolean rfInited = false;
    private boolean initialing = false;

    private final int NewUartData = 0;
    private final int SendUartData = 1;
    private final int UartNoDataTimeOut = 2;
    private final int NoDataTimeOutInterval = 2*1000;  //2秒无数据超时
    private float curTemp = 0f;

    private Handler callerHandler = null;

    private Handler uartDataHandler = null;
    private USBDeviceReceive usbDeviceReceiver = null;

    private class ReadThread extends Thread {
        byte[] buffer = new byte[64];
        public volatile boolean exit = false;
        @Override
        public void run() {
            super.run();
            //while (!isInterrupted()) {
            while(exit!= true){
                int size = 0;

                try {
                    //Log.d(TAG, "read start");
                    if (mInputStream == null) {
                        Log.e(TAG, "mInputStream == null");
                        return;
                    }
                    size = mInputStream.read(buffer);
                    //Log.d(TAG, "read "+size);
                    if (size > 0) {
                        //Log.d(TAG, "Read 1");
                        /*try {
                            mSemaphore.acquire(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        //Log.d(TAG, "Read 0");
                        onDataReceived(buffer, size);

                        if(mHandlerThread != null) {
                            uartDataHandler.removeMessages(NewUartData);
                            uartDataHandler.sendEmptyMessageDelayed(NewUartData, 20);
                        }

                        //mSemaphore.release(1);
                    }

                    try
                    {
                        Thread.sleep(200);//延时50ms
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    //Log.d(TAG, "read finish");
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void RFSyncInitInner(boolean needReinit)
    {
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
        mReadThread = new ReadThread();
        mReadThread.start();
        mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        initialing = true;
        uartDataHandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case NewUartData:
                        uartTimerHandler();
                        break;
                    case SendUartData:
                        gprs_at_cmd_send_pending();
                        break;
                    case UartNoDataTimeOut:
                        gprs_at_reply_nak();
                        uartTimerHandler();
                        break;
                }
                return false;
            }
        });
        rfInited = true;

        {
            rfTxRxInit();
        }

        if(needReinit == true) {
            setReportOnOff(true);
        }

    }

    public boolean TempInit(Context context, Handler mHandler, String path)
    {
        if(rfInited == true)
        {
            return true;
        }
        Log.d(TAG, "TempInit ");

        try {
            mSerialPortUtil = new serialPortUtil();
            mSerialPort = mSerialPortUtil.getSerialPort(path);
            callerHandler = mHandler;
            if(mSerialPort != null) {
                RFSyncInitInner(true);
            }
            else
            {
                Log.e(TAG, "Cannot open Temp dongle");
            }

        } catch (SecurityException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (InvalidParameterException e) {
            Log.e(TAG, e.toString());
        }
        return rfInited;
    }

    public void TempDeinit(Context context)
    {
        Log.d(TAG, "TempDeinit");
        mSerialPortUtil.closeSerialPort();

        if (mReadThread != null) {
            mReadThread.exit = true;

            //mSemaphore.release(1);
            //mReadThread.interrupt();

            //mReadThread.stop();
            mReadThread = null;
        }
        if (mHandlerThread != null) {
            //mHandlerThread.interrupt();
            mHandlerThread.quit();
            mHandlerThread = null;
            //mHandlerThread.stop();
        }

        mSerialPort = null;
        rfInited = false;
        mSerialPortUtil = null;
        System.gc();
    }

    public String getTempPath()
    {
        if((mSerialPort != null)&&(mSerialPortUtil != null))
        {
            return mSerialPortUtil.getPortPath();
        }
        return null;
    }

    public boolean isSerialPortAvalible()
    {
        if((mSerialPort != null)&&(mSerialPortUtil != null))
        {
            return mSerialPortUtil.SerialPortExist();
        }
        return false;
    }

    public void printByteAsHexWithLen(byte[] data, int dataLength)
    {
        StringBuilder r = new StringBuilder(" ");
        for(int j = 0; j<dataLength; j++)
        {
            int b = data[j] &0xff;
            if(b < 0x10)
            {
                r.append('0');
            }
            r.append(Integer.toHexString(b)).append(' ');
        }
        Log.d(TAG, r.toString());

    }
    public void printByteAsHex(byte[] data) {
        int pduLength = data.length;

        for(int i = 0; i < pduLength; i += 8)
        {
            StringBuilder r = new StringBuilder("SIMCOM SMS PDU data: ");
            for(int j = i; j < i+8 && j<pduLength; j++)
            {
                int b = data[j] &0xff;
                if(b < 0x10)
                {
                    r.append('0');
                }
                r.append(Integer.toHexString(b)).append(' ');
            }
            Log.d(TAG, r.toString());
        }

    }
    public String  printByteAsAscii(byte[] data) {
        String ascii_pdu ;

        try
        {
            ascii_pdu = new String(data, "UTF8");
            Log.d(TAG, ascii_pdu);
            return ascii_pdu;
        }
        catch(java.io.UnsupportedEncodingException e)
        {
            Log.e(TAG, e.toString());
            return e.toString();
        }

    }
    protected  void onDataReceived(final byte[] buffer, final int size)
    {
        //Log.d(TAG, "Received "+new String(buffer));
        rxDataEnqueue(buffer, size);
        return;
    }
/*
************************************************************************************************
* */
    enum at_cmd_reply_e
    {
        AT_CMD_REPLY_NULL,
        AT_CMD_REPLY_TEMP_ONOFF,
        AT_CMD_REPLY_TEMP_REPORT,
    }

    enum at_cmd_e
    {
        AT_CMD_SET_TEMP_ONOFF,
        AT_CMD_SET_SENSOR_ONOFF,

        AT_CMD_MAX_NUM,
    }

    private final int MAX_AT_REPLY_NUM = 128;
    private byte[] Ready_reply_buf = null;//new byte[MAX_AT_REPLY_NUM];
    private int Ready_reply_rptr = 0;
    private int Ready_reply_wptr = 0;
    private int Parsebuf_startptr = 0;
    private int Parsebuf_datalen = 0;
    private int nak_cnt = 0;
    private int cur_cmd_repeat_times = 0;
    private at_cmd_rx_list[] at_rx_list = {
            new at_cmd_rx_list(at_cmd_reply_e.AT_CMD_REPLY_TEMP_ONOFF, "+CVTempOn="),
            new at_cmd_rx_list(at_cmd_reply_e.AT_CMD_REPLY_TEMP_REPORT, "+Temp:"),

    };

    private at_cmd_list_t At_current_cmd ;//= new at_cmd_list_t(AT_CMD_MAX_NUM,"");
    private Queue<at_cmd_list_t> At_cmd_list = new LinkedList<at_cmd_list_t>();

    private void rfTxRxInit()
    {
        if(Ready_reply_buf == null)
        {
            Ready_reply_buf = new byte[MAX_AT_REPLY_NUM];
        }
        rxDataClear();
        Ready_reply_rptr = 0;
        Ready_reply_wptr = 0;
        Parsebuf_startptr = 0;
        Parsebuf_datalen = 0;
        nak_cnt = 0;

        if(At_current_cmd == null)
        {
            At_current_cmd = new at_cmd_list_t(at_cmd_e.AT_CMD_MAX_NUM,"");
        }
    }

    private void uartTimerHandler()
    {
        //Log.d(TAG, "Timer 1");
        /*try {
            mSemaphore.acquire(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //Log.d(TAG, "Timer 0");
        //printByteAsAscii(Ready_reply_buf);
        //printByteAsHex(Ready_reply_buf);
        gprs_at_reply_dequeue_and_parse();

        gprs_at_cmd_send_pending();
        //mSemaphore.release(1);
    }

    private void rxDataClear()
    {
        if(Ready_reply_buf != null) {
            byte[] temp = new byte[MAX_AT_REPLY_NUM];
            System.arraycopy(temp , 0, Ready_reply_buf, 0,  MAX_AT_REPLY_NUM);

        }
    }

    private void rxDataEnqueue(final byte[] data, final int size)
    {
        if(Ready_reply_buf != null)
        {
            if((size+Parsebuf_startptr) >= MAX_AT_REPLY_NUM)
            {
                Log.e(TAG, "Buffer overflow");
                Parsebuf_startptr = 0;//处理完，可以归0了
                Ready_reply_rptr = 0;
                Parsebuf_datalen = 0;
                rxDataClear();
                return;
            }
            System.arraycopy(data, 0, Ready_reply_buf, Parsebuf_startptr,  size);
            Parsebuf_startptr += size;
            Parsebuf_datalen += size;
            //printByteAsAscii(Ready_reply_buf);
            //printByteAsHex(Ready_reply_buf);
            printByteAsHexWithLen(data, size);
            //1`    Log.d(TAG, "Parsebuf_datalen " + Parsebuf_datalen );
        }
    }
    //返回1:说明busy
    private  int gprs_at_reply_dequeue_and_parse()
    {

        int valid_len = 0;
        int tail_len = 0;
        while(Parsebuf_startptr != Ready_reply_rptr)
        {
            valid_len = 0;
            byte[] line= new byte[128];
            tail_len = 0;
            for(int i = 0; i < Parsebuf_datalen; i++)
            {
                if(Ready_reply_buf[Ready_reply_rptr + i] != 0x0d)
                {
                    line[i] = Ready_reply_buf[Ready_reply_rptr + i];
                    valid_len++;
                }
                else
                {
                    if((i+1 < Parsebuf_datalen)&&(Ready_reply_buf[Ready_reply_rptr + i+1] == 0x0a))
                    {
                        tail_len = 2;
                    }
                    else
                    {
                        tail_len = 1;
                    }
                    break;
                }
            }

            if((valid_len != 0)&&(tail_len == 2))
            {
                line[valid_len] = 0x0d;
                line[valid_len+1] = 0x0a;
                line[valid_len+2] = 0x00;
                byte[] print_line = new byte[valid_len];
                System.arraycopy(line, 0, print_line, 0,  valid_len);
                Ready_reply_rptr += valid_len + tail_len; //0x0d,0x0a
                String strLine = new String(print_line);//.toString();
                Log.d(TAG, mSerialPortUtil.getPortPath()+" RX "+strLine);
                //printByteAsHex(line);
                gprs_at_reply_parse_from_buf(strLine);

                Parsebuf_datalen -= valid_len + tail_len;  //注意，此处-2是去掉了0x0d,0x0a
            }
            else
            {
                break;
            }
        }

        if (Parsebuf_startptr == Ready_reply_rptr)
        {
            Parsebuf_startptr = 0;//处理完，可以归0了
            Ready_reply_rptr = 0;
            Parsebuf_datalen = 0;
            rxDataClear();
            //P_BT_RTS = 1;
        }

        if(Parsebuf_startptr == Ready_reply_rptr)
        {
            return 0;  //空闲
        }
        else
        {
            return 1;
        }
    }


    //如果无需重发
    private int at_cmd_default_cbk(at_cmd_reply_e msg, at_cmd_e cmd, String msg_buf/*, unsigned char len*/)
    {
        int  ret = 0;
        cur_cmd_repeat_times = 0;
        switch(msg)
        {
            case AT_CMD_REPLY_TEMP_REPORT:
                String dataVal = msg_buf.replace("+Temp:","0");
                try {
                    float value = Float.valueOf(dataVal);
                    Log.d(TAG, dataVal + " " + value + " " + msg_buf);
                    if ((value - curTemp >= 0.1) || (curTemp - value >= 0.1)) {
                        curTemp = value;
                        if (callerHandler != null) {
                            Message sendmsg = new Message();
                            tempData msgData = new tempData();
                            sendmsg.what = NewTempReport;
                            msgData.portPath = mSerialPortUtil.getPortPath();
                            msgData.tempValue = curTemp;
                            //msgData.tempReader = this;
                            sendmsg.obj = (tempData) msgData;
                            callerHandler.sendMessage(sendmsg);
                        }
                    }
                }
                catch(java.lang.NumberFormatException e)
                {
                    Log.e(TAG, "Parse Temp error "+e.toString());
                }
                break;


            case AT_CMD_REPLY_TEMP_ONOFF:

                initialing = false;
                break;
            default:
                ret = -1;
                break;
        }

        return ret;
    }

    private void gprs_at_reply_nak()
    {
        At_current_cmd.cmd = at_cmd_e.AT_CMD_MAX_NUM;
    }

    //*************************************************************************
    //添加多一个参数，将数据内容也添加进来
    private int gprs_at_reply_dispatch(at_cmd_reply_e msg, String msg_buf/*, unsigned char len*/)
    {

        int ret = 0;
        if (At_current_cmd.cmd != at_cmd_e.AT_CMD_MAX_NUM)
        {
            ret = at_cmd_default_cbk(msg, At_current_cmd.cmd, msg_buf/*, len*/);
            //如果ret 为-1，则后面继续重发该命令，例如ATD，对方回了个ERROR，则继续拨打.
            if ((ret == 0)||(nak_cnt >= 4))
            {
                nak_cnt = 0;
                At_current_cmd.cmd = at_cmd_e.AT_CMD_MAX_NUM;
            }
            else
            {
                nak_cnt++;
            }
        }
        else
        {
            //可能会有RING,NO CARRIER等消息会主动过来
            ret = at_cmd_default_cbk(msg, at_cmd_e.AT_CMD_MAX_NUM,  msg_buf/*, len*/);
        }

        return ret;
    }
    //
    private int gprs_at_reply_parse_from_buf(String msg_buf)
    {
        at_cmd_reply_e reply_msg;

        reply_msg = _get_code(msg_buf);
        if (reply_msg != at_cmd_reply_e.AT_CMD_REPLY_NULL)
        {
            gprs_at_reply_dispatch(reply_msg, msg_buf/*, len*/);
            if(mHandlerThread != null) {
                uartDataHandler.removeMessages(UartNoDataTimeOut);
            }
        }
        else
        {
            Log.e(TAG, "CODE ERR\n");
            nak_cnt++;
            if(nak_cnt >= 12)
            {
                nak_cnt = 0;
                At_current_cmd.cmd = at_cmd_e.AT_CMD_MAX_NUM;
            }
            if (callerHandler != null) {
                Message sendmsg = new Message();
                tempData msgData = new tempData();
                sendmsg.what = RXReport;
                msgData.portPath = mSerialPortUtil.getPortPath();
                msgData.tempValue = 0;
                msgData.DispString = msg_buf;
                sendmsg.obj = (tempData) msgData;
                callerHandler.sendMessage(sendmsg);
            }
        }
        return 0;
    }

    private at_cmd_reply_e _get_code(String str)
    {
        int i;

        //print_str(str);
        for(i = 0; i < (at_rx_list.length); i++)
        {
            //print_str(at_reply_list[i].str);
            //print_str("\n");
            //if (/*(strlen(str) == strlen(at_reply_list[i].str))&&*/(strcmp(str, at_reply_list[i].str) == 0))
            //if (strstr(str, at_rx_list[i].str) != NULL)
            if(str.contains(at_rx_list[i].cmdStr))
            {
                return at_rx_list[i].cmd;
            }
            //print_str("\n\n");
        }

        return at_cmd_reply_e. AT_CMD_REPLY_NULL;
    }

    //*************************************************************************
    private int at_cmd_send_to_list(at_cmd_e cmd, String cmdStr)
    {
        //if (cmd != at_cmd_e.AT_CMD_MAX_NUM)
        {
            if(At_current_cmd.cmd != at_cmd_e.AT_CMD_MAX_NUM) {  //当前有AT命令还没得到回应，先入队
                if (At_cmd_list != null) {
                    at_cmd_list_t cmdItem = new at_cmd_list_t(cmd, cmdStr);
                    At_cmd_list.offer(cmdItem);
                }
            }
            else
            {
                At_current_cmd.cmd = cmd;
                At_current_cmd.cmdStr = cmdStr;
                at_uart_send(cmd,cmdStr);
            }
        }

        return 0;//ret;
    }

    private void gprs_at_cmd_send_pending()
    {
        if((At_cmd_list.size() != 0)&&(At_current_cmd.cmd == at_cmd_e.AT_CMD_MAX_NUM))
        {
            at_cmd_list_t cmdItem = At_cmd_list.poll();
            if(cmdItem != null)
            {
                At_current_cmd.cmd = cmdItem.cmd;
                At_current_cmd.cmdStr = cmdItem.cmdStr;
                at_uart_send(cmdItem.cmd,cmdItem.cmdStr);
                if(cmdItem.cmd == at_cmd_e.AT_CMD_MAX_NUM)
                {
                    //这种可能是没有回复的，需要起一个消息接着往下发
                    if(mHandlerThread != null) {
                        uartDataHandler.removeMessages(SendUartData);
                        uartDataHandler.sendEmptyMessageDelayed(SendUartData, 1000);
                    }
                }
            }
        }
    }

    public String byteToHexString(byte[] data) {
        int pduLength = data.length;

        StringBuilder r = new StringBuilder();
        for(int j = 0;  j<pduLength; j++)
        {
            int b = data[j] &0xff;
            if(b < 0x10)
            {
                r.append('0');
            }
            //r.append(Integer.toHexString(b)).append(':');
            r.append(Integer.toHexString(b));
            if(j != (pduLength-1))
            {
                r.append(' ');
            }
        }
        //Log.d(TAG, r.toString());

        return r.toString();
    }
    private boolean at_uart_send(TempUtil.at_cmd_e cmd, String str)
    {
        Log.d(TAG, mSerialPortUtil.getPortPath()+" TX: "+str);
        byte[] txByte = null;
        if(mHandlerThread != null) {
            uartDataHandler.removeMessages(UartNoDataTimeOut);
            uartDataHandler.sendEmptyMessageDelayed(UartNoDataTimeOut, NoDataTimeOutInterval);
        }
        if(rfInited == false)
        {
            Log.e(TAG, "rfInited == false！！");
            return false;
        }
        try {
            txByte = str.getBytes();
            //Log.d(TAG, "txByte " + byteToHexString(txByte));
            mOutputStream.write(txByte);
            if(cmd == at_cmd_e.AT_CMD_MAX_NUM) {
                mOutputStream.write((byte)0x0d);
                mOutputStream.write((byte)0x0a);
            }
            Log.d(TAG, "finish");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void setReportOnOff(boolean isTx)
    {
        if(isTx == true) {
            String str = "AT+CVTempOn=1";
            at_cmd_send_to_list(at_cmd_e.AT_CMD_SET_TEMP_ONOFF, str);
        }
        else
        {
            String str = "AT+CVTempOn=0";
            at_cmd_send_to_list(at_cmd_e.AT_CMD_SET_TEMP_ONOFF, str);
        }
    }

    private void sendRFRawMessage(String msg)
    {
        at_cmd_send_to_list(at_cmd_e.AT_CMD_MAX_NUM, msg);
    }
}
