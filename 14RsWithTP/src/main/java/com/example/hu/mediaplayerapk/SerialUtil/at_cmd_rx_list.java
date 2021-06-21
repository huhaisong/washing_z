package com.example.hu.mediaplayerapk.SerialUtil;


/**
 * Created by Administrator on 2020-08-08.
 */

public class at_cmd_rx_list {
    TempUtil.at_cmd_reply_e cmd;
    String cmdStr;

    public at_cmd_rx_list(TempUtil.at_cmd_reply_e cmd, String cmdStr)
    {
        this.cmd = cmd;
        this.cmdStr = cmdStr;
    }
}
