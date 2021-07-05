package org.cxct.sportlottery.ui.transactionStatus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.results.GameType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
//TODO 冠軍應有獨立樣式，等待新API完成後再根據資料做新UI
class TransactionRecordDiffAdapter : ListAdapter<Row, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    var isLastPage: Boolean = false
    var totalAmount: Long = 0

    private enum class ViewType { Match, Parlay }

    fun setupBetList(betListData: BetListData) {
        isLastPage = betListData.isLastPage
        submitList(betListData.row)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.Match.ordinal -> MatchRecordViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {
            is MatchRecordViewHolder -> {
                holder.bind(rvData)
            }
            is ParlayRecordViewHolder -> {
                holder.bind(rvData)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).parlayType) {
            "1C1", "OUTRIGHT" -> ViewType.Match.ordinal
            else -> ViewType.Parlay.ordinal
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

                content_play.text = "${getGameTypeName(data.gameType)} ${matchOdds.playName}"
                spread_name.text = matchOdds.homeName
                content_odds.text = matchOdds.odds.toString() //TODO 根據盤口類型切換 odds hkOdds
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = "${getTimeFormatFromDouble(data.addTime)} (盤口類型)" //TODO 盤口類型配置
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

    }

    class ParlayRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_parlay_record, viewGroup, false)
                return ParlayRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter() }

            itemView.apply {
                title_parlay_type.text = getParlayShowName(data.parlayType)
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(getGameTypeName(data.gameType), data.matchOdds)

                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
                content_parlay_winnable_amount.text = TextUtil.format(data.winnable)
                content_parlay_order_no.text = data.orderNo
                content_parlay_time_type.text = "${getTimeFormatFromDouble(data.addTime)} (盤口類型)" //TODO 盤口類型配置
            }
        }

        private fun getParlayShowName(parlayType: String): String {
            return parlayType.replace("C", " ${itemView.context.getString(R.string.conspire)} ")
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }
    }

    companion object {
        fun getTimeFormatFromDouble(time: Long): String {
            return TimeUtil.timeFormat(time, TimeUtil.MD_HMS_FORMAT)
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

