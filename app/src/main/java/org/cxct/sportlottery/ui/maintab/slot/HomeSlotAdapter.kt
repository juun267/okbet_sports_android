package org.cxct.sportlottery.ui.maintab.slot

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeSlotBinding
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import timber.log.Timber

class HomeSlotAdapter: BindingAdapter<QueryGameEntryData, ItemHomeSlotBinding>() {

    override fun onBinding(position: Int,
                           vb: ItemHomeSlotBinding,
                           item: QueryGameEntryData) = vb.run {

        ivPeople.load(item.entryImage)
        tvFirmName.text = item.firmName
        Timber.d("gameCode:${item.gameCode}")
        tvStatus.setText(if (
            item.gameCode == "TPG"  ||
                    item.gameCode == "FKG"
        ) R.string.new_games else R.string.new_games_beta)
        tvGameName.gone()
    }
}
