package com.example.hu.mediaplayerapk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.hu.mediaplayerapk.bean.WashingReportItem;

import java.util.List;


public class WashingReportManager {

    private static WashingReportManager INSTANCE;
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private WashingReportItemDao washingReportItemDao;

    public static synchronized WashingReportManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WashingReportManager(context);
        }
        return INSTANCE;
    }

    private void initGreenDao(Context context) {
        mHelper = new DaoMaster.DevOpenHelper(context, "washing.db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public WashingReportManager(Context context) {
        initGreenDao(context);
        washingReportItemDao = mDaoSession.getWashingReportItemDao();
    }

    /**
     * 会自动判定是插入还是替换
     */
    public void insertOrReplace(WashingReportItem washingReportItem) {

        washingReportItem.setTime((int) (System.currentTimeMillis() / 1000));
        try {
            WashingReportItem mOldResponseBean = washingReportItemDao.queryBuilder().where(WashingReportItemDao.Properties.Id.eq(washingReportItem.getId())).build().unique();//拿到之前的记录
            if (mOldResponseBean != null) {
                washingReportItem.setId(mOldResponseBean.getId());
            }
            washingReportItemDao.insertOrReplace(washingReportItem);
        } catch (Exception e) {
            washingReportItemDao.deleteAll();
            e.printStackTrace();
        }
    }

    private static final String TAG = "FriendDaoManager";

    /**
     * 查询所有数据
     */
    public List<WashingReportItem> searchAll() {
        List<WashingReportItem> searchHistories = washingReportItemDao.queryBuilder().list();
        return searchHistories;
    }

    public WashingReportItem searchById(int id) {
        return washingReportItemDao.queryBuilder().where(WashingReportItemDao.Properties.Id.eq(id)).build().unique();
    }


    public List<WashingReportItem> searchByFaceId(String faceID) {
        List<WashingReportItem> searchHistories = washingReportItemDao.queryBuilder()
                .where(WashingReportItemDao.Properties.FaceID.eq(faceID)).list();
        return searchHistories;
    }

    public List<WashingReportItem> searchByFaceIdAndDate(String faceID, int startTime ) {
        List<WashingReportItem> searchHistories = washingReportItemDao.queryBuilder()
                .where(WashingReportItemDao.Properties.FaceID.eq(faceID),
                        WashingReportItemDao.Properties.Time.ge(startTime),
                        WashingReportItemDao.Properties.Time.le(startTime+24*60*60)).list();
        return searchHistories;
    }
}
