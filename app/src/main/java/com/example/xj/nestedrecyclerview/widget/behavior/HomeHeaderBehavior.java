package com.example.xj.nestedrecyclerview.widget.behavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;

/**
 * 顶部RecyclerView Behavior
 */
public class HomeHeaderBehavior extends ViewOffsetBehavior {
    private static final String TAG = HomeHeaderBehavior.class.getName();
    public static final int STATE_OPENED = 0;//默认为打开状态
    public static final int STATE_CLOSED = 1;
    public static final int DURATION_SHORT = 300;
    public static final int DURATION_LONG = 600;

    private Context mContext;

    private int mCurState = STATE_OPENED;
    private OnPagerStateListener mPagerStateListener;

    private OverScroller mOverScroller;
    private DistanceHelper mDistanceHelper;

    private WeakReference<CoordinatorLayout> mParent;
    private WeakReference<View> mChild;

    private boolean isUp;//手势方向
    private int measuredHeight;

    private VelocityTracker mVelocityTracker;
    private int mScrollPointerId = -1;

    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;

    public void setPagerStateListener(OnPagerStateListener pagerStateListener) {
        mPagerStateListener = pagerStateListener;
    }

    public HomeHeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mOverScroller = new OverScroller(context, new DecelerateInterpolator());
        mDistanceHelper = new DistanceHelper(context);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void layoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        super.layoutChild(parent, child, layoutDirection);
        mParent = new WeakReference<>(parent);
        mChild = new WeakReference<>(child);
    }

    /**
     * 是否接受嵌套滚动
     *
     * @param coordinatorLayout
     * @param child
     * @param directTargetChild
     * @param target
     * @param nestedScrollAxes
     * @param type
     * @return true 父布局(CoordinatorLayout)处理滑动事件 false Behavior独立处理滑动事件
     */
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View
            directTargetChild, View target, int nestedScrollAxes, int type) {
        //拦截垂直方向上的滚动事件且当前状态是打开的
        boolean accepted = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && !isClosed(child);
        Log.d(TAG, "--------->onStartNestedScroll,是否由父布局共同处理滑动:" + accepted);
        return accepted;
    }

    /**
     * 结束嵌套滚动
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param type
     */
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        Log.d(TAG, "--------->onStopNestedScroll");
        mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
        float velocityX = mVelocityTracker.getXVelocity(mScrollPointerId);
        float velocityY = mVelocityTracker.getYVelocity(mScrollPointerId);
        mVelocityTracker.clear();

        //根据需要做模拟惯性滚动
        dispathFling(coordinatorLayout, child, velocityX, velocityY);
    }

    /**
     * 嵌套滚动前调用
     * Behavior通过consumed告诉coordinatorLayout自己消费了多少距离
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dx                X轴总计滑动距离 = consumed[0] + dxUnconsumed
     * @param dy                Y轴总计滑动距离
     * @param consumed          consumed[]表示当前Behavior所在控件实际消费的滑动距离,0：X轴 1：Y轴
     */
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        //dy>0 scroll up;dy<0,scroll down
        isUp = dy > 0;

        measuredHeight = child.getMeasuredHeight();//控件高度
        Log.i(TAG, "--------->onNestedPreScroll: dy=" + dy);

        float lastTranslationY = child.getTranslationY();//滑动前偏移

        if (isClosed(child)) {
            return;
        }

        //判断滑动方向
        if (isUp) {
            //滚动到了RecyclerView底部,由父控件统一处理滑动,进行位置偏移,即交界点从屏幕内移出过程
            if (isRecyclerViewBottom(child)) {
                if (lastTranslationY - dy <= 0 - measuredHeight) {
                    //考虑顶部边界
                    child.setTranslationY(0 - measuredHeight);
                    //实际消费距离为当前位置到顶部的距离
                    consumed[1] = (int) (lastTranslationY + measuredHeight);
                    changeState(STATE_CLOSED);
                } else {
                    child.setTranslationY(lastTranslationY - dy);
                    onTranslationFinished(child);
                    consumed[1] = dy;
                }
            }
        } else {
            //非打开状态都要进行联滚
            if (!isOpen(child)) {
                if (lastTranslationY - dy >= 0) {
                    //考虑底部边界
                    child.setTranslationY(0);
                    changeState(STATE_OPENED);
                    consumed[1] = (int) lastTranslationY;
                } else {
                    child.setTranslationY(lastTranslationY - dy);
                    onTranslationFinished(child);
                    consumed[1] = dy;
                }
            }
        }//else

    }

    /**
     * 嵌套滚动后调用
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dxConsumed
     * @param dyConsumed        Y轴已消耗距离
     * @param dxUnconsumed
     * @param dyUnconsumed      Y轴未消耗距离
     */
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (isClosed(child)) {
            return;
        }

        if (isUp) {
            //上滑且滑动到底部，需要偏移,即交界点从屏幕最下方开始往上移动过程
            if (isRecyclerViewBottom(child)) {
                child.setTranslationY(child.getTranslationY() - dyUnconsumed);
                onTranslationFinished(child);
            }
        } else {
            if (!isOpen(child)) {
                child.setTranslationY(child.getTranslationY() + dyUnconsumed);
                onTranslationFinished(child);
            }
        }
    }

    /**
     * 惯性滚动前调用
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param velocityX
     * @param velocityY
     * @return true Behavior处理惯性滚动 false 由父布局处理
     */
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        //这两种状态由子类滚动
        boolean coumsed = isOpen(child) || isClosed(child);
//        Log.i(TAG, "onNestedPreFling: coumsed=" + coumsed);
        return !coumsed;
    }

    /**
     * 是否处理了惯性滚动
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param velocityX
     * @param velocityY
     */
    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View child, View target,
                                 float velocityX, float velocityY, boolean consumed) {
        Log.i(TAG, "onNestedFling: velocityY=" + velocityY);
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    /**
     * 是否滚动到底部
     *
     * @param child
     * @return
     */
    private boolean isRecyclerViewBottom(View child) {
        boolean isBottom = false;
        RecyclerView recyclerView = findChildTypeOfRecyclerView(child);
        if (recyclerView != null) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            isBottom = lastPosition == recyclerView.getAdapter().getItemCount() - 1;
        }

        return isBottom;
    }


    /**
     * 关闭状态
     *
     * @param child
     * @return
     */
    private boolean isClosed(View child) {
        boolean isClosed = child.getTranslationY() == 0 - child.getMeasuredHeight();
//        Log.w(TAG, "isClosed ,child.getMeasuredHeight()=" + child.getMeasuredHeight());
        //第三方登录会导致更改为close,因为child.getMeasuredHeight()=0
        if (isClosed && mCurState != STATE_CLOSED && child.getMeasuredHeight() != 0) {
            changeState(STATE_CLOSED);
        }

        return isClosed;
    }

    private boolean isOpen(View child) {
        boolean isOpen = child.getTranslationY() == 0;
        return isOpen;
    }

    public boolean isClosed() {
        return mCurState == STATE_CLOSED;
    }

    private void changeState(int newState) {
//        Log.i("xj", "--->修改状态,0打开，1关闭，newState:" + newState + "  mCurState:" + mCurState);

        if (mCurState != newState) {
            mCurState = newState;
            if (mCurState == STATE_OPENED) {
                if (mPagerStateListener != null) {
                    mPagerStateListener.onPagerOpened();
                }
            } else {
                if (mPagerStateListener != null) {
                    mPagerStateListener.onPagerClosed();
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, final View child, MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = ev.getPointerId(0);//第一个点
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    int hDeltaJump;//以抬手速率估算滚动总Item个数
    int needScrollChildCount = 0;//滚动到底部之后还需要下面RecyclerView滚动位置
    float distancePerChild; //屏幕内Item估算高度

    /**
     * 模拟惯性滚动
     *
     * @param parent
     * @param child
     * @param velocityX
     * @param velocityY
     */
    private void dispathFling(CoordinatorLayout parent, final View child, final float velocityX, final float velocityY) {
//        Log.i("xj", "进入dispathFling,velocityX= " + velocityX + " velocityY=" + velocityY);

        if (Math.abs(velocityY) < mMinFlingVelocity) {
            return;
        }

        RecyclerView recyclerView = findChildTypeOfRecyclerView(child);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isUp || needScrollChildCount <= 0) {
                        //顶部RecyclerView能够消耗当前惯性滚动
                        return;
                    }
                    if (Math.abs(needScrollChildCount * distancePerChild) < measuredHeight) {
                        //需要偏移的量小于屏幕高度，只做偏移
                        startFling(0, (int) (velocityY / 8));
                    } else {
                        //上面recyclerview+偏移+下面recyclerview滚动
                        startFling(0, (int) (velocityY * Math.abs((float) needScrollChildCount / hDeltaJump)));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastVisiableItem = linearLayoutManager.findLastVisibleItemPosition();
        hDeltaJump = estimateNextPositionDiffForFling(linearLayoutManager, mDistanceHelper.getVerticalHelper(linearLayoutManager),
                (int) velocityX, (int) velocityY);//向下为负

        hDeltaJump = Math.abs(hDeltaJump);
        if (isUp) {
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                //零界点在屏幕内时的滚动
                startFling(0, (int) velocityY / 8);
            } else {
                //滚动到底部之后还需要下面RecyclerView滚动位置
                needScrollChildCount = lastVisiableItem + hDeltaJump - recyclerView.getAdapter().getItemCount();
                distancePerChild = mDistanceHelper.computeDistancePerChild(linearLayoutManager, mDistanceHelper.getVerticalHelper(linearLayoutManager));
            }
        } else {
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                //零界点在屏幕内时的滚动
                startFling(0, (int) velocityY / 8);
            }
        }

        Log.i("xj", "lastVisiableItem=" + lastVisiableItem + " 需要滚动hDeltaJump=" + hDeltaJump + " 下个recyclerview需要滚动: " + needScrollChildCount);
    }

    /**
     * 计算此速率需要滚动的position个数
     *
     * @param layoutManager
     * @param helper
     * @param velocityX
     * @param velocityY
     * @return
     */
    private int estimateNextPositionDiffForFling(RecyclerView.LayoutManager layoutManager,
                                                 OrientationHelper helper, int velocityX, int velocityY) {
        int[] distances = mDistanceHelper.calculateScrollDistance(velocityX, velocityY);
        float distancePerChild = mDistanceHelper.computeDistancePerChild(layoutManager, helper);
//        Log.w("xj", "总共滑动距离：" + distances[1] + " 子view高度:" + distancePerChild);
        if (distancePerChild <= 0) {
            return 0;
        }
        int distance = Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
        return (int) Math.abs(Math.floor(distance / distancePerChild));
    }


    /**
     * 判断偏移之后是否需要状态变更
     *
     * @param layout
     */
    public void onTranslationFinished(View layout) {
        changeState(isClosed(layout) ? STATE_CLOSED : STATE_OPENED);
    }


    public void openPager() {
        openPager(DURATION_SHORT);
    }

    /**
     * @param duration open animation duration
     */
    private void openPager(int duration) {
        View child = mChild.get();
        CoordinatorLayout parent = mParent.get();
        if (child != null) {
            if (mFlingRunnable != null) {
                child.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mFlingRunnable = new FlingRunnable(parent, child);
            mFlingRunnable.scrollToOpen(duration);
        }
    }

    public void closePager() {
        closePager(DURATION_SHORT);
    }

    /**
     * @param duration close animation duration
     */
    private void closePager(int duration) {
        View child = mChild.get();
        CoordinatorLayout parent = mParent.get();
        if (!isClosed()) {
            if (mFlingRunnable != null) {
                child.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mFlingRunnable = new FlingRunnable(parent, child);
            mFlingRunnable.scrollToClosed(duration);
        }
    }


    /**
     * 滑动到相应屏幕百分比的位置
     *
     * @param percentage 0.4
     */
    public void openPagerPercentage(float percentage) {
        View child = mChild.get();
        CoordinatorLayout parent = mParent.get();
        if (mFlingRunnable != null) {
            child.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }
        mFlingRunnable = new FlingRunnable(parent, child);
        mFlingRunnable.scrollToPercentage(percentage, DURATION_SHORT);
    }


    /**
     * 惯性滚动整个列表
     *
     * @param velocityX
     * @param velocityY
     */
    private void startFling(int velocityX, int velocityY) {
        final View child = mChild.get();
        if (child == null) {
            return;
        }

        //非底部不允许惯性滚动
        if (!isRecyclerViewBottom(child)) {
            return;
        }

        float startY = child.getTranslationY();
        float endY = child.getTranslationY() + velocityY;
        if (endY > 0) {
            endY = 0;
        }
        if (endY < 0 - measuredHeight) {
            endY = 0 - measuredHeight;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(child, "translationY",
                startY, endY);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                if (isClosed(child)) {
                    changeState(STATE_CLOSED);
                }
            }
        });
        objectAnimator.start();
    }

    private FlingRunnable mFlingRunnable;

    /**
     * For animation , Why not use {@link android.view.ViewPropertyAnimator } to play animation
     * is of the
     * other {@link CoordinatorLayout.Behavior} that depend on this could not receiving the
     * correct result of
     * {@link View#getTranslationY()} after animation finished for whatever reason that i don't know
     */
    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final View mLayout;

        FlingRunnable(CoordinatorLayout parent, View layout) {
            mParent = parent;
            mLayout = layout;
        }

        /**
         * 滚动到屏幕百分比位置
         */
        void scrollToPercentage(float persentage, int duration) {
            float curTranslationY = ViewCompat.getTranslationY(mLayout);
            //目标位置
            float disTranslationY = 0 - mLayout.getMeasuredHeight() * persentage;

            float scrollDistance = disTranslationY - curTranslationY;
            mOverScroller.startScroll(0, (int) curTranslationY, 0, (int) scrollDistance, duration);
            start();
        }


        void scrollToClosed(int duration) {
            float curTranslationY = ViewCompat.getTranslationY(mLayout);
            float dy = curTranslationY - mLayout.getMeasuredHeight();
            Log.d(TAG, "scrollToClosed: cur0:" + curTranslationY + ",end0:" + dy);
            Log.d(TAG, "scrollToClosed: cur:" + Math.round(curTranslationY) + ",end:" + Math
                    .round(dy));
            Log.d(TAG, "scrollToClosed: cur1:" + (int) (curTranslationY) + ",end:" + (int) dy);
            mOverScroller.startScroll(0, Math.round(curTranslationY - 0.1f), 0, Math.round(dy + 0.1f), duration);
            start();
        }

        void scrollToOpen(int duration) {
            float curTranslationY = ViewCompat.getTranslationY(mLayout);
            mOverScroller.startScroll(0, (int) curTranslationY, 0, (int) -curTranslationY,
                    duration);
            start();
        }

        private void start() {
            if (mOverScroller.computeScrollOffset()) {
                mFlingRunnable = new FlingRunnable(mParent, mLayout);
                ViewCompat.postOnAnimation(mLayout, mFlingRunnable);
            } else {
                onTranslationFinished(mLayout);
            }
        }

        @Override
        public void run() {
            if (mLayout != null && mOverScroller != null) {
                if (mOverScroller.computeScrollOffset()) {
                    Log.d(TAG, "run: " + mOverScroller.getCurrY());
                    ViewCompat.setTranslationY(mLayout, mOverScroller.getCurrY());
                    ViewCompat.postOnAnimation(mLayout, this);
                } else {
                    onTranslationFinished(mLayout);
                }
            }
        }
    }

    /**
     * 找到Behavior中RecyclerView实例
     *
     * @param view
     * @return
     */
    private RecyclerView findChildTypeOfRecyclerView(View view) {
        if (null == view || !(view instanceof ViewGroup)) {
            return null;
        }

        RecyclerView target = null;
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof RecyclerView) {
                return (RecyclerView) viewGroup.getChildAt(i);
            }
            target = findChildTypeOfRecyclerView(viewGroup.getChildAt(i));
        }
        return target;
    }

    /**
     * callback for HeaderPager 's state
     */
    public interface OnPagerStateListener {
        /**
         * do callback when pager closed
         */
        void onPagerClosed();

        /**
         * do callback when pager opened
         */
        void onPagerOpened();
    }

}
