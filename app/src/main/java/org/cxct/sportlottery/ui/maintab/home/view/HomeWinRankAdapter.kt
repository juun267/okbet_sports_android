package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemHomeWinRankBinding
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.util.TextUtil

class HomeWinRankAdapter : BindingAdapter<RecordNewEvent,ItemHomeWinRankBinding>() {

    private val redColor by lazy { context.resources.getColor(R.color.color_F23C3B) }
    private val greenColor by lazy { context.resources.getColor(R.color.color_1CD219) }

    override fun onBinding(position: Int, binding: ItemHomeWinRankBinding, item: RecordNewEvent) =binding.run{
        if (item.profitAmount.isNullOrEmpty()) {
            item.profitAmount= "0.00"
        }
        tvNickName.text = item.player
        tvGameName.text = item.games
        tvBetAmount.text = TextUtil.formatMoney(item.betAmount, 2)
        if (item.profitAmount.startsWith("-")) {
            tvProfit.setTextColor(redColor)
            tvProfit.text = TextUtil.formatMoney(item.profitAmount, 2)
        } else {
            tvProfit.setTextColor(greenColor)
            tvProfit.text = "+" + TextUtil.formatMoney(item.profitAmount, 2)
        }
    }
}