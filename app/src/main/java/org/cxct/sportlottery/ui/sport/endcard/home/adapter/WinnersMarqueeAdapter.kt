package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.Spanny

class WinnersMarqueeAdapter: BaseQuickAdapter<String, BaseViewHolder>(0) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = AppCompatTextView(parent.context)
        textView.textSize = 14f
        textView.typeface = AppFont.helvetica
        textView.setTextColor(context.getColor(R.color.color_BEC7DC))
        return BaseViewHolder(textView)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        val textView = holder.itemView as TextView
        textView.text = item
    }

    private fun spanWith(userName: String, amount: String, homeName: String, awayName: String): CharSequence {
        val content = "$userName win $amount in $homeName VS $awayName."
        return Spanny(content).findAndSpan(amount){
            ForegroundColorSpan(ContextCompat.getColor(context,R.color.color_00E701))
        }
    }
}