package com.example.hu.mediaplayerapk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.bean.StockBean;

import java.util.List;
import java.util.Random;

/**
 * Created by 码农专栏
 * on 2020-06-04.
 */
public class StockItemAdapter extends RecyclerView.Adapter<StockItemAdapter.ItemViewHolder> {

    private List<StockBean.Date> detailBeans;

    private Context mContext;
    OnItemClickListen onItemClickListener;

    public StockItemAdapter(Context mContext, OnItemClickListen onItemClickListen) {
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListen;
    }


    public void setDetailBeans(List<StockBean.Date> detailBeans) {
        this.detailBeans = detailBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_stock_content_detail, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        StockBean.Date item = detailBeans.get(position);
        holder.mTotalWashing.setText(item.getTotalWashing() + "");
        holder.mTotalInterrupt.setText(item.getTotalInterrupt() + "");
        holder.mTotalLongTime.setText(item.getTotalLongtime() + "");
        if (item.getTotalLongtime() != 0) {
            holder.mTotalLongTime.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_yellow_bg);
        } else {
            holder.mTotalLongTime.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_bg);
        }


        if (item.getTotalLongtime() != 0) {
            holder.mTotalWashing.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_light_blue_bg);
        } else {
            holder.mTotalWashing.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_bg);
        }

        if (item.getTotalInterrupt() != 0) {
            holder.mTotalInterrupt.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_red_bg);
        } else {
            holder.mTotalInterrupt.setBackgroundResource(R.drawable.shape_tv_stroke_no_corners_bg);
        }

        holder.mTotalWashing.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        holder.mTotalInterrupt.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        holder.mTotalLongTime.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return detailBeans.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView mTotalWashing;
        TextView mTotalInterrupt;
        TextView mTotalLongTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mTotalWashing = itemView.findViewById(R.id.tv_total_washing);
            mTotalInterrupt = itemView.findViewById(R.id.tv_total_interrupt);
            mTotalLongTime = itemView.findViewById(R.id.tv_total_long_time);
        }
    }


    public interface OnItemClickListen {

        void onItemClick(int position);
    }
}




