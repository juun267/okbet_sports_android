package org.cxct.sportlottery.ui.game.betList.receipt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.item_match_receipt.view.tv_match_at
import kotlinx.android.synthetic.main.item_parlay_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

class BetReceiptDiffAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetReceiptCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var betParlayList: List<ParlayOdd>? = null

    enum class ItemType { SINGLE, PARLAY_TITLE, PARLAY }

    /**
     * @param betParlayList 投注單的串關清單, 用來表示第一項投注單的賠率
     */
    fun submit(singleList: List<BetResult>, parlayList: List<BetResult>, betParlayList: List<ParlayOdd>) {
        adapterScope.launch {

            this@BetReceiptDiffAdapter.betParlayList = betParlayList

            val parlayItem =
                if (parlayList.isNotEmpty())
                    listOf(DataItem.ParlayTitle) + parlayList.map {
                        //標記串關第一項(NC1), 第一項需顯示賠率, 以parlayType做比對, 有投注的第一項不一定是NC1
                        if (betParlayList.first().parlayType == it.parlayType) DataItem.ParlayData(
                            it,
                            true
                        ) else DataItem.ParlayData(it)
                    }
                else listOf()

            val items = singleList.map { DataItem.SingleData(it) } + parlayItem

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.SingleData -> ItemType.SINGLE.ordinal
            is DataItem.ParlayData -> ItemType.PARLAY.ordinal
            is DataItem.ParlayTitle -> ItemType.PARLAY_TITLE.ordinal
            else -> ItemType.SINGLE.ordinal
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.SINGLE.ordinal -> SingleViewHolder.from(parent)
            ItemType.PARLAY.ordinal -> ParlayViewHolder.from(parent)
            ItemType.PARLAY_TITLE.ordinal -> ParlayTitleViewHolder.from(parent)
            else -> SingleViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SingleViewHolder -> {
                val itemData = getItem(position) as DataItem.SingleData
                holder.bind(itemData.result, oddsType)
            }

            is ParlayViewHolder -> {
                val itemData = getItem(position) as DataItem.ParlayData
                holder.bind(itemData.result, itemData.firstItem, oddsType, betParlayList)
            }

            is ParlayTitleViewHolder -> {
            }

        }
    }

    class SingleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_receipt, viewGroup, false)
                return SingleViewHolder(view)
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

                    itemView.setBetReceiptBackground(status)
                    tv_bet_amount.setBetReceiptAmount(itemData)
                    tv_winnable_amount.setMoneyFormat(winnable)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                    tv_bet_status.setBetReceiptStatus(status)
                    tv_bet_status.setReceiptStatusColor(status)

                    if (matchType == MatchType.OUTRIGHT) {
                        tv_spread.visibility = View.GONE
                        tv_team_home.visibility = View.GONE
                        tv_verse.visibility = View.GONE
                        tv_team_away.visibility = View.GONE
                    }
                }
            }
        }
    }

    class ParlayViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_parlay_receipt, viewGroup, false)
                return ParlayViewHolder(view)
            }
        }

        fun bind(itemData: BetResult, firstItem: Boolean, oddsType: OddsType, betParlay: List<ParlayOdd>?) {
            itemView.apply {
                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        parlayType?.let { tv_play_name_parlay.text = TextUtil.replaceParlayByC(it) }
                    }

                    tv_match_at.visibility = if (firstItem) View.VISIBLE else View.GONE
                    tv_match_odd_parlay.apply {
                        visibility = if (firstItem) View.VISIBLE else View.GONE
                        text = (betParlay?.getOrNull(0)
                            ?.let { parlayOdd -> TextUtil.formatForOdd(getOdds(parlayOdd, oddsType)) }) ?: "0.0"
                    }

                    itemView.setBetReceiptBackground(status)
                    tv_bet_amount.setBetParlayReceiptAmount(itemData)
                    tv_winnable_amount.setMoneyFormat(winnable)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                    tv_bet_status.setBetReceiptStatus(status)
                    tv_bet_status.setReceiptStatusColor(status)
                }
            }
        }
    }

    class ParlayTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view =
                    layoutInflater.inflate(R.layout.item_parlay_receipt_title, viewGroup, false)
                return ParlayTitleViewHolder(view)
            }
        }
    }


}

class BetReceiptCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.orderNo == newItem.orderNo
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {

    abstract val orderNo: String?

    data class SingleData(val result: BetResult) : DataItem() {
        override val orderNo = result.orderNo
    }

    data class ParlayData(val result: BetResult, val firstItem: Boolean = false) : DataItem() {
        override val orderNo = result.orderNo
    }

    object ParlayTitle : DataItem() {
        override val orderNo: String = ""
    }

}