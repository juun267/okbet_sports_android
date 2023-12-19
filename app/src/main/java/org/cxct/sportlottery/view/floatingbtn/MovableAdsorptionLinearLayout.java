package org.cxct.sportlottery.view.floatingbtn;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * 參考 https://www.jianshu.com/p/ba3e5fc5cff1
 * 修改成可拖曳 layout
 *
 * add 左右吸附功能 20211007 by Dean
 */

//"给要实现拖动左右吸附动view设置自定义OnTouchListener即可(View.setOnTouchListener(SuckEdgeTouch()))"
@Deprecated
public class MovableAdsorptionLinearLayout extends LinearLayout implements View.OnTouchListener {

    private final static float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    private float originalX, originalY; //紀錄初始座標
    private float downRawX, downRawY;
    private float dX, dY;

    public MovableAdsorptionLinearLayout(Context context) {
        super(context);
        init();
    }

    public MovableAdsorptionLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableAdsorptionLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        post(() -> {
            originalX = getX();
            originalY = getY();
        });
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            return true; // Consumed

        }
        else if (action == MotionEvent.ACTION_MOVE) {

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View)view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth, newX); // Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight, newY); // Don't allow the FAB past the bottom of the parent

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();

            return true; // Consumed

        }
        else if (action == MotionEvent.ACTION_UP) {

            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float parentWidth = ((ViewGroup) getParent()).getWidth();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

            if (upRawX >= parentWidth / 2) {
                //靠右吸附
                animate().setInterpolator(decelerateInterpolator)
                        .setDuration(500)
                        .xBy(parentWidth - getWidth() - getX())
                        .start();
            } else {
                //靠左吸附
                ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0F);
                oa.setInterpolator(decelerateInterpolator);
                oa.setDuration(500);
                oa.start();
            }

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            }
            else { // A drag
                return true; // Consumed
            }

        }
        else {
            return super.onTouchEvent(motionEvent);
        }

    }

    //回到初始座標
    public void resetPosition() {
        setX(originalX);
        setY(originalY);
    }

}
