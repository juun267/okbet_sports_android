package org.cxct.sportlottery.ui.game.betList.receipt

import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*

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

    enum class ItemType { SINGLE, PARLAY }

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
                    parlayList.map {
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
            tvTime.text = String.format(tvTime.context.getString(R.string.pending), 0)
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

        if (betParlayList?.getOrNull(position)?.odds == betParlayList?.getOrNull(position)?.malayOdds) {
            currentOddsType = OddsType.EU
        }

        when (holder) {
            is SingleViewHolder -> {
                val itemData = getItem(position) as DataItem.SingleData
                holder.bind(itemData.result, currentOddsType, interfaceStatusChangeListener, position)
                if (itemData.result.status == 0) {
                    starRunnable(betConfirmTime ?: 0, position, holder.itemView.tv_bet_status_single)
                }
            }

            is ParlayViewHolder -> {
                val itemData = getItem(position) as DataItem.ParlayData
                holder.bind(itemData.result, itemData.firstItem, currentOddsType, betParlayList,interfaceStatusChangeListener, position)
                if (itemData.result.status == 0) {
                    starRunnable(betConfirmTime ?: 0, position, holder.itemView.tv_bet_status)
                }
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

        fun bind(itemData: BetResult, oddsType: OddsType,  interfaceStatusChangeListener:InterfaceStatusChangeListener?, position: Int) {
            itemView.apply {
                top_space.visibility = if (position == 0) View.VISIBLE else View.GONE

                val currencySign = sConfigData?.systemCurrencySign
                tv_winnable_amount_title.text = context.getString(R.string.bet_receipt_win_quota_with_sign, currencySign)
                tv_bet_amount_title.text = context.getString(R.string.bet_receipt_bet_quota_with_sign, currencySign)

                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        val formatForOdd = if(this.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(getOdds(this, oddsType ?: OddsType.EU) - 1) else TextUtil.formatForOdd(getOdds(this, oddsType))
                        tv_play_content.text = setSpannedString(
                            needShowSpread(playCateCode) && (matchType != MatchType.OUTRIGHT),
                            playName,
                            if (matchType != MatchType.OUTRIGHT) spread else "",
                            formatForOdd,
                            context.getString(getOddTypeRes(this, oddsType))
                        )
                        tv_odds.text = "@ $formatForOdd"

                        tv_league.text = leagueName
                        tv_team_names.setTeamNames(15, homeName, awayName)
                        tv_match_type.tranByPlayCode(playCode, playCateCode, playCateName, rtScore)
                    }

                    tv_bet_amount.text = TextUtil.formatForOdd(itemData.stake ?: 0.0)
                    tv_winnable_amount.text = TextUtil.formatForOdd(winnable ?: 0.0)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo

                    if (status != 0)
                        tv_bet_status_single.setBetReceiptStatus(status)

                    //"status": 7 顯示賠率已改變
                    if (status == 7)
                        interfaceStatusChangeListener?.onChange("")

                    tv_bet_status_single.setReceiptStatusColor(status)

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
            val color_FFFFFF_414655 = MultiLanguagesApplication.getChangeModeColorCode("#414655", "#FFFFFF")
//            val color_e5e5e5_333333 = MultiLanguagesApplication.getChangeModeColorCode("#333333", "#e5e5e5")
//            val color_F75452_b73a20 = MultiLanguagesApplication.getChangeModeColorCode("#B73A20", "#F75452")

            val playNameStr = if (!playName.isNullOrEmpty()) "<font color=$color_FFFFFF_414655>$playName</font> " else ""
            val spreadStr = if (!spread.isNullOrEmpty() || isShowSpread) "<font color=$color_FFFFFF_414655>$spread</font> " else ""

//            return HtmlCompat.fromHtml(
//                playNameStr +
//                        spreadStr +
//                        "<font color=$color_e5e5e5_333333>@ $formatForOdd</font> ", HtmlCompat.FROM_HTML_MODE_LEGACY
//            )
            return HtmlCompat.fromHtml(playNameStr + spreadStr, HtmlCompat.FROM_HTML_MODE_LEGACY)
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
            interfaceStatusChangeListener:InterfaceStatusChangeListener?,
            position: Int
        ) {
            itemView.apply {

                val currencySign = sConfigData?.systemCurrencySign
                tv_winnable_amount_title.text = context.getString(R.string.bet_receipt_win_quota_with_sign, currencySign)
                tv_bet_amount_title.text = context.getString(R.string.bet_receipt_bet_quota_with_sign, currencySign)

                itemData.apply {
                    matchOdds?.firstOrNull()?.apply {
                        parlayType?.let { parlayTypeCode ->
                            tv_play_name_parlay.text =
                                getParlayStringRes(parlayTypeCode)?.let { context.getString(it) }
                                    ?: ""
                        }
                    }
                    tv_multiplier.text = " x ${betParlay?.find { parlayType == it.parlayType }?.num ?: 1}"

                    if (position == 0) { //僅有第一項item的上方，要顯示組合成串關的資料(matchOdds)
                        rv_show_singles_list.isVisible = true
                        rv_show_singles_list.apply {
                            layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            adapter = BetReceiptDiffForParlayShowSingleAdapter().apply {
                                submit(
                                    matchOdds ?: listOf(),
                                    betParlayList ?: listOf(),
                                    matchType
                                )
                            }
                        }
                    } else {
                        rv_show_singles_list.isVisible = false
                    }

                    //不顯示 "@ 組合賠率"
//                    tv_match_at.visibility = if (firstItem) View.VISIBLE else View.GONE
//                    tv_match_odd_parlay.apply {
//                        visibility = if (firstItem) View.VISIBLE else View.GONE
//                        text = (betParlay?.getOrNull(0)
//                            ?.let { parlayOdd ->
//                                TextUtil.formatForOdd(
//                                    getOdds(
//                                        parlayOdd,
//                                        oddsType
//                                    )
//                                )
//                            }) ?: "0.0"
//                    }

                    tv_bet_amount.setBetParlayReceiptAmount(
                        itemData,
                        betParlay?.find { parlayType == it.parlayType }?.num ?: 0
                    )
                    tv_winnable_amount.text = TextUtil.formatForOdd(winnable ?: 0.0)
                    tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                    if (status != 0)
                        tv_bet_status.setBetReceiptStatus(status)

                    //"status": 7 顯示賠率已改變
                    if (status == 7)
                        interfaceStatusChangeListener?.onChange("")

                    tv_bet_status.setReceiptStatusColor(status)
                }
            }
        }
    }

    interface InterfaceStatusChangeListener {
        //取消触发来源 0: 自动, 1: 手动
        fun onChange(cancelBy: String)
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

    data class ParlayData(val result: BetResult, val firstItem: Boolean = false) : DataItem() {
        override val orderNo = result.orderNo
        override val status = result.status
    }
}
