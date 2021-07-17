package com.example.hu.mediaplayerapk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.widget.CustomizeScrollView;

import java.util.ArrayList;

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
    private ArrayList<WashingReportItem> datas = null;

    public WashingReportDetailAdapter(Context mContext, ArrayList<WashingReportItem> datas) {
        this.mContext = mContext;
        this.datas = datas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_stock_content_layout, parent, false);
        if (viewType == TYPE_HEAD) {
            return new HeadViewHolder(view);
        } else {
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }


    public class NormalViewHolder extends ViewHolder {

        public TextView mStockName;
        public ImageView mStockImg;
        public CustomizeScrollView mStockScrollView;
        public RecyclerView mStockRecyclerView;

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            mStockName = itemView.findViewById(R.id.faceIDValue);
            mStockImg = itemView.findViewById(R.id.photoView);
            mStockScrollView = itemView.findViewById(R.id.stockScrollView);
            mStockRecyclerView = itemView.findViewById(R.id.stockRecyclerView);
        }
    }


    public class HeadViewHolder extends ViewHolder {

        public TextView mStockName;
        public ImageView mStockImg;
        public CustomizeScrollView mStockScrollView;
        public RecyclerView mStockRecyclerView;

        public HeadViewHolder(@NonNull View itemView) {
            super(itemView);
            mStockName = itemView.findViewById(R.id.faceIDValue);
            mStockImg = itemView.findViewById(R.id.photoView);
            mStockScrollView = itemView.findViewById(R.id.stockScrollView);
            mStockRecyclerView = itemView.findViewById(R.id.stockRecyclerView);
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mStockName;
        public ImageView mStockImg;
        public CustomizeScrollView mStockScrollView;
        public RecyclerView mStockRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mStockName = itemView.findViewById(R.id.faceIDValue);
            mStockImg = itemView.findViewById(R.id.photoView);
            mStockScrollView = itemView.findViewById(R.id.stockScrollView);
            mStockRecyclerView = itemView.findViewById(R.id.stockRecyclerView);
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
