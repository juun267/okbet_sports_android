package org.cxct.sportlottery.ui.maintab.games

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.util.TextUtil

class OkGameRecordAdapter :
    BaseQuickAdapter<RecordNewEvent, BaseViewHolder>(R.layout.item_okgame_p3_record) {

    private val redColor by lazy { context.resources.getColor(R.color.color_FF2E00) }
    private val greenColor by lazy { context.resources.getColor(R.color.color_1CD219) }

    override fun convert(holder: BaseViewHolder, item: RecordNewEvent) {
        if (item.profitAmount.isNullOrEmpty()) {
            item.profitAmount= "0.00"
        }

        holder.setText(R.id.item_okgame_name, item.games)
        holder.setText(R.id.item_okgame_user_nick, item.player)
        holder.setText(R.id.item_okgame_bet_amount, TextUtil.formatMoney(item.betAmount, 2))
        val tvProfit = holder.getView<TextView>(R.id.item_okgame_profit)
        if (item.profitAmount.startsWith("-")) {
            tvProfit.setTextColor(redColor)
            tvProfit.text = TextUtil.formatMoney(item.profitAmount, 2)
        } else {
            tvProfit.setTextColor(greenColor)
            tvProfit.text = "+" + TextUtil.formatMoney(item.profitAmount, 2)
        }
    }
}