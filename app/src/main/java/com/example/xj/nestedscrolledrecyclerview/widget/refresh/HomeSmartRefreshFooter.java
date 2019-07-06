package com.example.xj.nestedscrolledrecyclerview.widget.refresh;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xj.nestedscrolledrecyclerview.R;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;


public class HomeSmartRefreshFooter extends RelativeLayout implements RefreshFooter {

    private ConstraintLayout constraintLayout;
    private CircularProgress mCircularProgress;
    private TextView mLoadMoreText;


    public HomeSmartRefreshFooter(Context context) {
        this(context, null, 0);
    }

    public HomeSmartRefreshFooter(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public HomeSmartRefreshFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_smart_refresh_footer, this, true);
        constraintLayout = findViewById(R.id.footer_load_more_layout);
        mCircularProgress = (CircularProgress) findViewById(R.id.footer_load_more_view);
        mLoadMoreText = (TextView) findViewById(R.id.footer_load_more_title);
    }

    public void hideLoadMoreText() {
        mLoadMoreText.setVisibility(GONE);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(@ColorInt int... colors) {

    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {

    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        if (success) {
            // mRefreshText.setText("刷新完成");
        } else {
            mLoadMoreText.setText("加载失败");
        }
        return 500;//延迟500毫秒之后再弹回
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                mLoadMoreText.setText("上拉加载更多");
                break;
            case Loading:
                mLoadMoreText.setText("正在加载...");
                break;
            case ReleaseToLoad:
                mLoadMoreText.setText("松开加载更多");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean setNoMoreData(boolean noMoreData) {
        return false;
    }

}
