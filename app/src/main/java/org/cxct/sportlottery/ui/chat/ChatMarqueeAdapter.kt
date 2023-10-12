package org.cxct.sportlottery.ui.chat

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.MixFontTextView

/**
 * @author kevin
 * @create 2023/3/15
 * @description
 */
class ChatMarqueeAdapter : MarqueeAdapter() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val textView = MixFontTextView(viewGroup.context)
        textView.layoutParams = MarginLayoutParams(-2, -1).apply { marginEnd = 375.dp }
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.textSize = 12.0f
        textView.maxLines = 1
        textView.setTextColor(viewGroup.context.getColor(R.color.color_chat_marquee_text))
        return MarqueeVH(textView, textView)
    }


}