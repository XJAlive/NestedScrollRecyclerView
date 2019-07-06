package com.example.xj.nestedrecyclerview.widget.refresh;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xj.nestedrecyclerview.R;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.internal.InternalAbstract;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.util.Random;


/**
 * 自定义下拉刷新进度条布局
 */
public class HomeSmartRefreshHeader extends InternalAbstract implements RefreshHeader {

    private TextView mRefreshText;
    private CircularProgress mProgressBar;
    View newDataView;//本次刷新
    private TextView tvTips;

    protected int mPaddingTop = 20;
    protected int mPaddingBottom = 20;

    private Context mContext;

    public HomeSmartRefreshHeader(Context context) {
        this(context, null, 0);
    }

    public HomeSmartRefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeSmartRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));

        if (newDataView == null) {
            newDataView = LayoutInflater.from(getContext()).inflate(R.layout.home_new_data, null, false);
            tvTips = newDataView.findViewById(R.id.tv_tips);
        }

        final DensityUtil density = new DensityUtil();

        mProgressBar = new CircularProgress(mContext);

        mRefreshText = new TextView(mContext);
        mRefreshText.setTextColor(0xff888888);
        mRefreshText.setTextSize(14);
        mRefreshText.setText("下拉即可刷新");

        mSpinnerStyle = SpinnerStyle.Translate;
        LinearLayout mCenterLayout = new LinearLayout(mContext);
        mCenterLayout.setGravity(Gravity.CENTER);
        mCenterLayout.setOrientation(LinearLayout.HORIZONTAL);

        final View thisView = this;
        final ViewGroup thisGroup = this;
        final ViewGroup centerLayout = mCenterLayout;

        centerLayout.setId(android.R.id.widget_frame);

        //进度条
        RelativeLayout.LayoutParams lpArrow = new RelativeLayout.LayoutParams(density.dip2px(20), density.dip2px(20));
        lpArrow.addRule(CENTER_VERTICAL);
        lpArrow.addRule(LEFT_OF, android.R.id.widget_frame);
        centerLayout.addView(mProgressBar, lpArrow);

        LinearLayout.LayoutParams lpHeaderText = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpHeaderText.setMargins(20, 0, 10, 0);
        mRefreshText.setPadding(10, 10, 10, 10);
        centerLayout.addView(mRefreshText, lpHeaderText);
        centerLayout.addView(newDataView);
        newDataView.setVisibility(View.GONE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        thisGroup.addView(centerLayout, layoutParams);

        if (thisView.getPaddingTop() == 0) {
            if (thisView.getPaddingBottom() == 0) {
                thisView.setPadding(thisView.getPaddingLeft(), mPaddingTop = density.dip2px(20), thisView.getPaddingRight(), mPaddingBottom = density.dip2px(20));
            } else {
                thisView.setPadding(thisView.getPaddingLeft(), mPaddingTop = density.dip2px(20), thisView.getPaddingRight(), mPaddingBottom = thisView.getPaddingBottom());
            }
        } else {
            if (thisView.getPaddingBottom() == 0) {
                thisView.setPadding(thisView.getPaddingLeft(), mPaddingTop = thisView.getPaddingTop(), thisView.getPaddingRight(), mPaddingBottom = density.dip2px(20));
            } else {
                mPaddingTop = thisView.getPaddingTop();
                mPaddingBottom = thisView.getPaddingBottom();
            }
        }
    }

//    @Override
//    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
//        mAnimationDrawable.start();
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final View thisView = this;
        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) {
            thisView.setPadding(thisView.getPaddingLeft(), 0, thisView.getPaddingRight(), 0);
        } else {
            thisView.setPadding(thisView.getPaddingLeft(), mPaddingTop, thisView.getPaddingRight(), mPaddingBottom);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Scale;
    }


    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }


    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {
        if (!mProgressBar.isRunning()) {
            mProgressBar.start();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressBar.isRunning()) {
            mProgressBar.stop();
        }
    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        if (success) {
            // mRefreshText.setText("刷新完成");
        } else {
            mRefreshText.setText("刷新失败");
        }
        return 2500;//延迟3000毫秒之后再弹回
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case None:
                mRefreshText.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                newDataView.setVisibility(View.GONE);
            case PullDownToRefresh:
                mRefreshText.setText("下拉即可刷新");
                break;
            case Refreshing:
                mRefreshText.setText("正在刷新");
                break;
            case ReleaseToRefresh:
                mRefreshText.setText("松开刷新");
                break;
            case RefreshFinish:
                //刷新完成
                mRefreshText.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                tvTips.setText(String.format("为您更新了%s篇新内容", new Random().nextInt(30)));
                newDataView.setVisibility(View.VISIBLE);
                break;
            case LoadFinish:
                break;
            //刷新完成
            default:
                break;
        }
    }
}
