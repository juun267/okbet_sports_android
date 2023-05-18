package org.cxct.sportlottery.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.cxct.sportlottery.R;
import org.cxct.sportlottery.util.ScreenUtil;
import org.cxct.sportlottery.view.bottomnavigationview.BottomNavigationViewInner;

public class ScrollExpandTextview extends ScrollView implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int MAX_LINE_COLLAPSE = 2;//收起时最大展示行数
    private static final String TEXT_EXPAND = "展开";
    private static final String TEXT_COLLAPSE = "收起";
    private static  int MAX_H = 100;
    private int w ;
    private int minH;
    private int curH;
    private int arrowSize;
    private TextView tvText;
    private TextView tvMore;
    private State state = State.COLLAPSE;
    private CharSequence text;
    private LayoutParams paramsMore;
    private int space;

    public enum State {
        EXPAND, COLLAPSE
    }

    public ScrollExpandTextview(@NonNull Context context) {
        super(context);
        init();
    }

    public ScrollExpandTextview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollExpandTextview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(w, curH);//固定宽高
    }

    private void init() {
        w = (int) (0.7 * ScreenUtil.INSTANCE.getScreenWidth(getContext()));
        MAX_H = 3 * BottomNavigationViewInner.dp2px(getContext(),50);
        minH = BottomNavigationViewInner.dp2px(getContext(),50);
        curH = minH;
        arrowSize = BottomNavigationViewInner.dp2px(getContext(),7);
        space = BottomNavigationViewInner.dp2px(getContext(),3);

        FrameLayout flContainer = new FrameLayout(getContext());
        tvText = new TextView(getContext());
        tvText.setTextSize(14);
        tvText.setMaxHeight(BottomNavigationViewInner.dp2px(getContext(),50));
        tvText.setMaxLines(MAX_LINE_COLLAPSE);
        tvText.setIncludeFontPadding(true);
        tvText.setLineSpacing(1.0f, 1.2f);
        tvMore = new TextView(getContext());
        tvMore.setBackgroundResource(R.drawable.bg_sport_card_yellow);
        tvMore.setMaxLines(1);
        tvMore.setTextSize(12);
        tvMore.setGravity(Gravity.CENTER);
        tvMore.setVisibility(GONE);
        tvMore.setOnClickListener(v -> {
            if (state == State.COLLAPSE) {
                state = State.EXPAND;
                tvText.setMaxLines(Integer.MAX_VALUE);
                tvText.setText(text);
            } else {
                state = State.COLLAPSE;
                tvText.setMaxLines(MAX_LINE_COLLAPSE);
            }
            setMoreViewPosition();
        });

        tvText.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        paramsMore = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMore.setLayoutParams(paramsMore);
        flContainer.addView(tvText);
        flContainer.addView(tvMore);
        addView(flContainer);

        setTextColor(ContextCompat.getColor(getContext(), R.color.color_2b2b2b_ffffff));
        setMoreTextColor(ContextCompat.getColor(getContext(), R.color.color_2b2b2b_ffffff));

        setOverScrollMode(OVER_SCROLL_NEVER);
        setVerticalScrollBarEnabled(false);
    }

    private void setMoreViewPosition() {
        Layout layout = tvText.getLayout();
        if (layout == null)
            return;
        int lineCount = layout.getLineCount();
        int lineH = layout.getLineBottom(0) - layout.getLineTop(0);
        minH = MAX_LINE_COLLAPSE * lineH;
        curH = lineCount * lineH;
        if (text == null || lineCount <= MAX_LINE_COLLAPSE && tvText.length() == text.length()) {
            tvMore.setVisibility(GONE);
        } else {
            if (state == State.COLLAPSE) {
                curH = minH;

                float lineWidth = layout.getLineWidth(MAX_LINE_COLLAPSE - 1);
                //获取第2行最后一个字符的下标
                int lineEnd = layout.getLineEnd(MAX_LINE_COLLAPSE - 1);
                //计算每个字符占的宽度
                float widthPerChar = layout.getLineWidth(MAX_LINE_COLLAPSE - 1) / (lineEnd + 1);
                float diff = lineWidth + tvMore.getMeasuredWidth() + space - (getWidth() - getPaddingLeft() - getPaddingRight());
                //第二行展示不下，去掉第二行最后几个字符，用来放展开按钮
                if (diff > 0) {
                    int removeCount = (int) (diff / widthPerChar);
                    if (lineEnd > removeCount) {
                        CharSequence t = text.subSequence(0, lineEnd - removeCount) + "...";
                        setTextAndRefresh(t);
                        return;//setText会重新触发onGlobalLayout
                    }
                }
                //获取第二行字符的坐标，设置展开按钮的margin，使展开按钮在文本后面
                paramsMore.leftMargin = (int) layout.getLineRight(MAX_LINE_COLLAPSE - 1) + space;
                paramsMore.topMargin = lineH + tvText.getPaddingTop() - space;

                tvMore.setText(TEXT_EXPAND);
                drawRight4MoreView(R.drawable.arrow_up_white);
            } else {
                if (curH > MAX_H)
                    curH = MAX_H;

                float lineWidth = layout.getLineWidth(lineCount - 1);
                if (lineWidth + tvMore.getMeasuredWidth() - (getWidth() - getPaddingLeft() - getPaddingRight()) > 0) {//最后一行显示不下，将最后一行换行
                    if (text.length() > 2) {
                        //分两个字符到tvMore那一行，更协调
                        String tmp = text.subSequence(0, text.length() - 2) + "\n" + text.subSequence(text.length() - 2, text.length());
                        setTextAndRefresh(tmp);
                        return;//setText会重新触发onGlobalLayout
                    }
                }
                tvMore.setText(TEXT_COLLAPSE);
                drawRight4MoreView(R.drawable.arrow_up_white);

                paramsMore.leftMargin = (int) layout.getSecondaryHorizontal(layout.getLineEnd(lineCount - 1)) + space;
                paramsMore.topMargin = layout.getHeight() - tvText.getPaddingBottom() - lineH + BottomNavigationViewInner.dp2px(getContext(),2);
            }
            tvMore.setVisibility(VISIBLE);
        }
        getLayoutParams().height = curH;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setMoreViewPosition();
    }

    @Override
    public void onGlobalLayout() {
        //为保证TextView.getLayout()!=null，在这里再执行相关逻辑
        setMoreViewPosition();
        //记得移除，不然会一直回调
        tvText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void setText(final CharSequence text) {
        this.text = text;
        setTextAndRefresh(text);
    }

    public void setTextAndRefresh(CharSequence text) {
        tvText.getViewTreeObserver().addOnGlobalLayoutListener(this);
        tvText.setText(text);
    }

    private void drawRight4MoreView(int icRes) {
        Drawable drawable = getResources().getDrawable(icRes);
        /// 这一步必须要做,否则不会显示.
        drawable.setBounds(arrowSize / 3, 0, arrowSize, arrowSize / 3);
        tvMore.setCompoundDrawables(null, null, drawable, null);
    }

    public void setTextColor(int color) {
        tvText.setTextColor(color);
    }

    public void setMoreTextColor(int color) {
        tvMore.setTextColor(color);
    }


    //如果不需要处理滑动冲突，去掉下面的代码即可
    private int startX, startY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) ev.getX();
                int endY = (int) ev.getY();
                int disX = Math.abs(endX - startX);
                int disY = Math.abs(endY - startY);
                if (disX > disY) {
                    getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(startX - endX));
                } else {
                    getParent().requestDisallowInterceptTouchEvent(canScrollVertically(startY - endY));
                }
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
