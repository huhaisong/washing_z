package com.example.hu.mediaplayerapk.SerialUtil;


import android.content.SharedPreferences;
import android.serialport.SerialPort;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Created by Administrator on 2020-08-07.
 *
 */

public class serialPortUtil {
    private String TAG ="serialPortUtil";
    private SerialPort mSerialPort = null;
    private String serialPortPath = null;
    public SerialPort getSerialPort(String InitPath)
            throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */

            String path = "/dev/ttyACM";
            int baudrate = 115200;
            serialPortPath = null;
            if(InitPath == null) {
                for (int i = 0; i < 10; i++) {
                    if (new File(path + i).exists() == true) {
                        serialPortPath = path + i;
                        break;
                    }
                }
            }
            else
            {
                if (new File(InitPath).exists() == true) {
                    serialPortPath = InitPath;
                }
            }

            if(serialPortPath == null)
            {
                Log.e(TAG, "Device not exist!");
                return null;
            }

            Log.d(TAG, serialPortPath);

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            //mSerialPort = new SerialPort(new File(path), baudrate, 0);

            SerialPort serialPort = SerialPort //
                    .newBuilder(serialPortPath, baudrate) // 串口地址地址，波特率
                    .parity(0) // 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
                    .dataBits(8) // 数据位,默认8；可选值为5~8
                    .stopBits(1) // 停止位，默认1；1:1位停止位；2:2位停止位
                    .build();

            mSerialPort = serialPort;
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public boolean SerialPortExist()
    {
        if(serialPortPath == null)
        {
            return false;
        }

        if(new File(serialPortPath).exists() == true)
        {
            return true;
        }
        return false;
    }

    public boolean hasSerialPortValid()
    {
        String path = "/dev/ttyACM";

        for(int i = 0; i < 10; i++)
        {
            if(new File(path+i).exists() == true)
            {
                return true;

            }
        }
        return false;
    }

    public String getPortPath()
    {
        return this.serialPortPath;
    }
}
