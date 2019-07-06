package com.example.xj.nestedrecyclerview.module.home.itemview;

import android.content.Context;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xj.nestedrecyclerview.R;
import com.example.xj.nestedrecyclerview.module.home.adapter.GridViewTopicAdapter;
import com.example.xj.nestedrecyclerview.widget.ScalableImageView;
import com.example.xj.nestedrecyclerview.widget.recyclerview.base.ItemViewDelegate;
import com.example.xj.nestedrecyclerview.widget.recyclerview.base.ViewHolder;
import com.example.xj.nestedrecyclerview.widget.recyclerview.bean.RecyclerViewBean;

public class BottomItemView implements ItemViewDelegate<RecyclerViewBean> {

    private Context mContext;
    private GridView gbody;
    private ScalableImageView iv_avatar2;
    private RelativeLayout container;
    private TextView tvTitle, tvContent;
    private TextView tvFrom;
    private ImageView home_topic_close;

    public BottomItemView(Context context) {
        this.mContext = context;
    }

    protected void initView(ViewHolder holder) {
        container = holder.getView(R.id.rl_container);

        tvTitle = holder.getView(R.id.tv_home_topic_title);
        tvContent = holder.getView(R.id.tv_home_topic_content);
        tvFrom = holder.getView(R.id.tv_from);
        gbody = holder.getView(R.id.gbody);
        iv_avatar2 = holder.getView(R.id.iv_home_topic_avatar);
        home_topic_close = holder.getView(R.id.home_topic_close);
    }

    public void bindView(Object data, final int position, ViewHolder holder) {
        RecyclerViewBean bean = (RecyclerViewBean) data;
        if (bean == null) {
            return;
        }

        tvTitle.setText(String.format("这是一段相对较长的测试内容 ： %d", position));
        iv_avatar2.setRoundConner(72);
        gbody.setNumColumns(3);
        gbody.setAdapter(new GridViewTopicAdapter(mContext));
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_block_home_bottom;
    }

    @Override
    public boolean isForViewType(RecyclerViewBean item, int position) {
        return item.getType() == RecyclerViewBean.TYPE_BOTTOM_ITEM;
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
