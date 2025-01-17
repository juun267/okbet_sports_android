package org.cxct.sportlottery.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class RCVDecoration extends RecyclerView.ItemDecoration {

    private float mDividerHeight = 1; //线的高度
    private Paint mPaint;           //画笔将自己做出来的分割线矩形画出颜色
    private float margin = 0;       //左右偏移量
    private float rightMargin = 0;

    public RCVDecoration() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);          //抗锯齿
        mPaint.setColor(Color.GRAY);        //默认颜色
    }

    //通过建造者模式来设置三个属性
    //设置左右偏移(默认是设置的一样的&#xff0c;若需要自己更改)
    public RCVDecoration setMargin(float margin) {
        this.margin = margin;
        this.rightMargin = margin;
        return this;
    }

    //设置颜色
    public RCVDecoration setColor(int color) {
        mPaint.setColor(color);
        return this;
    }

    //设置分割线高度
    public RCVDecoration setDividerHeight(float height) {
        this.mDividerHeight = height;
        return this;
    }

    public RCVDecoration setRightMargin(float rightMargin) {
        this.rightMargin = rightMargin;
        return this;
    }

    //在这里就已经把宽度的偏移给做好了
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //第一个ItemView不需要在上面绘制分割线
        if (parent.getChildAdapterPosition(view) != 0) {

            outRect.top = (int) mDividerHeight;//指相对itemView顶部的偏移量
        }
    }

    //这里主要是绘制颜色的
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        int childCount = parent.getChildCount();
//因为getItemOffsets是针对每一个ItemView&#xff0c;而onDraw方法是针对RecyclerView本身&#xff0c;所以需要循环遍历来设置
        for (int i = 0; i < childCount ; i++){
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);
            //第一个ItemView不需要绘制
            if (index == 0){
                continue;//跳过本次循环体中尚未执行的语句&#xff0c;立即进行下一次的循环条件判断
            }
            float dividerTop = view.getTop() - mDividerHeight;
            float dividerLeft = parent.getPaddingLeft() + margin;
            float dividerBottom = view.getTop();
            float dividerRight = parent.getWidth() - parent.getPaddingRight() - rightMargin;
            c.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, mPaint);
        }
    }
}