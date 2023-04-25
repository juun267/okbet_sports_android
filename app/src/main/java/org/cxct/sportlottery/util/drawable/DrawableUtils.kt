package org.cxct.sportlottery.util.drawable

import android.graphics.drawable.Drawable
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp

object DrawableUtils {

    /**
     * 篮球末位比分，删除按钮
     */
    fun getBasketballDeleteAllDrawable(root: View): Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeWidth(1.dp.toFloat())
            .setStrokeColor(root.context.getColor(R.color.color_EAEAEA))
            .setSolidColor(root.context.getColor(R.color.color_F9F9F9)).build()
    }


    fun getBasketballBetListButton(root:View):Drawable{
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setStrokeWidth(1.dp.toFloat())
            .setStrokeColor(root.context.getColor(R.color.color_E1E9F8))
            .setSolidColor(root.context.getColor(R.color.color_F7FAFE)).build()
    }

    fun getBasketballDeleteButton(root:View) :Drawable {
        return DrawableCreator.Builder().setCornersRadius(5.dp.toFloat())
            .setSolidColor(root.context.getColor(R.color.color_636466)).build()
    }

}