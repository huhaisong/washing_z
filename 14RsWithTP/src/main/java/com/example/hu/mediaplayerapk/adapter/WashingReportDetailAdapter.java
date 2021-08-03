package com.example.hu.mediaplayerapk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ===================================
 * Author: Eric
 * Date: 2021/7/16 15:40
 * Description:
 * ===================================
 */
public class WashingReportDetailAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private static final int TYPE_HEAD = 1;
    private static final int TYPE_CONTENT = 2;
    private List<WashingReportItem> datas = null;

    public WashingReportDetailAdapter(Context mContext, List<WashingReportItem> datas) {
        this.mContext = mContext;
        this.datas = datas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     /*   if (viewType == TYPE_HEAD) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_washing_report_detail_header, parent, false);
            return new ViewHolder(view);
        } else {*/
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_washing_report_detail_content, parent, false);
        return new NormalViewHolder(view);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(datas.get(position), position);
    }


    public class NormalViewHolder extends ViewHolder {

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void setData(WashingReportItem item, int position) {
            super.setData(item, position);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date((item.getTime() * 1000l));
            tv_date.setText(simpleDateFormat.format(date));
            if (item.getPlayNum() == 1) {
                tv_play_number.setText("初回動画");
            } else if (item.getPlayNum() == 2) {
                tv_play_number.setText("２回目以降動画");
            }
            if (item.getIsPlayInterrupt() == 1) {
                tv_play_result.setText("中断");
            } else {
                tv_play_result.setText("完了");
            }
            tv_temperature.setText(item.getLastTemp() + "℃");
            if (item.getIsLongInterval() == 1) {
                tv_long_time.setText("○");
            } else {
                tv_long_time.setText("");
            }
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_date;
        public TextView tv_play_number;
        public TextView tv_play_result;
        public TextView tv_temperature;
        public TextView tv_long_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_play_number = itemView.findViewById(R.id.tv_play_number);
            tv_play_result = itemView.findViewById(R.id.tv_play_result);
            tv_temperature = itemView.findViewById(R.id.tv_temperature);
            tv_long_time = itemView.findViewById(R.id.tv_long_time);
        }

        public void setData(WashingReportItem item, int position) {


        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        } else {

            return TYPE_CONTENT;
        }
    }
}
