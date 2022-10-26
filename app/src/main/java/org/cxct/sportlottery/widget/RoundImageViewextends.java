package org.cxct.sportlottery.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

class RoundImageView extends AppCompatImageView {

    private int borderColor;// 圆形头像的边框颜色

    private int borderWidth;// 圆形头像的边框宽度

    private Paint mBitmapPaint;// 绘制图像的Paint

    private Paint mBorderPaint;

    private Matrix mMatrix;// 图像矩阵,本身是一个3*3矩阵

    private int mRadius;

    private int mImgWidth;

    private int centerX;

    private int centerY;

    /***
     *
     * @param context

     * @param attrs

     */

    private float smallInnerScaleRatio = 1f;//变小

    private float BigOutsideRatio = 1f;

    public RoundImageView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);

    }

    public RoundImageView(Context context) {

        this(context, null);

    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);

        mBitmapPaint = new Paint();

        mBorderPaint = new Paint();

        mMatrix = new Matrix();

        mBitmapPaint.setAntiAlias(true);

        mBorderPaint.setAntiAlias(true);

    }

    private void initAttrs(Context context, AttributeSet attrs) {

        borderColor = Color.RED;

        borderWidth = 8;

        postDelayed(new Runnable() {

            @Override

            public void run() {

                startAnim();

            }

        }, 2000);

    }

    private void startAnim() {

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.5f);

        valueAnimator.setInterpolator(new LinearInterpolator());

        valueAnimator.setDuration(500);

        valueAnimator.setRepeatCount(-1);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override

            public void onAnimationUpdate(ValueAnimator animation) {

                float value = (float) animation.getAnimatedValue();

                smallInnerScaleRatio = value;

                BigOutsideRatio = 1 + (1 - value);

                invalidate();

            }

        });

        valueAnimator.start();

    }

    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//写好一个控件自定义View,写出一个能用的自定义View不易啊。。。

        // 虽然测量这块儿寥寥几行代码,但是还得心细啊。。

        int imgHeight = setMeasureHeight(heightMeasureSpec) - getPaddingTop() - getPaddingBottom() - borderWidth * 2;

        int imgWidth = setMeasureWidth(widthMeasureSpec) - getPaddingLeft() - getPaddingRight() - borderWidth * 2;

        if (imgHeight < imgWidth) {

            mImgWidth = imgHeight;

            mRadius = mImgWidth / 2;

            centerX = mRadius;

            centerY = mRadius;

            Log.d("Round", "imgHeight < imgWidth");

        } else {//

            mImgWidth = imgWidth;

            mRadius = mImgWidth / 4;//半径是图片的一半

            centerX = mImgWidth / 2;

            centerY = centerX;

            Log.d("Round", "imgHeight > imgWidth");

        }

        setMeasuredDimension(setMeasureWidth(widthMeasureSpec), setMeasureHeight(heightMeasureSpec));

    }

    private int setMeasureHeight(int heightMeasureSpec) {

        int height = 0;

        int minHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56,

                getResources().getDisplayMetrics());

        int specMode = MeasureSpec.getMode(heightMeasureSpec);

        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (specMode) {

            case MeasureSpec.EXACTLY:

                height = (specSize < minHeight ? minHeight : specSize);// 此处我是设置了EXACTLY的值，仅是圆形图片大小的值

                break;

            case MeasureSpec.AT_MOST:

                height = minHeight + getPaddingTop() + getPaddingBottom();

                break;

            case MeasureSpec.UNSPECIFIED:

                height = minHeight + getPaddingTop() + getPaddingBottom();

                break;

        }

        return height;

    }

    private int setMeasureWidth(int widthMeasureSpec) {

        int width = 0;

        int minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56,

                getResources().getDisplayMetrics());

        int specMode = MeasureSpec.getMode(widthMeasureSpec);

        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        switch (specMode) {

            case MeasureSpec.EXACTLY:

                width = (specSize < minWidth ? minWidth : specSize);

                break;

            case MeasureSpec.AT_MOST:

                width = minWidth + getPaddingLeft() + getPaddingRight();

                break;

            case MeasureSpec.UNSPECIFIED:

                width = minWidth + getPaddingRight() + getPaddingLeft();

                break;

        }

        return width;

    }

    @Override

    protected void onDraw(Canvas canvas) {

        this.setBackgroundColor(Color.WHITE);

        if (getDrawable() == null) {

            return;

        }

        setShader();

        float realRadiusBitmap = mRadius * smallInnerScaleRatio;//修改图片绘制的半径，而缩放

        //中心点

        float centerTempx = centerX + borderWidth + getPaddingLeft();

        float centerTempy = centerY + borderWidth + getPaddingTop();

        canvas.drawCircle(centerTempx, centerTempy,

                realRadiusBitmap, mBitmapPaint);//绘制头像

        mBorderPaint.setColor(borderColor);

        mBorderPaint.setStyle(Paint.Style.STROKE);

        mBorderPaint.setStrokeWidth(borderWidth / 2);

        canvas.drawCircle(centerX + borderWidth + getPaddingLeft(), centerY + borderWidth + getPaddingTop(),

                mRadius + borderWidth / 2, mBorderPaint);//绘制外圆

        float realRadiusOut = mRadius * BigOutsideRatio;

        mBorderPaint.setStrokeWidth(borderWidth / 2);

        mBorderPaint.setColor(Color.GREEN);

        canvas.drawCircle(centerX + borderWidth + getPaddingLeft(), centerY + borderWidth + getPaddingTop(),

                realRadiusOut + borderWidth / 2, mBorderPaint);//绘制散动的圆

    }

    /**
     * 初始化bitmapShader
     * <p>
     * 图片缩放，通过矩阵进行。
     * <p>
     * 在于矩阵先缩放，再移动
     */

    private void setShader() {

        Drawable drawable = getDrawable();

        Bitmap bmp = drawableToBitmap(drawable);

        BitmapShader mBitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        float scale = 1.0f;

        // 去取bitmap中宽度和高度中更小的，为了使图像缩放之后，可以填充满控件的空间，

        // 此处切记要乘以1.0f，，这种低级错误写的时候又犯了一次。

        scale = mImgWidth * 1.0f / (Math.min(bmp.getWidth(), bmp.getHeight()));

//  scale=scale*currentScaleRatio;

        mMatrix.setScale(scale, scale);

        mMatrix.postTranslate(getPaddingLeft() + borderWidth, getPaddingTop() + borderWidth);//这里使用了Matrix的后乘进行效果叠加，

        // 使图像根据padding进行位移

        mBitmapShader.setLocalMatrix(mMatrix);

        mBitmapPaint.setShader(mBitmapShader);

    }

    /**
     * 将Drawable转变为bitmap
     */

    private Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {

            BitmapDrawable bd = (BitmapDrawable) drawable;

            return bd.getBitmap();

        }

        int h = drawable.getIntrinsicHeight();

        int w = drawable.getIntrinsicWidth();

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);// 建立对应的bitmap画布

        drawable.setBounds(0, 0, w, h);// 此处的setBounds是指，drawable将在canvas的0,0,w,h矩形区域内

        drawable.draw(canvas);// 将drawable的内容画到画布中去

        return bitmap;

    }

}