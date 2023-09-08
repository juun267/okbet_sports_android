package org.cxct.sportlottery.ui.maintab.menu.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBalanceGameBinding
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TextUtil

class GameBalanceAdapter : BindingAdapter<GameData, ItemBalanceGameBinding>()  {

    override fun onBinding(position: Int, binding: ItemBalanceGameBinding, item: GameData) {
        binding.tvName.text = item.showName
        binding.tvAmount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(item.money?:0)}"
    }

}