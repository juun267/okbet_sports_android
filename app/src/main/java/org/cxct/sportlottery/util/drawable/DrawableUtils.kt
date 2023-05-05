package org.cxct.sportlottery.util.drawable

import android.graphics.drawable.Drawable
import androidx.annotation.IntDef
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.util.DisplayUtil.dp
import java.lang.Exception

object DrawableUtils {

    private val appCtx by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MultiLanguagesApplication.appContext
    }

    /**
     * cornerRadius:圆角弧度，直接传数值即可，内部会自动转化为dp
     * strokeWidth:边框宽度，直接传int类型数值即可，内部会自动转化为dp
     * strokeColor:边框颜色
     * solidColor:填充颜色
     */
    fun getCommonBackgroundStyle(
        cornerRadius: Int, strokeWith: Int = 1, strokeColor: Int, solidColor: Int
    ): Drawable {
        val drawableBuilder = DrawableCreator.Builder()
        return try {
            drawableBuilder.setCornersRadius(cornerRadius.dp.toFloat())
                .setStrokeWidth(strokeWith.dp.toFloat())
                .setStrokeColor(appCtx.getColor(strokeColor))
                .setSolidColor(appCtx.getColor(solidColor))
            drawableBuilder.build()
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
            cornerRadius = 5, strokeColor = R.color.color_EAEAEA, solidColor = R.color.color_F9F9F9
        )
    }


    fun getBasketballBetListButton(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 5, strokeColor = R.color.color_E1E9F8, solidColor = R.color.color_FFFFFF
        )
    }

    fun getBasketballDeleteButton(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 5, strokeColor = R.color.color_636466, solidColor = R.color.color_636466
        )
    }

    fun getBasketballPlusMore(): Drawable {
        return getCommonBackgroundStyle(
            cornerRadius = 5, strokeColor = R.color.color_025BE8, solidColor = R.color.color_FFFFFF
        )
    }

}