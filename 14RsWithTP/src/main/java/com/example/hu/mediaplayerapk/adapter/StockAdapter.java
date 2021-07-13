package com.example.hu.mediaplayerapk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hu.mediaplayerapk.R;
import com.example.hu.mediaplayerapk.bean.StockBean;
import com.example.hu.mediaplayerapk.bean.WashingReportItem;
import com.example.hu.mediaplayerapk.widget.CustomizeScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 码农专栏
 * on 2020-06-04.
 */
public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<ViewHolder> recyclerViewHolder = new ArrayList<>();

    private int offestX;

    private OnTabScrollViewListener onTabScrollViewListener;

    private List<StockBean> stockBeans;

    private Context mContext;
    private Bitmap lady_pic, men_pic;
    public StockAdapter(Context mContext) {
        this.mContext = mContext;
        lady_pic = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.lady);
        men_pic = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gentleman);
    }

    public void setOnTabScrollViewListener(OnTabScrollViewListener onTabScrollViewListener) {
        this.onTabScrollViewListener = onTabScrollViewListener;
    }

    public void setStockBeans(List<StockBean> stockBeans) {
        this.stockBeans = stockBeans;
        notifyDataSetChanged();
    }

    public List<ViewHolder> getRecyclerViewHolder() {
        return recyclerViewHolder;
    }

    public int getOffestX() {
        return offestX;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_stock_content_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        StockBean item = stockBeans.get(position);
        holder.mStockName.setText(item.getStockName());
        if (item.getIsLadyOrMen() == 1) {
            holder.mStockImg.setImageBitmap(lady_pic);
        } else {
            holder.mStockImg.setImageBitmap(men_pic);
        }
        holder.mStockRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
        holder.mStockRecyclerView.setNestedScrollingEnabled(false);

        // TODO：文本RecyclerView中具体信息的RecyclerView（RecyclerView嵌套）
        StockItemAdapter stockItemAdapter = new StockItemAdapter(mContext);
        holder.mStockRecyclerView.setAdapter(stockItemAdapter);
        stockItemAdapter.setDetailBeans(item.getDetail());
        if (!recyclerViewHolder.contains(holder)) {
            recyclerViewHolder.add(holder);
        }

        /**
         * 第一步：水平滑动item时，遍历所有ViewHolder，使得整个列表的HorizontalScrollView同步滚动
         */
        holder.mStockScrollView.setViewListener(new CustomizeScrollView.OnScrollViewListener() {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt) {
                for (ViewHolder viewHolder : recyclerViewHolder) {
                    if (viewHolder != holder) {
                        viewHolder.mStockScrollView.scrollTo(l, 0);
                    }
                }
                /**
                 * 第二步：水平滑动item时，接口回调到Tab栏的HorizontalScrollView，使得Tab栏跟随item滚动实时更新
                 */
                if (onTabScrollViewListener != null) {
                    onTabScrollViewListener.scrollTo(l, t);
                    offestX = l;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockBeans.size() == 0 ? 0 : stockBeans.size();
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

    public interface OnTabScrollViewListener {
        void scrollTo(int l, int t);
    }
}
