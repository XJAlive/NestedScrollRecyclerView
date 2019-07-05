package com.example.xj.nestedscrolledrecyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeContentBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.behavior.HomeHeaderBehavior;
import com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.RecyclerViewAdapter;

public class MainActivity extends Activity {

    public RecyclerViewAdapter mHeaderRecyclerViewAdapter, mContentRecyclerViewAdapter;
    public RecyclerView mRecyclerViewHeader, mRecyclerViewContent;
    public FrameLayout flHomeHeader;
    public RelativeLayout rl_home_content;
    public HomeHeaderBehavior mHomeHeaderBehavior;
    public HomeContentBehavior mHomeContentBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}
