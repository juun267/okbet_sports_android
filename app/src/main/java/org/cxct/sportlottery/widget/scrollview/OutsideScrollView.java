package org.cxct.sportlottery.widget.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class OutsideScrollView extends ScrollView {

    public OutsideScrollView(Context context) {
        this(context, null);
    }

    public OutsideScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutsideScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);//由于真正的滑动效果，都是在onTouchEvent中处理的，所以，只要让本ScrollView的onTouchEvent能收到事件消费。
        //scrollView默认消费Event，所以上层的onTouchEvent是不会执行的
    }

}