package org.cxct.sportlottery.ui.results.vh

import org.cxct.sportlottery.databinding.ContentGameDetailResultRvBinding
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList

//詳情
class DetailItemViewHolder(val binding: ContentGameDetailResultRvBinding) {

    fun bind(detailData: MatchResultPlayList?) {
        setupDetailItem(detailData)
    }

    private fun setupDetailItem(detailData: MatchResultPlayList?) {
         binding.apply {
            tvPlayCateName.text = "${detailData?.playCateName} ${detailData?.spread}"
            tvPlayContent.text = detailData?.playName
        }
    }
}