package com.example.xj.nestedrecyclerview.utils;

import android.content.Context;

/**
 * Created by xiej on 2019/7/6
 */
public class PublicMethod {

    /**
     * dip to px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px to dip
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取去掉边距后的屏幕宽度
     *
     * @param appContext
     * @param edgeDimensResId 边距值需在dimens.xml中定义
     * @return context==null will return 0
     */
    public static int getScreenWidthExcuEdge(Context appContext, int edgeDimensResId) {
        if (appContext == null) {
            return 0;
        }

        int screenWidth = appContext.getResources().getDisplayMetrics().widthPixels;
        if (screenWidth > 0) {
            int edgeSize = appContext.getResources().getDimensionPixelSize(edgeDimensResId);
            screenWidth -= 2 * edgeSize;
        }
        return screenWidth;
    }

}
