package org.cxct.sportlottery.ui.transactionStatus

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_last_total_record.view.*
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import kotlinx.android.synthetic.main.itemview_game_no_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setPlayContent

//TODO 20210719當前api缺少總金額,待後端修正後進行確認
class TransactionRecordDiffAdapter :
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

    fun updateListStatus(sportBet: SportBet) {
        itemList.forEach { dataItem ->
            if(dataItem.orderNo == sportBet.orderNo)
                (dataItem as DataItem.Item).row.status = sportBet.status ?: 999
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
                holder.bind((rvData as DataItem.Item).row)
            }
            is ParlayRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row)
            }
            is OutrightRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row)
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
                return MatchRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = matchOdds.leagueName.replace("\n","")
                title_team_name.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)

                val oddsTypeStr = when (matchOdds.oddsType) {
                    OddsType.HK.code -> "【" + context.getString(OddsType.HK.res) + "】"
                    OddsType.MYS.code -> "【" + context.getString(OddsType.MYS.res) + "】"
                    OddsType.IDN.code -> "【" + context.getString(OddsType.IDN.res) + "】"
                    else -> "【" + context.getString(OddsType.EU.res) + "】"
                }

                val formatForOdd = if(matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(matchOdds.odds - 1) else TextUtil.formatForOdd(matchOdds.odds)
                play_content.setPlayContent(
                    matchOdds.playName,
                    matchOdds.spread,
                    formatForOdd
                )

                match_play_time.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)

                content_play.text = if (data.matchType != null) {
                    //篮球 滚球 全场让分【欧洲盘】
                    "${getGameTypeName(data.gameType)} ${getMatchTypeName(data.matchType)} ${matchOdds.playCateName}$oddsTypeStr"
                } else {
                    "${getGameTypeName(data.gameType)} ${matchOdds.playCateName}$oddsTypeStr"
                }

//                if(data.betConfirmTime?.toInt() != 0){
//                    val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
//                    object : CountDownTimer(leftTime ?: 0, 1000) {
//
//                        override fun onTick(millisUntilFinished: Long) {
//                            tv_count_down.visibility = View.VISIBLE
//                            tv_count_down.text = "${TimeUtil.longToSecond(millisUntilFinished)} ${context.getString(R.string.sec)}"
//                        }
//
//                        override fun onFinish() {
//                            tv_count_down.text = "0 ${context.getString(R.string.sec)}"
//                            if(data.status != 0){
//                                tv_count_down.visibility = View.GONE
//                            }
//                        }
//                    }.start()
//                }else{
//                    tv_count_down.visibility = View.GONE
//                }

                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)
                when (data.gameType) {
                    GameType.FT.key, GameType.BK.key -> {
                        if (matchOdds.rtScore?.isNotEmpty() == true) tv_score.text = "(${matchOdds.rtScore})"
                    }
                }

                val singleTitle =
                    context.getString(R.string.bet_record_single) + "-${getGameTypeName(data.gameType)}"
                tv_match_title.text = singleTitle

                tv_bet_result.setBetReceiptStatus(data.status, data.cancelledBy)
                tv_bet_result.isVisible = data.status != 7

                ll_copy_bet_order.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

        private fun getMatchTypeName(matchType: String?): String {
            return itemView.context.getString(MatchType.getMatchTypeStringRes(matchType))
        }
    }

    class OutrightRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view =
                    layoutInflater.inflate(R.layout.content_match_record, viewGroup, false)
                return OutrightRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
//                title_league_name.text = "${matchOdds.leagueName} - ${matchOdds.playCateName}"
                title_league_name.text = matchOdds.leagueName
                title_team_name.text = matchOdds.leagueName
                val oddsTypeStr = when (matchOdds.oddsType) {
                    OddsType.HK.code -> "【" + context.getString(OddsType.HK.res) + "】"
                    OddsType.MYS.code -> "【" + context.getString(OddsType.MYS.res) + "】"
                    OddsType.IDN.code -> "【" + context.getString(OddsType.IDN.res) + "】"
                    else -> "【" + context.getString(OddsType.EU.res) + "】"
                }
                content_play.text = if (data.matchType != null) {
                    //篮球 滚球 全场让分【欧洲盘】
                    "${getGameTypeName(data.gameType)} ${getMatchTypeName(data.matchType)} ${matchOdds.playCateName}$oddsTypeStr"
                } else {
                    "${getGameTypeName(data.gameType)} ${matchOdds.playCateName}$oddsTypeStr"
                }

                val formatForOdd = if(matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(matchOdds.odds - 1) else TextUtil.formatForOdd(matchOdds.odds)
                play_content.setPlayContent(
                    matchOdds.playName,
                    matchOdds.spread,
                    formatForOdd
                )
                matchOdds.startTime?.let {
                    match_play_time.text = TimeUtil.timeFormat(it, TimeUtil.DM_HM_FORMAT)
                }
                match_play_time.isVisible = data.parlayType != ParlayType.OUTRIGHT.key
                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)

                val singleTitle =
                    context.getString(R.string.bet_record_single) + "-${getGameTypeName(data.gameType)}"
                tv_match_title.text = singleTitle

                tv_bet_result.setBetReceiptStatus(data.status, data.cancelledBy)
                tv_bet_result.isVisible = data.status != 7

                ll_copy_bet_order.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

        private fun getMatchTypeName(matchType: String?): String {
            return itemView.context.getString(MatchType.getMatchTypeStringRes(matchType))
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

        fun bind(data: Row) {
            val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter(data.status) }

            itemView.apply {
                getParlayStringRes(data.parlayType)?.let { parlayTypeStringResId ->
                    //盡量避免直接使用 MultiLanguagesApplication.appContext.getString 容易出現語系顯示錯誤
//                    title_parlay_type.text = itemView.context.getString(parlayTypeStringResId)
                    val parlayTitle = context.getString(R.string.bet_record_parlay) +
                            "(${context.getString(parlayTypeStringResId)})" +
                            "-${getGameTypeName(data.gameType)}"
                    title_parlay_type.text = parlayTitle
                }
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(
                        getGameTypeName(data.gameType),
                        data.matchOdds,
                        data.betConfirmTime,
                        data.matchType
                    )

                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
                content_parlay_winnable_amount.text = TextUtil.format(data.winnable)
                content_parlay_order_no.text = data.orderNo
                content_parlay_time_type.text = getTimeFormatFromDouble(data.addTime)

                tv_bet_result_parlay.setBetReceiptStatus(data.status, data.cancelledBy)
                tv_bet_result_parlay.isVisible = data.status != 7

                ll_copy_bet_order_parlay.setOnClickListener {
                    context.copyToClipboard(data.orderNo)
                }
            }
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
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
                list_no_record_img.apply {
                    viewTreeObserver.addOnGlobalLayoutListener {
                        val lp = layoutParams as ConstraintLayout.LayoutParams
                        lp.topMargin = 30.dp
                        layoutParams = lp
                    }
                }
                list_no_record_text.apply {
                    viewTreeObserver.addOnGlobalLayoutListener {
                        val lp = layoutParams as ConstraintLayout.LayoutParams
                        lp.topMargin = 45.dp
                        layoutParams = lp
                    }
                }
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