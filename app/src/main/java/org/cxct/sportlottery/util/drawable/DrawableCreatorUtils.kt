package org.cxct.sportlottery.util.drawable

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.util.DisplayUtil.dp
import java.lang.Exception

object DrawableCreatorUtils {

    private val appCtx by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MultiLanguagesApplication.appContext
    }

    /**
     * cornerRadius:圆角弧度，直接传数值即可，内部会自动转化为dp
     * strokeWidth:边框宽度，直接传int类型数值即可，内部会自动转化为dp
     * strokeColor:边框颜色, 默认值0xaaff，如果没有传的话就不设置边框颜色
     * solidColor:填充颜色
     */
    fun getCommonBackgroundStyle(
        cornerRadius: Int, @ColorRes solidColor: Int, @ColorRes strokeColor: Int = 0x0, strokeWidth: Int = 1
    ): Drawable {
        val drawableBuilder = DrawableCreator.Builder()
        return try {
            drawableBuilder.apply {
                setCornersRadius(cornerRadius.dp.toFloat())
                setStrokeWidth(strokeWidth.dp.toFloat())
                if (strokeColor != 0x0) {
                    setStrokeColor(appCtx.getColor(strokeColor))
                }
                setSolidColor(appCtx.getColor(solidColor))
            }.build()
        } catch (e: Exception) {
            e.printStackTrace()
            drawableBuilder.build()
        }
    }

    fun getGradientBackgroundStyle(
        cornerRadius: Int, @ColorRes startColorRes: Int, @ColorRes endColorRes: Int, @ColorRes strokeColor: Int = 0x0, strokeWidth: Int = 1
    ): Drawable {
        val drawableBuilder = DrawableCreator.Builder()
        return try {
            drawableBuilder.apply {
                setCornersRadius(cornerRadius.dp.toFloat())
                setGradientAngle(0)
                setGradientColor(appCtx.getColor(startColorRes), appCtx.getColor(endColorRes))
                setStrokeWidth(strokeWidth.dp.toFloat())
                if (strokeColor != 0x0) {
                    setStrokeColor(appCtx.getColor(strokeColor))
                }
            }.build()
        } catch (e: Exception) {
            e.printStackTrace()
            drawableBuilder.build()
        }
    }

    /**
     * color 直接传色值即可 R.color.xxx
     */
    fun getCommonBackgroundStyle(
        leftTopCornerRadius: Int = 0,
        rightTopCornerRadius: Int = 0,
        leftBottomCornerRadius: Int = 0,
        rightBottomCornerRadius: Int = 0,
        solidColor: Int,
        strokeColor: Int = 0xaaff,
        strokeWidth: Int,
    ): Drawable {
        val drawableBuilder = DrawableCreator.Builder()
        return try {
            drawableBuilder.apply {
                setCornersRadius(
                    leftBottomCornerRadius.dp.toFloat(),
                    rightBottomCornerRadius.dp.toFloat(),
                    leftTopCornerRadius.dp.toFloat(),
                    rightTopCornerRadius.dp.toFloat()
                )
                setStrokeWidth(strokeWidth.dp.toFloat())
                if (strokeColor != 0xaaff) {
                    setStrokeColor(appCtx.getColor(strokeColor))
                }
                setSolidColor(appCtx.getColor(solidColor))
            }.build()
        } catch (e: Exception) {
            e.printStackTrace()
            drawableBuilder.build()
        }
    }


    /**
     * 篮球末位比分，删除全部按钮
     */
    fun getBasketballDeleteAllDrawable(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 16, strokeColor = R.color.color_FFCCC9, solidColor = R.color.color_F9F9F9
        )
    }


    fun getBasketballBetListButton(): Drawable {
        return getCommonBackgroundStyle(
            5, R.color.color_FFFFFF, R.color.color_E1E9F8
        )
    }

    fun getBasketballDeleteButton(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 5, solidColor = R.color.color_A1050B18
        )
    }

    fun getBasketballPlusMore(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 5, strokeColor = R.color.color_025BE8, solidColor = R.color.color_ecf2fe
        )
    }

}