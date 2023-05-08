package org.cxct.sportlottery.ui.betRecord

import org.cxct.sportlottery.databinding.ItemBetRecordEndscoreBinding
import org.cxct.sportlottery.network.bet.list.EndScoreInfo
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class BetRecordEndScoreAdapter :
    BindingAdapter<EndScoreInfo, ItemBetRecordEndscoreBinding>() {
    override fun onBinding(
        position: Int,
        binding: ItemBetRecordEndscoreBinding,
        item: EndScoreInfo,
    ) {
        binding.root.text = item.playName
    }
}

