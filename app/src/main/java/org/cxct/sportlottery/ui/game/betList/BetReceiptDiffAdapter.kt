package org.cxct.sportlottery.ui.game.betList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.Row
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setStatus

class BetReceiptDiffAdapter : ListAdapter<Row, RecyclerView.ViewHolder>(BetReceiptCallback()) {
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

        fun bind(itemData: Row, oddsType: OddsType) {
            itemView.apply {
                itemData.apply {
                    matchOdds.firstOrNull()?.apply {
                        tv_play_name.text = playName
                        tv_match_odd.text = TextUtil.formatForOdd(
                            when (oddsType) {
                                OddsType.HK -> hkOdds
                                else -> odds
                            }
                        )
                        tv_league.text = leagueName
                        tv_team_home.text = homeName
                        tv_spread.text = spread
                        tv_team_away.text = awayName
                        tv_match_type.text = playCateName
                    }
                    tv_bet_amount.text = TextUtil.formatBetQuota(stake)
                    tv_order_number.text = if (orderNo.isEmpty()) "-" else orderNo
                    tv_winnable_amount.text = TextUtil.formatMoney(winnable)
                    tv_bet_status.setStatus(status)
                }
            }
        }
    }
}

class BetReceiptCallback : DiffUtil.ItemCallback<Row>() {
    override fun areItemsTheSame(oldItem: Row, newItem: Row): Boolean {
        return oldItem.orderNo == newItem.orderNo
    }

    override fun areContentsTheSame(oldItem: Row, newItem: Row): Boolean {
        return oldItem == newItem
    }
}