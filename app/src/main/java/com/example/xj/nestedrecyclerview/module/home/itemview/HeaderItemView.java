package com.example.xj.nestedrecyclerview.module.home.itemview;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xj.nestedrecyclerview.R;
import com.example.xj.nestedrecyclerview.widget.ScalableImageView;
import com.example.xj.nestedrecyclerview.widget.recyclerview.base.ItemViewDelegate;
import com.example.xj.nestedrecyclerview.widget.recyclerview.base.ViewHolder;
import com.example.xj.nestedrecyclerview.widget.recyclerview.bean.RecyclerViewBean;

public class HeaderItemView implements ItemViewDelegate<RecyclerViewBean> {

    private Context mContext;

    public HeaderItemView(Context context) {
        this.mContext = context;
    }

    protected void initView(ViewHolder holder) {
    }

    public void bindView(Object data, final int position, ViewHolder holder) {
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_block_home_header;
    }

    @Override
    public boolean isForViewType(RecyclerViewBean item, int position) {
        return item.getType() == RecyclerViewBean.TYPE_HEADER_ITEM;
    }

    @Override
    public void convert(ViewHolder holder, RecyclerViewBean homeRecycleBean, int position) {
        if (homeRecycleBean == null) {
            return;
        }
        initView(holder);
        bindView(homeRecycleBean, position, holder);
    }
}
