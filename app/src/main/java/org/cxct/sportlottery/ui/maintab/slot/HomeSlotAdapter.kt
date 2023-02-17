package org.cxct.sportlottery.ui.maintab.slot

import org.cxct.sportlottery.R
import org.cxct.sportlottery.adapter.recyclerview.BindingAdapter
import org.cxct.sportlottery.databinding.ItemHomeSlotBinding
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.load
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData

class HomeSlotAdapter: BindingAdapter<QueryGameEntryData, ItemHomeSlotBinding>() {

    override fun onBinding(position: Int,
                           vb: ItemHomeSlotBinding,
                           item: QueryGameEntryData) = vb.run {

        ivPeople.load(item.entryImage)
        tvFirmName.text = item.firmName
        tvStatus.setText(if (item.gameCode == "TPG") R.string.new_games else R.string.new_games_beta)
        tvGameName.gone()
    }
}
