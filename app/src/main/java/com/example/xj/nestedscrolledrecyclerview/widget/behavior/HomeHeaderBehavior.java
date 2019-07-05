package com.example.xj.nestedscrolledrecyclerview.widget.behavior;

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
 * RecyclerView滑动到底部后开始嵌套滚动,进行位移
 */
public class HomeHeaderBehavior extends ViewOffsetBehavior {
    private static final String TAG = "UcNewsHeaderPager";
    public static final int STATE_OPENED = 0;
    public static final int STATE_CLOSED = 1;//滚到下方为close
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
    int measuredHeight;

    private VelocityTracker mVelocityTracker;
    private int mScrollPointerId = -1;

    int mMinFlingVelocity;
    int mMaxFlingVelocity;

    float velocityX;
    float velocityY;

    //信息流有数据才需要共同滑动
    private boolean needNestedScroll;

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

        final ViewConfiguration vc = ViewConfiguration.get(context);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void layoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        super.layoutChild(parent, child, layoutDirection);
        mParent = new WeakReference<CoordinatorLayout>(parent);
        mChild = new WeakReference<View>(child);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View
            directTargetChild, View target, int nestedScrollAxes, int type) {
        //拦截垂直方向上的滚动事件且当前状态是打开的
//        Log.d(TAG, "onStartNestedScroll: nestedScrollAxes=" + nestedScrollAxes + " b = " + b);
        boolean b = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && !isClosed(child);
//        LogUtil.d(TAG, "是否由父类共同处理滑动:" + b);

        return b && needNestedScroll;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
//        LogUtil.i("xj", "--------->onStopNestedScroll");
        mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
        velocityX = mVelocityTracker.getXVelocity(mScrollPointerId);
        velocityY = mVelocityTracker.getYVelocity(mScrollPointerId);
        mVelocityTracker.clear();

        //behavior机制可能更新不到close状态
//        refreshState(child);

        dispathFling(coordinatorLayout, child, velocityX, velocityY);
    }

    /**
     * 底部位置,做偏移处理
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        //dy>0 scroll up;dy<0,scroll down
//        LogUtil.i(TAG, "onNestedPreScroll: dy=" + dy);
        isUp = dy > 0;

        measuredHeight = child.getMeasuredHeight();//控件高度

//        LogUtil.w(TAG, "onNestedPreScroll ,child.getMeasuredHeight()=" + child.getMeasuredHeight());

        float lastTranslationY = child.getTranslationY();//滑动前偏移

        if (isClosed(child)) {
            return;
        }

        if (isUp) {
            //上滑
            if (isRecyclerViewBottom(child)) {
                //滚动recyclerView底部,有父类处理滑动
                if (lastTranslationY - dy <= 0 - measuredHeight) {
                    //考虑顶部边界
                    child.setTranslationY(0 - measuredHeight);
                    consumed[1] = (int) (lastTranslationY + measuredHeight);//实际消费距离
//                    LogUtil.i("xj", "onNestedPreScroll>>>>调用修改状态为close");

                    changeState(STATE_CLOSED);
                } else {
                    child.setTranslationY(lastTranslationY - dy);
//                    LogUtil.i("xj", "onNestedPreScroll>>>>判断是否需要修改状态");
                    onTranslationFinished(child);
                    consumed[1] = dy;
                }
            }
        } else {
            if (!isOpen(child)) {
                //非打开状态都要进行联滚
                if (lastTranslationY - dy >= 0) {
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
     * 结合子View联滚处理
     *
     * @param coordinatorLayout
     * @param child
     * @param target
     * @param dxConsumed
     * @param dyConsumed
     * @param dxUnconsumed
     * @param dyUnconsumed
     */
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (isClosed(child)) {
            return;
        }

        if (isUp) {
            //上滑且滑动到底部，需要偏移
            if (isRecyclerViewBottom(child)) {
                //父View继续消费
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


    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        // consumed the flinging behavior until Closed
        boolean coumsed = isOpen(child) || isClosed(child);//这两种状态由子类滚动
//        LogUtil.i(TAG, "onNestedPreFling: coumsed=" + coumsed);
        return !coumsed;
    }

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
        RecyclerView recyclerView = findChildTypeOfRecyclerView(child);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        return lastPosition == recyclerView.getAdapter().getItemCount() - 1;
    }

    /**
     * 是否滚动到头部
     *
     * @param child
     * @return
     */
    private boolean isRecyclerViewTop(View child) {
        RecyclerView recyclerView = findChildTypeOfRecyclerView(child);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return linearLayoutManager.findFirstVisibleItemPosition() == 0;//是否处于顶部
    }

    /**
     * 关闭状态
     *
     * @param child
     * @return
     */
    private boolean isClosed(View child) {
        boolean isClosed = child.getTranslationY() == 0 - child.getMeasuredHeight();
//        LogUtil.w(TAG, "isClosed ,child.getMeasuredHeight()=" + child.getMeasuredHeight());
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


    public boolean isNeedNestedScroll() {
        return needNestedScroll;
    }

    public void setNeedNestedScroll(boolean needNestedScroll) {
        this.needNestedScroll = needNestedScroll;
    }

    public boolean isClosed() {
        return mCurState == STATE_CLOSED;
    }

    private void changeState(int newState) {
//        LogUtil.i("xj", "--->修改状态,0打开，1关闭，newState:" + newState + "  mCurState:" + mCurState);

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

    int needScrollChildCount = 0;
    float distancePerChild;
    int hDeltaJump;

    /**
     * 共同处理惯性滑动
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
                        return;
                    }
                    if (Math.abs(needScrollChildCount * distancePerChild) < measuredHeight) {
                        //需要偏移的量小于屏幕高度，只做偏移
                        startFling(0, (int) (velocityY / 8));
                    } else {
                        //上面recyclerview+偏移+下面recyclerview
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
                startFling(0, (int) velocityY / 8);
            } else {
                needScrollChildCount = lastVisiableItem + hDeltaJump - recyclerView.getAdapter().getItemCount();//滚动到底部之后还需要滚动位置
                distancePerChild = mDistanceHelper.computeDistancePerChild(linearLayoutManager, mDistanceHelper.getVerticalHelper(linearLayoutManager));
            }
        } else {
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                //模拟滚动
                startFling(0, (int) velocityY / 8);
            }
        }

//        LogUtil.i("xj", "lastVisiableItem=" + lastVisiableItem + " 需要滚动hDeltaJump=" + hDeltaJump + " 下个recyclerview需要滚动: " + needScrollChildCount);
    }

    /**
     * 获取滚动position个数
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
//        LogUtil.w("xj", "总共滑动距离：" + distances[1] + " 子view高度:" + distancePerChild);
        if (distancePerChild <= 0) {
            return 0;
        }
        int distance = Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
        return (int) Math.abs(Math.floor(distance / distancePerChild));
    }


    private void onFlingFinished(CoordinatorLayout coordinatorLayout, View layout) {
        changeState(isClosed(layout) ? STATE_CLOSED : STATE_OPENED);
    }

    /**
     * 作为偏移之后是否需要状态变更
     *
     * @param layout
     */
    public void onTranslationFinished(View layout) {
        changeState(isClosed(layout) ? STATE_CLOSED : STATE_OPENED);
    }

    /**
     * 外部刷新抽屉状态
     *
     * @param child
     */
    public void refreshState(View child) {
        if (isOpen(child)) {
            changeState(STATE_OPENED);
        } else if (isClosed(child)) {
            changeState(STATE_CLOSED);
        }
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
     * 只展开到PK位置
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
//            changeState(STATE_CLOSED);
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
                onFlingFinished(mParent, mLayout);
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
                    onFlingFinished(mParent, mLayout);
                }
            }
        }
    }

    /**
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
