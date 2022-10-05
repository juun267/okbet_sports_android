package org.cxct.sportlottery.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.youth.banner.indicator.BaseIndicator;

/**
 * 圆形指示器
 * 如果想要大小一样，可以将选中和默认设置成同样大小
 */
public class HomeBannerIndicator extends BaseIndicator {
    private int mNormalRadius;
    private int mSelectedRadius;

    public HomeBannerIndicator(Context context) {
        this(context, null);
    }

    public HomeBannerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeBannerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNormalRadius = config.getNormalWidth() / 2;
        mSelectedRadius = config.getSelectedWidth() / 2;
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }

        mNormalRadius = config.getNormalWidth() / 2;
        mSelectedRadius = config.getSelectedWidth() / 2;
        //间距*（总数-1）+选中宽度+默认宽度*（总数-1）
        int width = (count - 1) * config.getIndicatorSpace() + config.getSelectedWidth() + config.getNormalWidth() * (count - 1);
        setMeasuredDimension(width, config.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = config.getIndicatorSize();
        if (count <= 1) {
            return;
        }
        float left = 0;
        for (int i = 0; i < count; i++) {
            boolean isSelect = (config.getCurrentPosition() == i);
            mPaint.setColor(isSelect ? config.getSelectedColor() : config.getNormalColor());
            int indicatorWidth = isSelect ? config.getSelectedWidth() : config.getNormalWidth();
            int radius = isSelect ? mSelectedRadius : mNormalRadius;
            mPaint.setStyle(isSelect ? Paint.Style.FILL : Paint.Style.STROKE);
            if (isSelect) {
                canvas.drawRoundRect(new RectF(left, 0, left + indicatorWidth, config.getHeight()), radius, radius, mPaint);
            } else {
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(left + radius, radius, radius - 2, mPaint);
            }

            left += indicatorWidth + config.getIndicatorSpace();
        }
//        mPaint.setColor(config.getNormalColor());
//        for (int i = 0; i < count; i++) {
//            canvas.drawCircle(left + maxRadius, maxRadius, mNormalRadius, mPaint);
//            left += config.getNormalWidth() + config.getIndicatorSpace();
//        }
//        mPaint.setColor(config.getSelectedColor());
//        left = maxRadius + (config.getNormalWidth() + config.getIndicatorSpace()) * config.getCurrentPosition();
//        canvas.drawCircle(left, maxRadius, mSelectedRadius, mPaint);
    }

}
