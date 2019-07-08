package com.example.xj.nestedrecyclerview.module.home.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.xj.nestedrecyclerview.R;
import com.example.xj.nestedrecyclerview.utils.PublicMethod;

import java.util.List;

public class GridViewTopicAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> icons;
    private int width;

    public GridViewTopicAdapter(Context context) {
        this.mContext = context;

        //图片宽度
        width = (PublicMethod.getScreenWidth(context) - PublicMethod.dip2px(context, 12 + 18 * 2)) / 3;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_gridview_topic, null);
            viewHolder.iv_icon = convertView.findViewById(R.id.iv_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (width != 0) {
            float fHeigh = ((float) 170 / 228) * width;
            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(width, (int) fHeigh);
            viewHolder.iv_icon.setLayoutParams(lp1);
        }

        if (position % 3 == 0) {
            viewHolder.iv_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.food1));
        } else if (position % 3 == 1) {
            viewHolder.iv_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.food2));
        } else {
            viewHolder.iv_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.food3));
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView iv_icon;
    }

}
