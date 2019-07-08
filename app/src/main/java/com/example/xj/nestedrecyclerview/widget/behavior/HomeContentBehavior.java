package com.example.xj.nestedrecyclerview.widget.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.xj.nestedrecyclerview.R;

import java.util.List;


/**
 * 底部RecyclerView Behavior
 */
public class HomeContentBehavior extends HeaderScrollingViewBehavior {
    private static final String TAG = HomeContentBehavior.class.getName();

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

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        offsetChildAsNeeded(parent, child, dependency);
        return false;
    }

    private void offsetChildAsNeeded(CoordinatorLayout parent, View child, View dependency) {
        float dependencyTranslationY = dependency.getTranslationY();
        child.setTranslationY(dependencyTranslationY);

        float translationY = -dependencyTranslationY;
//        Log.i(TAG, "offsetChildAsNeeded: translationY=" + translationY + "  dependencyTranslationY=" + dependencyTranslationY);

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
            return Math.max(0, v.getMeasuredHeight());
        } else {
            return super.getScrollRange(v);
        }
    }

    private boolean isDependOn(View dependency) {
        return dependency != null && dependency.getId() == R.id.rl_home_header;
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
