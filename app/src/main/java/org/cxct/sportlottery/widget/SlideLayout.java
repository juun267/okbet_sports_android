package org.cxct.sportlottery.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SlideLayout extends ViewGroup {
    private static final int TIME = 100;

    private View childOne, childTwo;

    //最大滑动距离
    private int scrollLimitSize;

    private int startX;
    private int startY;
    private Scroller scroller;

    //当前滑动位置
    private int currentScroll;

    private boolean isClick = true;

    private boolean canScrooll;

    public SlideLayout(Context context) {
        super(context);
        init(context);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
    }

    /**
     * 展开
     */
    public void open() {
        scroller.startScroll(getScrollX(), 0, scrollLimitSize - getScrollX(), 0, TIME);
        invalidate();
    }

    /**
     * 收起
     */
    public void put() {
        scroller.startScroll(getScrollX(), 0, -getScrollX(), 0, TIME);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count != 2) {
            throw new RuntimeException("only can host 2 direct child view");
        }

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        childOne = getChildAt(0);
        childTwo = getChildAt(1);

        childTwo.measure(MeasureSpec.makeMeasureSpec(childTwo.getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childOne.getMeasuredHeight(), MeasureSpec.EXACTLY));

        int ps = getPaddingStart();
        int pt = getPaddingTop();
        int pe = getPaddingEnd();
        int pb = getPaddingBottom();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int slideWith = ps + pe;
        int slideHigh = pt + pb;

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                slideWith = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    slideWith += child.getMeasuredWidth();
                }
                break;
            default:
                break;
        }
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                slideHigh = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                slideHigh += Math.max(childOne.getMeasuredHeight(), childTwo.getMeasuredHeight());
                break;
            default:
                break;
        }
        setMeasuredDimension(slideWith, slideHigh);
    }

    @Override
    protected void onLayout(boolean b, int l, int i1, int i2, int i3) {
        int ps = getPaddingStart();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        int childOneWith = childOne.getMeasuredWidth();
        int childOneHigh = childOne.getMeasuredHeight();
        int childTwoWith = childTwo.getMeasuredWidth();
        int childTwoHigjh = childTwo.getMeasuredHeight();

        childOne.layout(ps, pt, ps + childOneWith, pt + childOneHigh + pb);
        childTwo.layout(ps + childOneWith, pt, ps + childOneWith + childTwoWith, pt + Math.max(childOneHigh, childTwoHigjh) + pb);

        scrollLimitSize = childTwoWith;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                startX = (int) event.getX();
                startY = (int) event.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                isClick = false;
                return true;
            case MotionEvent.ACTION_UP:
                return !isClick;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int endY = (int) (event.getY() - startY);
                int endX = (int) (event.getX() - startX);
                if ((Math.abs(endY) > Math.abs(endX)) && !canScrooll) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    resetScroll();
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    canScrooll = true;
                }
                if (canScrooll) {
                    int scroll = currentScroll + endX;
                    if (scroll < -scrollLimitSize) {
                        scroll = -scrollLimitSize;
                    } else if (scroll > 0) {
                        scroll = 0;
                    }
                    scrollTo(-scroll, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                int divide = scrollLimitSize >> 1;
                if (getScrollX() > divide) {
                    scroller.startScroll(getScrollX(), 0, scrollLimitSize - getScrollX(), 0, TIME);
                } else {
                    scroller.startScroll(getScrollX(), 0, -getScrollX(), 0, TIME);
                }
                invalidate();
                currentScroll = -getScrollX();
                getParent().requestDisallowInterceptTouchEvent(false);
                canScrooll = false;
                return !isClick;
            default:
                break;
        }
        return true;
    }

    private void resetScroll() {
        scroller.startScroll(getScrollX(), 0, -getScrollX(), 0, TIME);
        invalidate();
        currentScroll = -getScrollX();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }
}
