package com.example.hu.mediaplayerapk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.SerialUtil.TempUtil;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.config.Config;
import com.example.hu.mediaplayerapk.util.FileUtils;
import com.example.hu.mediaplayerapk.util.SPUtils;

import java.io.File;
import java.util.ArrayList;

public class washingReportAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Bitmap lady_pic, men_pic;
    private ArrayList<WashingReportItem> ReportList = null;
    private final String PIC_PATH = "/mnt/sdcard/pista/tearai/thumbnail/";
    private Context mContext;
    //参数初始化
    public washingReportAdapter(Context context, ArrayList<WashingReportItem> na) {
        ReportList = na;
        lady_pic = BitmapFactory.decodeResource(context.getResources(), R.drawable.lady);
        men_pic = BitmapFactory.decodeResource(context.getResources(), R.drawable.gentleman);
        //缩小图片
        //directory = small(directory, 0.16f);
        //file = small(file, 0.1f);
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return ReportList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return ReportList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (null == convertView) {


            convertView = inflater.inflate(R.layout.washingreport_item, null);
            holder = new ViewHolder();
            holder.washingEventText = (TextView) convertView.findViewById(R.id.washingEventValue);
            holder.moveAwayText = (TextView) convertView.findViewById(R.id.moveAwayValue);
            holder.averageTempText = (TextView) convertView.findViewById(R.id.averageTempValue);
            //holder.tempErrorText = (TextView) convertView.findViewById(R.id.errorCntValue);
            holder.curTempText = (TextView)convertView.findViewById(R.id.lastTempValue);

            holder.averageTempTitle = (TextView) convertView.findViewById(R.id.averageTempTitle);
            //holder.tempErrorText = (TextView) convertView.findViewById(R.id.errorCntValue);
            holder.curTempTitle = (TextView)convertView.findViewById(R.id.lastTempTitle);

            holder.faceIDText = (TextView)convertView.findViewById(R.id.faceIDValue);
            holder.image = (ImageView) convertView.findViewById(R.id.photoView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        WashingReportItem item = ReportList.get(position);
        if((item != null)&&(holder != null))
        {
            holder.faceIDText.setText(item.getFaceID());
            holder.washingEventText.setText(""+item.getWashingEventCnt());
            holder.moveAwayText.setText(""+item.getMoveAwayCnt());

            if(SPUtils.getInt(mContext, Config.CFGTempFunctionEn, Config.DefTempFunctionEn) > 0) {
                if(holder.averageTempText != null) {
                    holder.averageTempText.setText("" + item.getAverageTemp());
                }
                if (holder.tempErrorText != null) {
                    holder.tempErrorText.setText("" + item.getTempErrorCnt());
                }

                if (holder.curTempText != null) {
                    holder.curTempText.setText("" + item.getLastTemp());
                }
            }
            else
            {
                if(holder.averageTempText != null) {
                    holder.averageTempText.setVisibility(View.INVISIBLE);
                }
                if (holder.tempErrorText != null) {
                    holder.tempErrorText.setVisibility(View.INVISIBLE);
                }

                if (holder.curTempText != null) {
                    holder.curTempText.setVisibility(View.INVISIBLE);
                }

                if(holder.averageTempTitle != null) {
                    holder.averageTempTitle.setVisibility(View.INVISIBLE);
                }

                if (holder.curTempTitle != null) {
                    holder.curTempTitle.setVisibility(View.INVISIBLE);
                }
            }

            //display thumbnail
            {
                String picPath = PIC_PATH + item.getFaceID() + ".jpg";
                Uri picUri= Uri.parse(picPath);

                if(FileUtils.fileState(picPath) == true) {
                    holder.image.setImageURI(picUri);
                }
                else {
                    if (item.getIsLadyOrMen() == 1) {


                        holder.image.setImageBitmap(lady_pic);


                    } else {
                        holder.image.setImageBitmap(men_pic);
                    }
                }
            }
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView washingEventText = null;
        private TextView moveAwayText = null;
        private TextView averageTempText = null;
        private TextView tempErrorText = null;

        private TextView curTempText = null;
        private TextView faceIDText = null;
        private ImageView image;

        private TextView averageTempTitle = null;
        private TextView curTempTitle = null;
    }

    private Bitmap small(Bitmap map, float num) {
        Matrix matrix = new Matrix();
        matrix.postScale(num, num);
        return Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), matrix, true);
    }
}
