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

    fun submit(singleList: List<BetResult>, parlayList: List<BetResult>, newBetParlayList: List<ParlayOdd>) {
        adapterScope.launch {

            val parlayItem =
                if (parlayList.isNotEmpty())
                    listOf(DataItem.ParlayTitle) + parlayList.map { DataItem.ParlayData(it) }
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
                holder.bind(itemData.result, oddsType, betParlayList)
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

        fun bind(itemData: BetResult, oddsType: OddsType, betParlay: List<ParlayOdd>?) {
            itemView.apply {
                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        parlayType?.let { tv_play_name_parlay.text = TextUtil.replaceParlayByC(it) }
                    }
                    tv_bet_amount.text = stake?.let { "${TextUtil.formatBetQuota(it)} * $num" }
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

    data class ParlayData(val result: BetResult) : DataItem() {
        override val orderNo = result.orderNo
    }

    object ParlayTitle : DataItem() {
        override val orderNo: String = ""
    }

}