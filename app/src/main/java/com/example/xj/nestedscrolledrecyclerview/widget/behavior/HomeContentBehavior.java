package com.example.xj.nestedscrolledrecyclerview.widget.behavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.xj.nestedscrolledrecyclerview.R;

import java.util.List;
import java.util.Objects;


/**
 * 底部RecyclerView Behavior
 * 跟随顶部控件进行位移滚动
 */
public class HomeContentBehavior extends HeaderScrollingViewBehavior {
    private static final String TAG = "HomeContentBehavior";

    OnPagerStateListener mOnPagerStateListener;

    public HomeContentBehavior() {
    }

    public HomeContentBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return isDependOn(dependency);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        offsetChildAsNeeded(parent, child, dependency);
        return false;
    }

    @SuppressLint("NewApi")
    private void offsetChildAsNeeded(CoordinatorLayout parent, View child, View dependency) {
        float dependencyTranslationY = dependency.getTranslationY();
//        int translationY = (int) (-dependencyTranslationY / (getHeaderOffsetRange(dependency) * 1.0f) *
//                getScrollRange(dependency));
        float translationY = -dependencyTranslationY;
        Log.i(TAG, "offsetChildAsNeeded: translationY=" + translationY + "  dependencyTranslationY=" + dependencyTranslationY);

        //再进行一次判断
        CoordinatorLayout.LayoutParams headerParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        ((HomeHeaderBehavior) Objects.requireNonNull(headerParams.getBehavior())).refreshState(dependency);

        child.setTranslationY(0 - translationY);

        if (mOnPagerStateListener != null) {
            float ratio = translationY * 1.0f / child.getMeasuredHeight();
            mOnPagerStateListener.onViewTranslationChanged(0 - translationY, ratio);
        }
    }

    @Override
    protected View findFirstDependency(List<View> views) {
        for (int i = 0, z = views.size(); i < z; i++) {
            View view = views.get(i);
            if (isDependOn(view)) return view;
        }
        return null;
    }

    @Override
    protected int getScrollRange(View v) {
        if (isDependOn(v)) {
            return Math.max(0, v.getMeasuredHeight() - getFinalHeight());
        } else {
            return super.getScrollRange(v);
        }
    }

    private int getHeaderOffsetRange(View dependency) {
        return dependency.getMeasuredHeight();
    }

    private int getFinalHeight() {
        return 0;
    }

    private boolean isDependOn(View dependency) {
        return dependency != null && dependency.getId() == R.id.fl_home_header;
    }

    public OnPagerStateListener getOnPagerStateListener() {
        return mOnPagerStateListener;
    }

    public void setOnPagerStateListener(OnPagerStateListener onPagerStateListener) {
        mOnPagerStateListener = onPagerStateListener;
    }

    /**
     * callback for ContentPager's state
     */
    public interface OnPagerStateListener {

        /**
         * 在父布局中位置
         *
         * @param ratio
         */
        void onViewTranslationChanged(float translationY, float ratio);
    }

}
