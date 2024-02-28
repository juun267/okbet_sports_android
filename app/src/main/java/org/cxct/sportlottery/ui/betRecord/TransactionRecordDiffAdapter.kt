package org.cxct.sportlottery.ui.betRecord

import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import kotlinx.android.synthetic.main.include_bet_record_endscore.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.ui.betRecord.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScorePlatCateName
import org.cxct.sportlottery.view.onClick

class TransactionRecordDiffAdapter(val viewModel: AccountHistoryViewModel) :
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
            ViewType.Match.ordinal -> MatchRecordViewHolder.from(parent)
            ViewType.Outright.ordinal -> OutrightRecordViewHolder.from(parent)
//            ViewType.LastTotal.ordinal -> LastTotalViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {
            is MatchRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, viewModel)
            }

            is ParlayRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, viewModel)
            }

            is OutrightRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, viewModel)
            }
//            is LastTotalViewHolder -> {
//                holder.bind((rvData as DataItem.Total).totalAmount)
//            }
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

    class MatchRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_match_record, viewGroup, false)
                view.findViewById<TextView>(R.id.content_play)
                    .setCompoundDrawablesRelative(null, null, null, null)
                return MatchRecordViewHolder(view)
            }
        }

        fun bind(data: Row, viewModel: AccountHistoryViewModel) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                itemView.iv_country.setLeagueLogo(matchOdds.categoryIcon)
                title_league_name.text = matchOdds.leagueName.replace("\n", "")
                title_team_name.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)

                //篮球 滚球 全场让分【欧洲盘】
                content_play.setGameType_MatchType_PlayCateName_OddsType(
                    data.gameType, data.matchType, matchOdds.playCateName, matchOdds.oddsType
                )

                tvPrint.visible()

                tvPrint.setOnClickListener {
                    val dialog = PrintDialog(context)
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.observerRemarkBetLiveData {
                                //uniqNo=B0d7593ed42d8840ec9a56f5530e09773c&addTime=1681790156872
                                dialog.dismiss()
                                val newUrl =
                                    Constants.getPrintReceipt(
                                        context,
                                        it.remarkBetResult?.uniqNo,
                                        orderTime.toString(),
                                        it1
                                    )
                                JumpUtil.toExternalWeb(context, newUrl)
                            }
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }

                val formatForOdd =
                    if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                        matchOdds.odds - 1
                    ) else TextUtil.formatForOdd(matchOdds.odds)

                if (matchOdds.playCateCode.isEndScoreType())
                    play_content.setPlayContent(
                        matchOdds.playCateCode.getEndScorePlatCateName(context),
                        matchOdds.spread,
                        formatForOdd
                    )
                else
                    play_content.setPlayContent(
                        matchOdds.playName, matchOdds.spread, formatForOdd
                    )

                match_play_time.text =
                    TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
                if (data.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (data.betConfirmTime
                        ?: 0L)
                ) {
                    val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                    object : CountDownTimer(leftTime ?: 0, 1000) {

                        override fun onTick(millisUntilFinished: Long) {
                            tv_bet_result.text = String.format(
                                context.getString(R.string.pending),
                                TimeUtil.longToSecond(millisUntilFinished)
                            )
                            tvPrint.gone()
                        }

                        override fun onFinish() {
                            tv_bet_result.setBetReceiptStatus(data.status)
                            tvPrint.visible()
                        }
                    }.start()
                } else {
                    tv_bet_result.visibility = View.GONE
                }

                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)
                when (data.gameType) {
                    GameType.FT.key, GameType.BK.key -> {
                        if (matchOdds.rtScore?.isNotEmpty() == true) tv_score.text =
                            "(${matchOdds.rtScore})"
                    }
                }

                val singleTitle = context.getString(R.string.bet_record_single) + "-${
                    GameType.getGameTypeString(
                        context, data.gameType
                    )
                }"
                tv_match_title.text = singleTitle

                if (data.status != 0) tv_bet_result.setBetReceiptStatus(
                    data.status, data.cancelledBy
                )
                tv_bet_result.isVisible = data.status != 7

                ll_copy_bet_order.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
                lin_endscore.isVisible =
                    data.matchOdds.firstOrNull()?.playCateCode.isEndScoreType()
                if (lin_endscore.isVisible) {
                    val sortList = data.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode }
                        ?: listOf()
                    content_play.setCompoundDrawablesWithIntrinsicBounds(null,
                        null,
                        context.getDrawable(R.drawable.ic_right_arrow_gray),
                        null)
                    itemView.onClick {
                        val intent = Intent(context, BetDetailsActivity::class.java)
                        intent.putExtra("data", data)
                        context?.startActivity(intent)
                    }
                    val listData = if (sortList.size > 6) {
                        sortList.subList(0, 6)
                    } else {
                        sortList
                    }
                    if (rv_endscore_info.adapter == null) {
                        rv_endscore_info.layoutManager =
                            LinearLayoutManager(rv_endscore_info.context,
                                RecyclerView.HORIZONTAL,
                                false)
                        rv_endscore_info.addItemDecoration(SpaceItemDecoration(context,
                            R.dimen.margin_4))
                        val scoreAdapter = BetRecordEndScoreAdapter()
                        rv_endscore_info.adapter = scoreAdapter
                        scoreAdapter.setList(listData)
                    } else {
                        (rv_endscore_info.adapter as BetRecordEndScoreAdapter).setList(listData)
                    }
                    tv_more?.let {
                        tv_more.isVisible = sortList.size > 6
                    }
                }
            }
        }

    }

    class OutrightRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_match_record, viewGroup, false)
                view.findViewById<TextView>(R.id.content_play)
                    .setCompoundDrawablesRelative(null, null, null, null)
                return OutrightRecordViewHolder(view)
            }
        }

        fun bind(data: Row, viewModel: AccountHistoryViewModel) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                itemView.iv_country.setLeagueLogo(matchOdds.categoryIcon)
//                title_league_name.text = "${matchOdds.leagueName} - ${matchOdds.playCateName}"
                title_league_name.text = matchOdds.leagueName
                title_team_name.text = matchOdds.leagueName

                //篮球 滚球 全场让分【欧洲盘】
                content_play.setGameType_MatchType_PlayCateName_OddsType(
                    data.gameType, data.matchType, matchOdds.playCateName, matchOdds.oddsType
                )
                tvPrint.visible()

                tvPrint.setOnClickListener {
                    val dialog = PrintDialog(context)
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.observerRemarkBetLiveData {
                                //uniqNo=B0d7593ed42d8840ec9a56f5530e09773c&addTime=1681790156872
                                dialog.dismiss()
                                val newUrl =
                                    Constants.getPrintReceipt(
                                        context,
                                        it.remarkBetResult?.uniqNo,
                                        orderTime.toString(),
                                        it1
                                    )
                                JumpUtil.toExternalWeb(context, newUrl)
                            }
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }

                val formatForOdd =
                    if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                        matchOdds.odds - 1
                    ) else TextUtil.formatForOdd(matchOdds.odds)

                val playName =
                    if (matchOdds.playCateCode.isEndScoreType())
                        matchOdds.playCateCode.getEndScorePlatCateName(context)
                    else matchOdds.playName
                play_content.setPlayContent(
                    playName, matchOdds.spread, formatForOdd
                )
                matchOdds.startTime?.let {
                    match_play_time.text = TimeUtil.timeFormat(it, TimeUtil.DM_HM_FORMAT)
                }
                match_play_time.isVisible = data.parlayType != ParlayType.OUTRIGHT.key
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)

                val singleTitle = context.getString(R.string.bet_record_single) + "-${
                    GameType.getGameTypeString(
                        context, data.gameType
                    )
                }"
                tv_match_title.text = singleTitle

                tv_bet_result.setBetReceiptStatus(data.status, data.cancelledBy)
                tv_bet_result.isVisible = data.status != 7

                ll_copy_bet_order.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
            }
        }
    }



//    class LastTotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        companion object {
//            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
//                val layoutInflater = LayoutInflater.from(viewGroup.context)
//                val view =
//                    layoutInflater.inflate(R.layout.content_last_total_record, viewGroup, false)
//                return LastTotalViewHolder(view)
//            }
//        }
//
//        fun bind(totalAmount: Double) {
//            itemView.apply {
//                last_total_amount.text =
//                    "${sConfigData?.systemCurrencySign} ${TextUtil.format(totalAmount)}"
//            }
//        }
//    }

    class ParlayRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_parlay_record, viewGroup, false)
                return ParlayRecordViewHolder(view)
            }
        }

        fun bind(data: Row, viewModel: AccountHistoryViewModel) {
            val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter(data, viewModel) }

            itemView.apply {

                getParlayStringRes(data.parlayType)?.let { parlayTypeStringResId ->
                    //盡量避免直接使用 MultiLanguagesApplication.appContext.getString 容易出現語系顯示錯誤
//                    title_parlay_type.text = itemView.context.getString(parlayTypeStringResId)
                    val parlayTitle = context.getString(R.string.bet_record_parlay) + "(${
                        context.getString(parlayTypeStringResId)
                    })" + "-${GameType.getGameTypeString(context, data.gameType)}"
                    title_parlay_type.text = parlayTitle
                }
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(
                        data.gameType, data.matchOdds, data.betConfirmTime, data.matchType
                    )

                }
                tvParlayPrint.visible()

                tvParlayPrint.setOnClickListener {
                    val dialog = PrintDialog(context)
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.observerRemarkBetLiveData {
                                //uniqNo=B0d7593ed42d8840ec9a56f5530e09773c&addTime=1681790156872
                                dialog.dismiss()
                                val newUrl =
                                    Constants.getPrintReceipt(
                                        context,
                                        it.remarkBetResult?.uniqNo,
                                        orderTime.toString(),
                                        it1
                                    )
                                JumpUtil.toExternalWeb(context, newUrl)
                            }
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
                content_parlay_winnable_amount.text = TextUtil.format(data.winnable)
                content_parlay_order_no.text = data.orderNo
                content_parlay_time_type.text = getTimeFormatFromDouble(data.addTime)

                if (data.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (data.betConfirmTime
                        ?: 0L)
                ) {
                    val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                    object : CountDownTimer(leftTime ?: 0, 1000) {

                        override fun onTick(millisUntilFinished: Long) {
                            tv_bet_result_parlay.text = String.format(
                                context.getString(R.string.pending),
                                TimeUtil.longToSecond(millisUntilFinished)
                            )
                        }

                        override fun onFinish() {
                            tv_bet_result_parlay.setBetReceiptStatus(data.status)
                        }
                    }.start()
                } else {
                    tv_bet_result_parlay.visibility = View.GONE
                }

                if (data.status != 0) tv_bet_result_parlay.setBetReceiptStatus(
                    data.status, data.cancelledBy
                )
                tv_bet_result_parlay.isVisible = data.status != 7

                ll_copy_bet_order_parlay.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
            }
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.itemview_game_no_record, parent, false)
            )
        }

        fun bind() {
            itemView.apply {
//                list_no_record_img.apply {
//                    viewTreeObserver.addOnGlobalLayoutListener {
//                        val lp = layoutParams as ConstraintLayout.LayoutParams
//                        lp.topMargin = 30.dp
//                        layoutParams = lp
//                    }
//                }
//                list_no_record_text.apply {
//                    viewTreeObserver.addOnGlobalLayoutListener {
//                        val lp = layoutParams as ConstraintLayout.LayoutParams
//                        lp.topMargin = 45.dp
//                        layoutParams = lp
//                    }
//                }
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