package org.cxct.sportlottery.ui.game.publicity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.cxct.sportlottery.databinding.ContentPublicityPromotionMarqueeBinding
import org.cxct.sportlottery.ui.MarqueeAdapter

class PublicityPromotionMarqueeAdapter : MarqueeAdapter() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView =
            ContentPublicityPromotionMarqueeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == Type.BLANK.ordinal) layoutView.root.minimumWidth = viewGroup.measuredWidth
        return PromotionDetailViewHolder(layoutView)
    }

    inner class PromotionDetailViewHolder(val binding: ContentPublicityPromotionMarqueeBinding) :
        DetailViewHolder(binding.root) {
        override var detail: TextView = binding.tvMarquee
    }
}