package com.example.xj.nestedscrolledrecyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.xj.nestedscrolledrecyclerview.module.home.bean.HomeTopBean;
import com.example.xj.nestedscrolledrecyclerview.module.home.itemview.BottomItemView;
import com.example.xj.nestedscrolledrecyclerview.module.home.itemview.TopItemView;
import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeContentBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeHeaderBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.RecyclerViewAdapter;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.adapter.MultiItemTypeAdapter;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.bean.RecyclerViewBean;
import com.example.xj.nestedscrolledrecyclerview.widget.refresh.HomeSmartRefreshFooter;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

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

        MaterialHeader materialHeader = new MaterialHeader(this);
        materialHeader.setColorSchemeColors(Color.parseColor("#ff98bf"));
        srlHeader.setRefreshHeader(materialHeader);
        srlHeader.setRefreshFooter(new HomeSmartRefreshFooter(this));

        srlHeader.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData();
            }
        });

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
        for (int i = 0; i < 10; i++) {
            RecyclerViewBean recyclerViewBean = new RecyclerViewBean();
            recyclerViewBean.setType(RecyclerViewBean.TYPE_TOP_ITEM);
            recyclerViewBean.setData(new HomeTopBean());
            headerList.add(recyclerViewBean);
        }

        for (int i = 0; i < 30; i++) {
            RecyclerViewBean recyclerViewBean = new RecyclerViewBean();
            recyclerViewBean.setType(RecyclerViewBean.TYPE_BOTTOM_ITEM);
            recyclerViewBean.setData(new HomeTopBean());
            contentList.add(recyclerViewBean);
        }

        mHeaderRecyclerViewAdapter.notifyDataSetChanged();
        mContentRecyclerViewAdapter.notifyDataSetChanged();
    }

    @SuppressLint("CheckResult")
    private void requestData() {
        Observable.timer(3, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Toast.makeText(MainActivity.this, "头部数据刷新完成", Toast.LENGTH_SHORT).show();
                srlHeader.finishRefresh();
            }
        });
    }

}
