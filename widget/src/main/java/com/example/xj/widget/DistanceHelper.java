package com.example.xj.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

/**
 * Created by xiej on 2019/4/2
 */
public class DistanceHelper {

    private Context mContext;

    private static final float INVALID_DISTANCE = 1f;
    private OverScroller mScroller;

    @Nullable
    private OrientationHelper mVerticalHelper;

    public DistanceHelper(Context context) {
        mContext = context;
    }

    @NonNull
    public OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    /**
     * 计算当前velocity能够滑动的距离
     *
     * @param velocityX
     * @param velocityY
     * @return
     */
    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        mScroller = new OverScroller(mContext, new DecelerateInterpolator());

        int[] outDist = new int[2];
        mScroller.fling(0, 0, velocityX, velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        outDist[0] = mScroller.getFinalX();
        outDist[1] = mScroller.getFinalY();
        return outDist;
    }

    /**
     * 通过当前屏幕Item高度估算recyclerView子Item平均高度
     *
     * @param layoutManager
     * @param helper
     * @return
     */
    public float computeDistancePerChild(RecyclerView.LayoutManager layoutManager,
                                         OrientationHelper helper) {
        View minPosView = null;
        View maxPosView = null;
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return INVALID_DISTANCE;
        }

        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            final int pos = layoutManager.getPosition(child);
            if (pos == RecyclerView.NO_POSITION) {
                continue;
            }
            if (pos < minPos) {
                minPos = pos;
                minPosView = child;
            }
            if (pos > maxPos) {
                maxPos = pos;
                maxPosView = child;
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE;
        }
        int start = Math.min(helper.getDecoratedStart(minPosView),
                helper.getDecoratedStart(maxPosView));
        int end = Math.max(helper.getDecoratedEnd(minPosView),
                helper.getDecoratedEnd(maxPosView));
        int distance = end - start;
        if (distance == 0) {
            return INVALID_DISTANCE;
        }
        return 1f * distance / ((maxPos - minPos) + 1);
    }

}
