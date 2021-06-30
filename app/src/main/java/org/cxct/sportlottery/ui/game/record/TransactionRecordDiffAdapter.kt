package org.cxct.sportlottery.ui.game.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_match_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.util.TextUtil

class TransactionRecordDiffAdapter : ListAdapter<Row, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MatchRecordViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {
            is MatchRecordViewHolder -> {
                holder.bind(rvData)
            }
        }
    }

    class MatchRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_match_record, viewGroup, false)
                return MatchRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = matchOdds.leagueName
                title_home_name.text = matchOdds.homeName
                title_spread.text = matchOdds.spread
                title_away_name.text = matchOdds.awayName

                content_play.text = "${data.gameType} ${matchOdds.playName}"
                spread_name.text = matchOdds.homeName
                content_odds.text = matchOdds.odds.toString() //TODO 根據盤口類型切換 odds hkOdds
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = "${data.addTime} (盤口類型)" //TODO 盤口類型配置
            }
        }
    }
}

class TransactionRecordDiffCallBack : DiffUtil.ItemCallback<Row>() {
    override fun areItemsTheSame(oldItem: Row, newItem: Row): Boolean {
        //TODO review 應使用訂單編號
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Row, newItem: Row): Boolean {
        return oldItem == newItem
    }

}