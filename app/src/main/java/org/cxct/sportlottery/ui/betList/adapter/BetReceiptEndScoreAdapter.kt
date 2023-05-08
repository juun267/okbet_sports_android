package org.cxct.sportlottery.ui.betList.adapter

import org.cxct.sportlottery.databinding.ItemBetReceiptEndscoreBinding
import org.cxct.sportlottery.network.bet.list.EndScoreInfo
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class BetReceiptEndScoreAdapter :
    BindingAdapter<EndScoreInfo, ItemBetReceiptEndscoreBinding>() {
    override fun onBinding(
        position: Int,
        binding: ItemBetReceiptEndscoreBinding,
        item: EndScoreInfo,
    ) {
        binding.root.text = item.playName
    }
}

