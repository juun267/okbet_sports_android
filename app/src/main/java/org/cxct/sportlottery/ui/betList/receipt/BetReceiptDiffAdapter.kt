package org.cxct.sportlottery.ui.betList.receipt

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.item_parlay_receipt.view.*
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd

class BetReceiptDiffAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetReceiptCallback()) {

    var refreshBetStatusFunction: ((Long) -> Unit)? = null
    var refreshBetStatusFinishFunction: (() -> Unit)? = null

    var interfaceStatusChangeListener: InterfaceStatusChangeListener? = null

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var betParlayList: List<ParlayOdd>? = null

    private var betConfirmTime: Long? = 0

    enum class ItemType { SINGLE, PARLAY }

    private val mHandler = Handler(Looper.getMainLooper())

    private val mRunnableList: MutableList<Runnable?> by lazy {
        MutableList(itemCount) { _ -> null }
    }

    var items = listOf<DataItem>()


    /**
     * @param betParlayList 投注單的串關清單, 用來表示第一項投注單的賠率
     */
    fun submit(
        singleList: List<BetResult>,
        parlayList: List<BetResult>,
        betParlayList: List<ParlayOdd>,
        betConfirmTime: Long,
        parlayOrSingle: ((ItemType) -> Unit)? = null,
    ) {
        this@BetReceiptDiffAdapter.betParlayList = betParlayList
        this@BetReceiptDiffAdapter.betConfirmTime = betConfirmTime

        val parlayItem = if (parlayList.isNotEmpty()) {
            parlayList.map {
                //標記串關第一項(NC1), 第一項需顯示賠率, 以parlayType做比對, 有投注的第一項不一定是NC1
                parlayOrSingle?.invoke(ItemType.PARLAY)
                DataItem.ParlayData(it)
            }
        } else {
            parlayOrSingle?.invoke(ItemType.SINGLE)
            listOf()
        }

        items = singleList.map { DataItem.SingleData(it) } + parlayItem

        submitList(items)
    }

    private fun starRunnable(
        startTime: Long, adapterPosition: Int, tvTime: TextView
    ) {
        try {
            if (mRunnableList[adapterPosition] == null) {
                mRunnableList[adapterPosition] = getRunnable(startTime, adapterPosition, tvTime)
                mRunnableList[adapterPosition]?.let { mHandler.post(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRunnable(
        startTime: Long,
        position: Int,
        tvTime: TextView,
    ): Runnable {
        return Runnable {
            refreshTime(startTime, position, tvTime)//可以直接倒數了
            mRunnableList[position]?.let { mHandler.postDelayed(it, 1000) }
        }
    }

    private fun refreshTime(
        startTime: Long, position: Int, tvTime: TextView
    ) {
        if (startTime.minus(System.currentTimeMillis()).div(1000L) < 1) {
            refreshBetStatusFinishFunction?.invoke()
        } else {
            refreshBetStatusFunction?.invoke(startTime.minus(System.currentTimeMillis()).div(1000L))
        }
    }

    private fun stopRunnable(position: Int) {
        try {
            if (position < 0 || position >= mRunnableList.size || mRunnableList[position] != null) {
                mRunnableList[position]?.let { mHandler.removeCallbacks(it) }//從執行緒移除
                mRunnableList[position] = null//取消任務
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun removeAllRunnable() {
        runWithCatch { mRunnableList.onEach { it?.let { it1 -> mHandler.removeCallbacks(it1) } } }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.SingleData -> ItemType.SINGLE.ordinal
            is DataItem.ParlayData -> ItemType.PARLAY.ordinal
            else -> ItemType.SINGLE.ordinal
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.SINGLE.ordinal -> SingleViewHolder.from(parent)
            ItemType.PARLAY.ordinal -> ParlayViewHolder.from(parent)
            else -> SingleViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var currentOddsType = oddsType

        when (holder) {
            is SingleViewHolder -> {
                val itemData = getItem(position) as DataItem.SingleData
                holder.bind(
                    betConfirmTime,
                    itemData.result,
                    currentOddsType,
                    interfaceStatusChangeListener,
                    position
                )
                if (itemData.result.status == 0 && (System.currentTimeMillis() < (betConfirmTime
                        ?: 0L))
                ) {
                    starRunnable(
                        betConfirmTime ?: 0,
                        position,
                        holder.itemView.tv_bet_status_single,
                    )
                }else{
                    stopRunnable(position)
                }
            }

            is ParlayViewHolder -> {
                betParlayList?.getOrNull(position)?.let {
                    if (it.odds == it.malayOdds) {
                        currentOddsType = OddsType.EU
                    }
                }
                val itemData = getItem(position) as DataItem.ParlayData
                holder.bind(
                    itemData.result,
                    currentOddsType,
                    betParlayList,
                    interfaceStatusChangeListener,
                    position
                )
                if (itemData.result.status == 0 && (System.currentTimeMillis() < (betConfirmTime
                        ?: 0L))
                ) {
                    starRunnable(
                        betConfirmTime ?: 0,
                        position,
                        holder.itemView.tv_bet_status,
                    )
                }else{
                    stopRunnable(position)
                }
            }
        }
    }


    interface InterfaceStatusChangeListener {
        //取消触发来源 0: 自动, 1: 手动
        fun onChange(cancelBy: String?)
    }
}

class BetReceiptCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.orderNo == newItem.orderNo && oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {

    abstract val orderNo: String?
    abstract val status: Int?

    data class SingleData(val result: BetResult) : DataItem() {
        override val orderNo = result.orderNo
        override val status = result.status
    }

    data class ParlayData(val result: BetResult) : DataItem() {
        override val orderNo = result.orderNo
        override val status = result.status
    }
}
