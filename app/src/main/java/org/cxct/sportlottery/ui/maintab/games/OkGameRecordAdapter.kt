package org.cxct.sportlottery.ui.maintab.games

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.record.RecordNewEvent

class OkGameRecordAdapter :
    BaseQuickAdapter<RecordNewEvent, BaseViewHolder>(R.layout.item_okgame_p3_record) {


    override fun convert(holder: BaseViewHolder, item: RecordNewEvent) {
        holder.setText(R.id.item_okgame_name, item.games)
        holder.setText(R.id.item_okgame_user_nick, item.player)
        holder.setText(R.id.item_okgame_bet_amount, item.betAmount)
        var tvProfit = holder.getView<TextView>(R.id.item_okgame_profit)
        tvProfit.text = item.profitAmount

        tvProfit.setTextColor(if (item.profitAmount.startsWith("+")) {
             context.resources.getColor(R.color.color_1CD219)
        }else{
            context.resources.getColor(R.color.color_FF2E00)
        })
    }
}