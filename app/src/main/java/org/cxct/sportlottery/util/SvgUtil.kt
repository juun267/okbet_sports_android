package org.cxct.sportlottery.util

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.graphics.drawable.VectorDrawable
import com.caverock.androidsvg.SVG
import org.cxct.sportlottery.R

object SvgUtil {
    fun getSvgDrawable(context: Context, svgData: String): PictureDrawable{
        val data =
            String.format(context.getString(R.string.svg_format), 48, 48, 24, 24, svgData)
        val svgFile = SVG.getFromString(data)
        return PictureDrawable(svgFile.renderToPicture())
    }
}