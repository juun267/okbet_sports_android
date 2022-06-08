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
import kotlinx.android.synthetic.main.content_match_record.view.play_content
import kotlinx.android.synthetic.main.content_outright_record.view.*
import kotlinx.android.synthetic.main.content_outright_record.view.content_bet_amount
import kotlinx.android.synthetic.main.content_outright_record.view.content_order_no
import kotlinx.android.synthetic.main.content_outright_record.view.content_play
import kotlinx.android.synthetic.main.content_outright_record.view.content_time_type
import kotlinx.android.synthetic.main.content_outright_record.view.content_winnable_amount
import kotlinx.android.synthetic.main.content_outright_record.view.title_league_name
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import kotlinx.android.synthetic.main.itemview_game_no_record.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TextUtil.getParlayShowName
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setPlayContent

//TODO 20210719當前api缺少總金額,待後端修正後進行確認
class TransactionRecordDiffAdapter :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    var isLastPage: Boolean = false
    var totalAmount: Double = 0.0
    var status = 0
    var itemList = listOf<DataItem>()

    private enum class ViewType { Match, Parlay, Outright, LastTotal, NoData }

    fun setupBetList(betListData: BetListData, status: Int) {
        isLastPage = betListData.isLastPage
        totalAmount = betListData.totalMoney
        this.status = status
        itemList = when {
            betListData.row.isEmpty() -> listOf(DataItem.NoData)
            else -> betListData.row.map { DataItem.Item(it) } + listOf(DataItem.Total(totalAmount))
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
            ViewType.LastTotal.ordinal -> LastTotalViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.adapterPosition)
        when (holder) {
            is MatchRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, status)
            }
            is ParlayRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row, status)
            }
            is OutrightRecordViewHolder -> {
                holder.bind((rvData as DataItem.Item).row)
            }
            is LastTotalViewHolder -> {
                holder.bind((rvData as DataItem.Total).totalAmount)
            }
            is NoDataViewHolder -> {
                holder.bind()
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

        fun bind(data: Row, status: Int) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = matchOdds.leagueName.replace("\n","")
                title_home_name.text = matchOdds.homeName
                title_away_name.text = matchOdds.awayName

//                val oddsTypeStr = when (matchOdds.oddsType) {
//                    OddsType.HK.code -> "(" + context.getString(OddsType.HK.res) + ")"
//                    OddsType.MYS.code -> "(" + context.getString(OddsType.MYS.res) + ")"
//                    OddsType.IDN.code -> "(" + context.getString(OddsType.IDN.res) + ")"
//                    else -> "(" + context.getString(OddsType.EU.res) + ")"
//                }

                val formatForOdd = if(matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(matchOdds.odds - 1) else TextUtil.formatForOdd(matchOdds.odds)
                play_content.setPlayContent(
                    matchOdds.playName,
                    matchOdds.spread,
                    formatForOdd
                )

                match_play_time.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.YMD_HM_FORMAT)
                content_play.text = "${getGameTypeName(data.gameType)} ${matchOdds.playCateName}"

                if(data.betConfirmTime?.toInt() != 0){
                    val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                    object : CountDownTimer(leftTime ?: 0, 1000) {

                        override fun onTick(millisUntilFinished: Long) {
                            tv_count_down.visibility = View.VISIBLE
                            tv_count_down.text = "${TimeUtil.longToSecond(millisUntilFinished)} ${context.getString(R.string.sec)}"
                        }

                        override fun onFinish() {
                            tv_count_down.text = "0 ${context.getString(R.string.sec)}"
                            if(status != 0){
                                tv_count_down.visibility = View.GONE
                            }
                        }
                    }.start()
                }else{
                    tv_count_down.visibility = View.GONE
                }


                content_bet_amount.text = TextUtil.format(data.totalAmount)
                content_winnable_amount.text = TextUtil.format(data.winnable)
                content_order_no.text = data.orderNo
                content_time_type.text = getTimeFormatFromDouble(data.addTime)
                when (data.gameType) {
                    GameType.FT.key, GameType.BK.key -> {
                        if (matchOdds.rtScore?.isNotEmpty() == true)
                            tv_score.text = String.format(
                                context.getString(R.string.brackets),
                                matchOdds.rtScore
                            )
                    }
                }
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
                val view =
                    layoutInflater.inflate(R.layout.content_outright_record, viewGroup, false)
                return OutrightRecordViewHolder(view)
            }
        }

        fun bind(data: Row) {
            val matchOdds = data.matchOdds[0]
            itemView.apply {
                title_league_name.text = "${matchOdds.leagueName} - ${matchOdds.playCateName}"
                content_play.text = "${getGameTypeName(data.gameType)} ${matchOdds.playCateName}"
//                val oddsTypeStr = when (matchOdds.oddsType) {
//                    OddsType.HK.code -> "(" + context.getString(OddsType.HK.res) + ")"
//                    OddsType.MYS.code -> "(" + context.getString(OddsType.MYS.res) + ")"
//                    OddsType.IDN.code -> "(" + context.getString(OddsType.IDN.res) + ")"
//                    else -> "(" + context.getString(OddsType.EU.res) + ")"
//                }

                val formatForOdd = if(matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(matchOdds.odds - 1) else TextUtil.formatForOdd(matchOdds.odds)
                play_content.setPlayContent(
                    matchOdds.playName,
                    matchOdds.spread,
                    formatForOdd
                )
                matchOdds.startTime?.let {
                    play_time.text = TimeUtil.timeFormat(it, TimeUtil.YMD_HM_FORMAT)
                }
                play_time.isVisible = data.parlayType != ParlayType.OUTRIGHT.key
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
                val view =
                    layoutInflater.inflate(R.layout.content_last_total_record, viewGroup, false)
                return LastTotalViewHolder(view)
            }
        }

        fun bind(totalAmount: Double) {
            itemView.apply {
                last_total_amount.text =
                    "${sConfigData?.systemCurrency}${TextUtil.format(totalAmount)}"
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

        fun bind(data: Row, status: Int) {
            val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter(status) }

            itemView.apply {
                getParlayStringRes(data.parlayType)?.let { parlayTypeStringResId ->
                    title_parlay_type.text = MultiLanguagesApplication.appContext.getString(parlayTypeStringResId)
                }
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(
                        getGameTypeName(data.gameType),
                        data.matchOdds,
                        data.betConfirmTime
                    )

                }

                content_parlay_bet_amount.text = TextUtil.format(data.totalAmount)
                content_parlay_winnable_amount.text = TextUtil.format(data.winnable)
                content_parlay_order_no.text = data.orderNo
                content_parlay_time_type.text = getTimeFormatFromDouble(data.addTime)
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

    data class Total(val totalAmount: Double) : DataItem() {
        override var parlayType: String? = null
        override var orderNo: String? = null
    }


    object NoData : DataItem() {
        override var parlayType: String? = null
        override var orderNo: String? = null
    }
}