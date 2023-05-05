package org.cxct.sportlottery.util.drawable

import android.graphics.drawable.Drawable
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.util.DisplayUtil.dp

object DrawableUtils {

    private val appCtx by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MultiLanguagesApplication.appContext
    }

    fun getCommonBackgroundStyle(
        cornerRadius: Float, strokeWith: Float = 1.dp.toFloat(), strokeColor: Int, solidColor: Int
    ): Drawable {
        return DrawableCreator.Builder().setCornersRadius(cornerRadius).setStrokeWidth(strokeWith)
            .setStrokeColor(appCtx.getColor(strokeColor)).setSolidColor(appCtx.getColor(solidColor))
            .build()
    }

    /**
     * 篮球末位比分，删除按钮
     */
    fun getBasketballDeleteAllDrawable(): Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeWidth(1.dp.toFloat()).setStrokeColor(appCtx.getColor(R.color.color_EAEAEA))
            .setSolidColor(appCtx.getColor(R.color.color_F9F9F9)).build()
    }


    fun getBasketballBetListButton(): Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeWidth(1.dp.toFloat()).setStrokeColor(appCtx.getColor(R.color.color_E1E9F8))
            .setSolidColor(appCtx.getColor(R.color.color_FFFFFF)).build()
    }

    fun getBasketballDeleteButton(): Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setSolidColor(appCtx.getColor(R.color.color_636466)).build()
    }

    fun getBasketballPlusMore(): Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeWidth(1.dp.toFloat()).setStrokeColor(appCtx.getColor(R.color.color_025BE8))
            .setSolidColor(appCtx.getColor(R.color.color_FFFFFF)).build()
    }

}