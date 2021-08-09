package org.cxct.sportlottery.ui.game.betList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.Row
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

class BetReceiptDiffAdapter : ListAdapter<BetResult, RecyclerView.ViewHolder>(BetReceiptCallback()) {
    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MatchReceiptViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = getItem(position)
        when (holder) {
            is MatchReceiptViewHolder -> holder.bind(itemData, oddsType)
        }
    }

    class MatchReceiptViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_receipt, viewGroup, false)
                return MatchReceiptViewHolder(view)
            }
        }

        fun bind(itemData: BetResult, oddsType: OddsType) {
            itemView.apply {
                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        tv_play_name.text = playName
                        tv_match_odd.text = TextUtil.formatForOdd(getOdds(this, oddsType))
                        tv_league.text = leagueName
                        tv_team_home.text = homeName
                        tv_spread.text = spread
                        tv_team_away.text = awayName
                        tv_match_type.text = playCateName
                    }
                    tv_bet_amount.text = stake?.let { TextUtil.formatBetQuota(it) }
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                    tv_winnable_amount.setMoneyFormat(winnable)
                    tv_bet_status.setStatus(status)
                }
            }
        }
    }
}

class BetReceiptCallback : DiffUtil.ItemCallback<BetResult>() {
    override fun areItemsTheSame(oldItem: BetResult, newItem: BetResult): Boolean {
        return oldItem.orderNo == newItem.orderNo
    }

    override fun areContentsTheSame(oldItem: BetResult, newItem: BetResult): Boolean {
        return oldItem == newItem
    }
}