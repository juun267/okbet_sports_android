package org.cxct.sportlottery.ui.maintab.betdetails

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_details.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.transactionStatus.BetListData
import org.cxct.sportlottery.ui.transactionStatus.ParlayType
import org.cxct.sportlottery.util.*

class TransactionRecordDetailAdapter :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    var isLastPage: Boolean = false
    var totalAmount: Double = 0.0
    var itemList = listOf<DataItem>()

    //    private enum class ViewType { Match, Parlay, Outright, LastTotal, NoData }
    private enum class ViewType { Match, Parlay, Outright, NoData }

    fun setupBetList(betListData: BetListData) {
        isLastPage = betListData.isLastPage
        totalAmount = betListData.totalMoney
        itemList = when {
            betListData.row.isEmpty() -> listOf(DataItem.NoData)
//            else -> betListData.row.map { DataItem.Item(it) } + listOf(DataItem.Total(totalAmount))
            else -> betListData.row.map { DataItem.Item(it) }
        }
        submitList(itemList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NoData.ordinal -> NoDataViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {

            is ParlayRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row)
            }

            is NoDataViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            getItem(position).orderNo.isNullOrBlank() && itemCount == 1 -> ViewType.NoData.ordinal
//            position == itemCount - 1 -> ViewType.LastTotal.ordinal
            getItem(position).parlayType == ParlayType.SINGLE.key -> ViewType.Match.ordinal
            getItem(position).parlayType == ParlayType.OUTRIGHT.key -> ViewType.Outright.ordinal
            else -> ViewType.Parlay.ordinal
        }
    }



    class ParlayRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_bet_details, viewGroup, false)
                return ParlayRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val contentParlayMatchAdapter by lazy { ContentParlayDetailAdapter(data.status) }

            itemView.apply {
                ParlayType.getParlayStringRes(data.parlayType)?.let { parlayTypeStringResId ->
                    //盡量避免直接使用 MultiLanguagesApplication.appContext.getString 容易出現語系顯示錯誤
//                    title_parlay_type.text = itemView.context.getString(parlayTypeStringResId)
                    val parlayTitle = context.getString(R.string.bet_record_parlay) +
                            "(${context.getString(parlayTypeStringResId)})" +
                            "-${GameType.getGameTypeString(context, data.gameType)}"
                    title_parlay_type.text = parlayTitle
                }
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(
                        data.gameType,
                        data.matchOdds,
                        data.betConfirmTime,
                        data.matchType
                    )

                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
            }
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.itemview_game_no_record, parent, false)
                )
        }

        fun bind() {
            itemView.apply {
            }
        }
    }

    companion object {
        fun getTimeFormatFromDouble(time: Long): String {
            return TimeUtil.timeFormat(time, TimeUtil.DM_HM_FORMAT)
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
    ) : DataItem()

//    data class Total(val totalAmount: Double) : DataItem() {
//        override var parlayType: String? = null
//        override var orderNo: String? = null
//    }


    object NoData : DataItem() {
        override var parlayType: String? = null
        override var orderNo: String? = null
    }
}