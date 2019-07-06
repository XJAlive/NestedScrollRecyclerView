package com.example.xj.nestedscrolledrecyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.example.xj.nestedscrolledrecyclerview.module.home.itemview.BottomItemView;
import com.example.xj.nestedscrolledrecyclerview.module.home.itemview.TopItemView;
import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeContentBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeHeaderBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.RecyclerViewAdapter;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.adapter.MultiItemTypeAdapter;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.bean.RecyclerViewBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecyclerView mRecyclerViewHeader, mRecyclerViewContent;
    private RelativeLayout rl_home_content;
    private SmartRefreshLayout srlHeader, srlContent;

    private HomeHeaderBehavior mHomeHeaderBehavior;
    private HomeContentBehavior mHomeContentBehavior;
    private RecyclerViewAdapter mHeaderRecyclerViewAdapter, mContentRecyclerViewAdapter;

    private List<RecyclerViewBean> headerList, contentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        mRecyclerViewHeader = findViewById(R.id.recycler_home_header);
        mRecyclerViewContent = findViewById(R.id.recycler_home_content);
        srlHeader = findViewById(R.id.srl_home_header);
        srlContent = findViewById(R.id.srl_home_content);

        headerList = new ArrayList<>();
        contentList = new ArrayList<>();

        MultiItemTypeAdapter headerAdapter = new MultiItemTypeAdapter(this, headerList);
        headerAdapter.addItemViewDelegate(new TopItemView(this));
        mHeaderRecyclerViewAdapter = new RecyclerViewAdapter(headerAdapter);
        mRecyclerViewHeader.setAdapter(mHeaderRecyclerViewAdapter);

        MultiItemTypeAdapter contentAdapter = new MultiItemTypeAdapter(this, contentList);
        contentAdapter.addItemViewDelegate(new BottomItemView(this));
        mContentRecyclerViewAdapter = new RecyclerViewAdapter(contentAdapter);

        mRecyclerViewContent.setAdapter(mContentRecyclerViewAdapter);
    }

    private void initData() {
        for(int i = 0; i < 10; i++){
            RecyclerViewBean recyclerViewBean = new RecyclerViewBean();
            recyclerViewBean.setType(RecyclerViewBean.TYPE_TOP_ITEM);
        }
    }

}
