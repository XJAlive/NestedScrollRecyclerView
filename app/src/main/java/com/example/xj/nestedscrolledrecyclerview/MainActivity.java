package com.example.xj.nestedscrolledrecyclerview;

import android.app.Activity;
import android.os.Bundle;

import com.example.xj.widget.refresh.HomeSmartRefreshHeader;

public class MainActivity extends Activity {

    HomeSmartRefreshHeader mHomeSmartRefreshHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
