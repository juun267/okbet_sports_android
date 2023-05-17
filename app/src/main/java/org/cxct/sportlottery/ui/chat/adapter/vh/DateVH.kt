package org.cxct.sportlottery.ui.chat.adapter.vh

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.ui.chat.bean.ChatDateMsg
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class DateVH private constructor(private val tvDate: TextView, root: View): BaseViewHolder(root) {

    companion object {

        fun ofContext(context: Context): DateVH {

            val pV = 6.dp
            val pH = 28.dp
            val textView = AppCompatTextView(context)

            textView.setPadding(pH, pV, pH, pV)
            textView.setTextColor(Color.WHITE)
            textView.textSize = 10f
            textView.background = DrawableCreator.Builder()
                .setSolidColor(Color.BLACK)
                .setShapeAlpha(0.5f)
                .setCornersRadius(30.dp.toFloat())
                .build()

            val root = LinearLayout(context)
            root.gravity = Gravity.CENTER_HORIZONTAL
            root.setBackgroundColor(Color.YELLOW)
            root.layoutParams = LinearLayout.LayoutParams(-1, -2)
            root.addView(textView)
            return DateVH(textView, root)
        }

    }

    fun bind(data: ChatDateMsg) {
        tvDate.text = data.content
    }
}