package org.cxct.sportlottery.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import org.cxct.sportlottery.R;

public class StrokeTextView extends AppCompatTextView {
    private final int strokeColor;
    private final float strokeWidth;
    TextPaint paint = new TextPaint();

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StrokeStyleable, 0, 0);
        strokeWidth = a.getDimension(R.styleable.StrokeStyleable_strokeWidth, 6);
        strokeColor = a.getColor(R.styleable.StrokeStyleable_strokeColor, Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ColorStateList textColor = getTextColors();
        paint = this.getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(10);
        this.setTextColor(strokeColor);
        paint.setStrokeWidth(strokeWidth);
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        setTextColor(textColor);
        super.onDraw(canvas);
    }
}