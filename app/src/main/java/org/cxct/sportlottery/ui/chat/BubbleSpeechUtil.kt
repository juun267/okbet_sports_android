package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R

object BubbleSpeechUtil {

    fun getBubbleSpeech(context: Context, colorTop: String, colorBottom: String): Drawable? {
        val drawable =
            ContextCompat.getDrawable(context, R.drawable.bg_bubble_speech)
        try {
            val color = Color.parseColor(colorBottom)
            val color2 = Color.parseColor(colorTop)

            (((drawable as LayerDrawable).getDrawable(0) as LayerDrawable).getDrawable(0) as VectorDrawable).mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN)
            (drawable.getDrawable(0) as LayerDrawable).getDrawable(1).mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN)

            ((drawable.getDrawable(1) as LayerDrawable).getDrawable(0) as VectorDrawable).mutate()
                .setColorFilter(color2, PorterDuff.Mode.SRC_IN)
            (drawable.getDrawable(1) as LayerDrawable).getDrawable(1).mutate()
                .setColorFilter(color2, PorterDuff.Mode.SRC_IN)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return drawable
    }

    fun getBubblePlanPush(context: Context, colorTop: String, colorBottom: String): Drawable? {
        val drawable =
            ContextCompat.getDrawable(context, R.drawable.bg_bubble_plan_push)
        try {
            val color = Color.parseColor(colorBottom)
            val color2 = Color.parseColor(colorTop)

            (((drawable as LayerDrawable).getDrawable(0) as LayerDrawable).getDrawable(0) as VectorDrawable).mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN)
            (drawable.getDrawable(0) as LayerDrawable).getDrawable(1).mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN)

            //已經是白背景不需要再覆蓋白色了 會擋住原先的箭頭
//            ((drawable.getDrawable(1) as LayerDrawable).getDrawable(0) as InsetDrawable).mutate().setColorFilter(color2, PorterDuff.Mode.SRC_IN)
//            (drawable.getDrawable(1) as LayerDrawable).getDrawable(1).mutate().setColorFilter(color2, PorterDuff.Mode.SRC_IN)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return drawable
    }

    interface OnSetColorListener {
        fun onReceive(bubbleSpeech: Drawable)
        fun onError()
    }

}