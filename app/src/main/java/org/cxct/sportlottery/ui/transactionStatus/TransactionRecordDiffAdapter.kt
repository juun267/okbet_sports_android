package org.cxct.sportlottery.ui.transactionStatus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_last_total_record.view.*
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.content_outright_record.view.content_bet_amount
import kotlinx.android.synthetic.main.content_outright_record.view.content_odds
import kotlinx.android.synthetic.main.content_outright_record.view.content_order_no
import kotlinx.android.synthetic.main.content_outright_record.view.content_play
import kotlinx.android.synthetic.main.content_outright_record.view.content_time_type
import kotlinx.android.synthetic.main.content_outright_record.view.content_winnable_amount
import kotlinx.android.synthetic.main.content_outright_record.view.spread_name
import kotlinx.android.synthetic.main.content_outright_record.view.title_league_name
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

//TODO 20210719當前api缺少總金額,待後端修正後進行確認
class TransactionRecordDiffAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    var isLastPage: Boolean = false
    var totalAmount: Long = 0
    var oddsType: OddsType = OddsType.EU

    private enum class ViewType { Match, Parlay, Outright, LastTotal, NoData }

    fun setupBetList(betListData: BetListData) {
        isLastPage = betListData.isLastPage
        oddsType = betListData.oddsType
        totalAmount = betListData.totalMoney
        val itemList = when {
            betListData.row.isEmpty() -> listOf(DataItem.NoData)
            else -> betListData.row.map { DataItem.Item(it) } + listOf(DataItem.Total(totalAmount.toString()))
        }
        submitList(itemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NoData.ordinal -> NoDataViewHolder.from(parent)
            ViewType.Match.ordinal -> MatchRecordViewHolder.from(parent)
            ViewType.Outright.ordinal -> OutrightRecordViewHolder.from(parent)
            ViewType.LastTotal.ordinal -> LastTotalViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {
            is MatchRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, oddsType)
            }
            is ParlayRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, oddsType)
            }
            is OutrightRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, oddsType)
            }
            is LastTotalViewHolder -> {
                holder.bind((rvData as DataItem.Total).totalAmount)
            }
            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            getItem(position).orderNo.isNullOrBlank() && itemCount == 1 -> ViewType.NoData.ordinal
            position == itemCount - 1 -> ViewType.LastTotal.ordinal
            getItem(position).parlayType == ParlayType.SINGLE.key -> ViewType.Match.ordinal
            getItem(position).parlayType == ParlayType.OUTRIGHT.key -> ViewType.Outright.ordinal
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

        fun bind(data: Row, oddsType: OddsType) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = matchOdds.leagueName
                title_home_name.text = matchOdds.homeName
                title_spread.text = matchOdds.spread
                title_away_name.text = matchOdds.awayName

                content_play.text = "${getGameTypeName(data.gameType)} ${matchOdds.playName}"
                spread_name.text = matchOdds.homeName
                content_odds.text = when (oddsType) {
                    OddsType.HK -> matchOdds.hkOdds
                    else -> matchOdds.odds
                }.toString()
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

    }

    class OutrightRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_outright_record, viewGroup, false)
                return OutrightRecordViewHolder(view)
            }
        }

        fun bind(data: Row, oddsType: OddsType) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = "${matchOdds.leagueName} - ${context.getString(R.string.champion)}"

                content_play.text = "${getGameTypeName(data.gameType)} ${matchOdds.playCateName}"
                spread_name.text = matchOdds.spread
                content_odds.text = when (oddsType) {
                    OddsType.HK -> matchOdds.hkOdds
                    else -> matchOdds.odds
                }.toString()
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

    }

    class LastTotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_last_total_record, viewGroup, false)
                return LastTotalViewHolder(view)
            }
        }

        fun bind(totalAmount: String) {
            itemView.apply {
                last_total_amount.text = "$totalAmount ${context.getString(R.string.currency)}"
            }
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

        fun bind(data: Row, oddsType: OddsType) {
            val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter() }

            itemView.apply {
                title_parlay_type.text = getParlayShowName(data.parlayType)
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(getGameTypeName(data.gameType), oddsType, data.matchOdds)

                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
                content_parlay_winnable_amount.text = TextUtil.format(data.winnable)
                content_parlay_order_no.text = data.orderNo
                content_parlay_time_type.text = getTimeFormatFromDouble(data.addTime)
            }
        }

        private fun getParlayShowName(parlayType: String): String {
            return parlayType.replace("C", " ${itemView.context.getString(R.string.conspire)} ")
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false))
        }
    }

    companion object {
        fun getTimeFormatFromDouble(time: Long): String {
            return TimeUtil.timeFormat(time, TimeUtil.MD_HMS_FORMAT)
        }
    }
}

class TransactionRecordDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.orderNo == newItem.orderNo
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}

sealed class DataItem {
    abstract var parlayType: String?
    abstract var orderNo: String?

    data class Item(
        val row: Row,
        override var parlayType: String? = row.parlayType,
        override var orderNo: String? = row.orderNo
    ) :
        DataItem()

    data class Total(val totalAmount: String) : DataItem() {
        override var parlayType: String? = null
        override var orderNo: String? = null
    }


    object NoData : DataItem() {
        override var parlayType: String? = null
        override var orderNo: String? = null
    }
}