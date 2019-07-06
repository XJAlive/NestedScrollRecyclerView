package com.example.xj.nestedscrolledrecyclerview.module.home.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.xj.nestedscrolledrecyclerview.R;
import com.example.xj.nestedscrolledrecyclerview.utils.PublicMethod;

import java.util.List;

public class GridViewTopicAdapter extends BaseAdapter {

    private Context context;
    private List<String> icons;
    private int width;

    public GridViewTopicAdapter(Context context) {
        this.context = context;

        //图片宽度
        if (icons != null && !icons.isEmpty()) {
            width = (PublicMethod.getScreenWidthExcuEdge(context, 18) - PublicMethod.dip2px(context, 12)) / icons.size();
        }
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_topic, null);
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

        return convertView;
    }

    private class ViewHolder {
        ImageView iv_icon;
    }

}
