package org.cxct.sportlottery.ui.game.publicity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.cxct.sportlottery.databinding.ContentPublicityAnnouncementMarqueeBinding

class PublicityAnnouncementMarqueeAdapter : MarqueeAdapter() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView =
            ContentPublicityAnnouncementMarqueeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == TYPE_BLANK) layoutView.root.minimumWidth = viewGroup.measuredWidth
        return AnnouncementDetailViewHolder(layoutView)
    }

    inner class AnnouncementDetailViewHolder(val binding: ContentPublicityAnnouncementMarqueeBinding) :
        DetailViewHolder(binding.root) {
        override var detail: TextView = binding.tvMarquee
    }
}