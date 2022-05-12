package org.cxct.sportlottery.ui.game.betList.receipt

import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.item_parlay_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*
import java.util.*

class BetReceiptDiffAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetReceiptCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var interfaceStatusChangeListener:InterfaceStatusChangeListener? = null

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var betParlayList: List<ParlayOdd>? = null

    var betConfirmTime: Long? = 0

    enum class ItemType { SINGLE, PARLAY_TITLE, PARLAY }

    val mHandler = Handler(Looper.getMainLooper())

    val mRunnableList: MutableList<Runnable?> by lazy {
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
        betConfirmTime: Long
    ) {
        adapterScope.launch {

            this@BetReceiptDiffAdapter.betParlayList = betParlayList
            this@BetReceiptDiffAdapter.betConfirmTime = betConfirmTime

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

            items = singleList.map { DataItem.SingleData(it) } + parlayItem

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun updateListStatus(sportBet: SportBet) {
        items.forEach { dataItem ->
            if(dataItem.orderNo == sportBet.orderNo)
                (dataItem as DataItem.SingleData).result.status = sportBet.status
        }
        submitList(items)
    }

     private fun starRunnable(startTime: Long, adapterPosition: Int, tvTime: TextView) {
        try {
            if (mRunnableList[adapterPosition] == null) {
                mRunnableList[adapterPosition] = getRunnable(startTime, adapterPosition, tvTime)
                mRunnableList[adapterPosition]?.let { mHandler.post(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     private fun getRunnable(startTime: Long, position: Int, tvTime: TextView): Runnable {
        return Runnable {
            refreshTime(startTime, position, tvTime)//可以直接倒數了
            mRunnableList[position]?.let { mHandler.postDelayed(it, 1000) }
        }
    }

    private fun refreshTime(startTime: Long, position: Int, tvTime: TextView) {
        if (startTime.minus(System.currentTimeMillis()).div(1000L) < 0) {
            tvTime.text = String.format(
                tvTime.context.getString(R.string.pending),
                0
            )
            stopRunnable(position)
        } else {
            tvTime.text = String.format(
                tvTime.context.getString(R.string.pending),
                startTime.minus(System.currentTimeMillis()).div(1000L)
            )
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

        var currentOddsType = oddsType

        if (betParlayList?.getOrNull(position)?.odds == betParlayList?.getOrNull(position)?.malayOdds) {
            currentOddsType = OddsType.EU
        }

        when (holder) {
            is SingleViewHolder -> {
                val itemData = getItem(position) as DataItem.SingleData
                holder.bind(itemData.result, currentOddsType, interfaceStatusChangeListener)
                if (itemData.result.status == 0) {
                    starRunnable(betConfirmTime ?: 0, position, holder.itemView.tv_bet_status)
                }
            }

            is ParlayViewHolder -> {
                val itemData = getItem(position) as DataItem.ParlayData
                holder.bind(itemData.result, itemData.firstItem, currentOddsType, betParlayList,interfaceStatusChangeListener)
                if (itemData.result.status == 0) {
                    starRunnable(betConfirmTime ?: 0, position, holder.itemView.tv_bet_status)
                }
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

        fun bind(itemData: BetResult, oddsType: OddsType,  interfaceStatusChangeListener:InterfaceStatusChangeListener?) {
            itemView.apply {
                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        tv_play_content.text = setSpannedString(
                            needShowSpread(playCateCode) && (matchType != MatchType.OUTRIGHT),
                            playName,
                            if (matchType != MatchType.OUTRIGHT) spread else "",
                            TextUtil.formatForOdd(getOdds(this, oddsType)),
                            context.getString(getOddTypeRes(this, oddsType))
                        )

                        tv_league.text = leagueName
                        val teamNamesStr = if (homeName?.length ?: 0 > 15) "$homeName v\n$awayName" else "$homeName v $awayName"
                        tv_team_names.text = teamNamesStr
                        tv_match_type.tranByPlayCode(playCode, playCateName)
                    }

                    itemView.setBetReceiptBackground(status)
                    tv_bet_amount.text = TextUtil.formatMoney(itemData.stake ?: 0.0)
                    tv_winnable_amount.text = TextUtil.formatMoney(winnable ?: 0.0)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo

                    if (status != 0)
                        tv_bet_status.setBetReceiptStatus(status)

                    if (status == 7)
                        interfaceStatusChangeListener?.onChange()

                    tv_bet_status.setReceiptStatusColor(status)

                    if (matchType == MatchType.OUTRIGHT) {
                        tv_team_names.visibility = View.GONE
                    }
                }
            }
        }

        private fun setSpannedString(
            isShowSpread: Boolean,
            playName: String?,
            spread: String?,
            formatForOdd: String,
            oddsType: String
        ): Spanned {
            val color_e5e5e5_333333 = MultiLanguagesApplication.getChangeModeColorCode("#333333", "#e5e5e5")
            val color_F75452_b73a20 = MultiLanguagesApplication.getChangeModeColorCode("#B73A20", "#F75452")

            val playNameStr = if (!playName.isNullOrEmpty()) "<font color=$color_e5e5e5_333333>$playName</font> " else ""
            val spreadStr = if (!spread.isNullOrEmpty() || isShowSpread) "<font color=$color_F75452_b73a20>$spread</font> " else ""

            return HtmlCompat.fromHtml(
                playNameStr +
                        spreadStr +
                        "<font color=$color_e5e5e5_333333>@ $formatForOdd</font> ", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
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

        fun bind(
            itemData: BetResult,
            firstItem: Boolean,
            oddsType: OddsType,
            betParlay: List<ParlayOdd>?,
            interfaceStatusChangeListener:InterfaceStatusChangeListener?
        ) {
            itemView.apply {
                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        parlayType?.let { parlayTypeCode ->
                            tv_play_name_parlay.text =
                                getParlayStringRes(parlayTypeCode)?.let { context.getString(it) }
                                    ?: ""
                        }
                    }

                    tv_match_at.visibility = if (firstItem) View.VISIBLE else View.GONE
                    tv_match_odd_parlay.apply {
                        visibility = if (firstItem) View.VISIBLE else View.GONE
                        text = (betParlay?.getOrNull(0)
                            ?.let { parlayOdd ->
                                TextUtil.formatForOdd(
                                    getOdds(
                                        parlayOdd,
                                        oddsType
                                    )
                                )
                            }) ?: "0.0"
                    }

                    itemView.setBetReceiptBackground(status)
                    tv_bet_amount.setBetParlayReceiptAmount(
                        itemData,
                        betParlay?.find { parlayType == it.parlayType }?.num ?: 0
                    )
                    tv_winnable_amount.text = TextUtil.formatMoney(winnable ?: 0.0)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                    if (status != 0)
                        tv_bet_status.setBetReceiptStatus(status)

                    if (status == 7)
                        interfaceStatusChangeListener?.onChange()

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

    interface InterfaceStatusChangeListener {
        fun onChange()
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