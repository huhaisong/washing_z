package com.example.hu.mediaplayerapk.SerialUtil;


/**
 * Created by Administrator on 2020-08-08.
 */

public class at_cmd_list_t {
    TempUtil.at_cmd_e cmd;
    String cmdStr;
    public at_cmd_list_t(TempUtil.at_cmd_e cmd, String cmdStr)
    {
        this.cmd = cmd;
        this.cmdStr = cmdStr;
    }
    public void setCmd(TempUtil.at_cmd_e scmd)
    {
        cmd = scmd;
    }
    public void setCmdStr(String str)
    {
        cmdStr = str;
    }

}
