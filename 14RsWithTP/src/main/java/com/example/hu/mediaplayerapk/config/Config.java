package com.example.hu.mediaplayerapk.config;

/**
 * Created by Administrator on 2016/11/23.
 */

public class Config {

public static int MALE = 1;
    public static int FEMALE = 2;
    public static int UNKNOWN_AGE = -1;
    public static int ALIVE = 1;
    public static int NOT_ALIVE = 0;

    //人感
    public static final String CHECK_FACE_STATE = "check_face_state";
    //eco
    public static final String ECO_MODE_STATE = "eco_mode_state";

    //保存照片
    public static final String SAVE_IMAGE_STATE = "save_image_state";  // 值为0则为关闭状态，否则为打开状态

    public static final String BEACON_MODE_STATE = "eco_mode_state";  //  -1 代表开启，1代表关闭

    public static final String IMAGE_DIRECTION = "image_direction";
    public static final String IMAGE_TIME = "image_time";
    public static final String IMAGE_BGM_IMPACTV = "image_bgm_impactv";
    public static final String IMAGE_BGM_EVENT = "image_bgm_event";

    //图片动画方向
    public static final int IMAGE_DIRECTION_NORMAL = 0;
    public static final int IMAGE_DIRECTION_RANDOM = 1;
    public static final int IMAGE_DIRECTION_UPTODOWN = 2;
    public static final int IMAGE_DIRECTION_LEFTTORIGHT = 3;
    public static final int IMAGE_DIRECTION_HORIZONTALCROSS = 4;

    //背景音乐开启状态
    public static final int IMAGE_BGM_ON = 0;
    public static final int IMAGE_BGM_OFF = 1;

    //播放模式
    public static final int PLAY_BACK_MODE_ONE_FILE = 0;
    public static final int PLAY_BACK_MODE_ALL_FILE = 1;
    public static final int PLAY_BACK_MODE_MIX_PROGRAM = 2;
    public static final int PLAY_BACK_MODE_SCHEDULE = 3;

    public static final String INTERNAL_IMPACTV_PLAY_BACK_MODE_ONE_FILE_TITLE = "internal_impactv_play_back_mode_one_file_title";
    public static final String EXTERNAL_IMPACTV_PLAY_BACK_MODE_ONE_FILE_TITLE = "external_impactv_play_back_mode_one_file_title";
    public static final String INTERNAL_EVENT_PLAY_BACK_MODE_ONE_FILE_TITLE = "internal_event_play_back_mode_one_file_title";
    public static final String EXTERNAL_EVENT_PLAY_BACK_MODE_ONE_FILE_TITLE = "external_event_play_back_mode_one_file_title";
    public static final String INTERNAL_PLAY_BACK_MODE_IMPACTV = "internal_play_back_mode_impactv";
    public static final String EXTERNAL_PLAY_BACK_MODE_IMPACTV = "external_play_back_mode_impactv";
    public static final String INTERNAL_PLAY_BACK_MODE_EVENT = "internal_play_back_mode_event";
    public static final String EXTERNAL_PLAY_BACK_MODE_EVENT = "external_play_back_mode_event";

    public static final String WORK_TIMER_FILE_PATH = "worktimer.txt";
    public static final String HOLIDAY_FILE_PATH = "holiday.txt";
    public static final String FACEID_STORE_FILE_PATH = "washing_stored_infor.ini";
    public static final String FACEID_STORE_BAK_FILE_PATH = "washing_stored_infor.bak";
    public static final String DISPLAY_RATIO = "display_ratio";
    public static final String LANGUAGE = "language";

    public static final String RESET_HOUR = "reset_hour";
    public static final String RESET_ON = "reset_on";

    //文件名称
    public static final String IMPACTV_MIX_PROGRAM_FILE_LIST_FILE_NAME = "impactv_mix_program_list.txt";
    public static final String EVENT_MIX_PROGRAM_FILE_LIST_FILE_NAME = "event_mix_program_list.txt";
    public static final String IMPACTV_BGM_FILE_LIST_FILE_NAME = "impactv_bgm_list.txt";
    public static final String EVENT_BGM_FILE_LIST_FILE_NAME = "event_bgm_list.txt";
    public static final String SCHEDULE_FILE_NAME = "ImpacTV.txt";
    public static final String BEACON_DEVICE_FILE_NAME = "beaconList.csv";
    public static final String BEACON_SCHEDULE_FILE_NAME = "beaconSchedule.txt";
    public static final String USB_STORAGE_IMPACTTV_FILE_NAME_INVARIANT = "impacttv14";
    public static final String USB_STORAGE_IMPACTV_FILE_NAME_INVARIANT = "impactv14";
    public static final String USB_STORAGE_EVENT_FILE_NAME_INVARIANT = "event14";
    public static final String USB_STORAGE_BEACON_EVENT_FILE_NAME_INVARIANT = "beacon14";
    public static final String USB_STORAGE_SYSTEM_FILE_NAME_INVARIANT = "system14";
    public static final String USB_STORAGE_WASHING_FILE_NAME_INVARIANT = "washing14";
    public static final String USB_STORAGE_WARNING_FILE_NAME_INVARIANT = "warning14";
    public static String USB_STORAGE_IMPACTTV_FILE_NAME = USB_STORAGE_IMPACTTV_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_IMPACTV_FILE_NAME = USB_STORAGE_IMPACTV_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_EVENT_FILE_NAME = USB_STORAGE_EVENT_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_SYSTEM_FILE_NAME = USB_STORAGE_SYSTEM_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_BEACON_EVENT_FILE_NAME = USB_STORAGE_BEACON_EVENT_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_WASHING_FILE_NAME = USB_STORAGE_WASHING_FILE_NAME_INVARIANT;
    public static String USB_STORAGE_WARNING_FILE_NAME = USB_STORAGE_WARNING_FILE_NAME_INVARIANT;
    public static final String USB_STORAGE_PATH = "/mnt/usb_storage/USB_DISK0";
    public static String USB_STORAGE_ROOT_PATH = USB_STORAGE_PATH;
    public static final String LOGFolder = "ITVLog";
    public static final String PlayLogName = "PlayLog";
    public static final String SysLogName = "SysLog";
    public static final String FaceLogName = "FaceLog";
    public static final String WashingRawFolderName = "Raw";

    //文件路径
    public static final String EXTERNAL_FILE_PATH = "/mnt/external_sd";
    public static final String INTERNAL_FILE_PATH = "/mnt/internal_sd";
    public static String EXTERNAL_FILE_ROOT_PATH = EXTERNAL_FILE_PATH;
    public static String INTERNAL_FILE_ROOT_PATH = INTERNAL_FILE_PATH;
    public static final String IMPACTV_FILE_NAME = "impactv";
    public static final String IMPACTTV_FILE_NAME = "impacttv";
    public static final String WASHING_FILE_NAME = "WASHING";
    public static final String WARNING_FILE_NAME = "WARNING";
    public static final String BEACON_FILE_NAME = "Beacons";
    public static final String EVENT_FILE_NAME = "Event";
    public static final String SYSTEM_FILE_NAME = "System";

    public static final String APK_PACKAGE_NAME = "com.example.hu.mediaplayerapk";

    //蓝牙传入
    public static final int BEACON_TAG_NO_PERSION = 4;  //4代表没人
    public static final int BEACON_TAG_PERSION = 0;  //0代表有人
    public static final int BEACON_TAG_PERSION_REFRESH = 1;  //

    public static final String PICKTURE_OK_FOLDER = "OUTPUT_OK";
    public static final String PICKTURE_NG_FOLDER = "OUTPUT_NG";
    public static final String PICKTURE_TEMP_FOLDER = "OUTPUT_TEMP";

    public static final String CFGHasCopyAssetFile = "CFGHasCopyAssetFile";

    public static final String CFGFaceShortVIDEOTime = "CFGFaceShortVIDEOTime";
    public static final long DefFaceShortVideoTime = (long)3.0*60*60*1000L;  //unit:milliseconds
    public static final long MinFaceShortVideoTime = (long)(0.1*60*60*1000L);  //unit:milliseconds

    public static final String CFGFaceResumeTime = "CFGFaceResumeTime";
    public static final long DefFaceResumeTime = (long)(2*60*1000L);   //断点续播的时间
    public static final long MinFaceResumeTime = (long)(1*6*1000L);

    public static final String CFGFaceNewEventTime = "CFGFaceNewEventTime";
    public static final long DefFaceNewEventTime = (long)(1*1000L);  //至少1秒表示稳定了才算有效
    public static final long MinFaceNewEventTime = (long)(500L);  //

    public static final String CFGFaceDisappearEventTime = "CFGFaceDisappearEventTime";
    public static final long DefFaceDisappearEventTime = (long)(1500L);  //至少1秒表示才算离开
    public static final long MinFaceDisappearEventTime = (long)(500L);  //

    public static final String CFGWashingZEmailAddr = "CFGWashingZEmailAddr";
    public static final String DefWashingZEmailRXAddr = "test@null.com";

    public static final String CFGErrorTempCFG ="CFGErrorTempCFG";
    public static final float DefErrorTempValue = (float)37.4;

    public static final String CFGTempFunctionEn = "CFGTempFunctionEn";
    public static final int DefTempFunctionEn = 0;

    public static final int DefMaxFaceIDNum = 1000;  //默认最多1000个face id


    public static final String IS_OPEN_ALARM_NOTICE = "IS_OPEN_ALARM_NOTICE";
    public static final String ALARM_NOTICE_START_TIME_HOUR = "ALARM_NOTICE_START_TIME_HOUR";
    public static final String ALARM_NOTICE_START_TIME_MINUTE = "ALARM_NOTICE_START_TIME_MINUTE";
    public static final String ALARM_NOTICE_END_TIME_HOUR = "ALARM_NOTICE_END_TIME_HOUR";
    public static final String ALARM_NOTICE_END_TIME_MINUTE = "ALARM_NOTICE_END_TIME_MINUTE";
    public static final String ALARM_NOTICE_INTERVAL = "ALARM_NOTICE_INTERVAL";  //unit: hour
    public static final String ALARM_NOTICE_VALID_TIME = "ALARM_NOTICE_VALID_TIME";


	//提前结束判断时间算完成
    public static final String CFGInterrupptingFinishEN = "CFGInterrupptingFinishEN";
    public static final int DefInterruptingFinishEN = 1;

    public static final String CFGLongWashingFinishTime = "CFGLongWashingFinishTime";
    public static final int DefLongWashingFinishTime = 66;  //播完到66秒即表示洗手完成，不用拷贝到NG
    public static final int MinLongWashingFinishTime = 1;
    public static final int MaxLongWashingFinishTime = 100;

    public static final String CFGShortWashingFinishTime = "CFGShortWashingFinishTime";
    public static final int DefShortWashingFinishTime = 31;  //播完到31秒即表示洗手完成，不用拷贝到NG
    public static final int MinShortWashingFinishTime = 1;
    public static final int MaxShortWashingFinishTime = 100;
}
