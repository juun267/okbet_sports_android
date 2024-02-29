package org.cxct.sportlottery.ui.betRecord

import android.content.Intent
import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingMutilAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.databinding.ContentMatchRecordBinding
import org.cxct.sportlottery.databinding.ContentParlayRecordBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.ui.betRecord.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScorePlatCateName
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.view.onClick

class TransactionRecordDiffAdapter(val viewModel: AccountHistoryViewModel) :
    BindingMutilAdapter<DataItem>() {
    var isLastPage: Boolean = false
    var totalAmount: Double = 0.0
    var itemList = listOf<DataItem>()

    private enum class ViewType { Match, Parlay, Outright }

    fun setupBetList(betListData: BetListData) {
        isLastPage = betListData.isLastPage
        totalAmount = betListData.totalMoney
        itemList = when {
            else -> betListData.row.map { DataItem(it) }
        }
        setList(itemList)
    }
    override fun initItemType() {
       addItemType(ViewType.Match.ordinal,object :OnMultiItemAdapterListener<DataItem,ContentMatchRecordBinding>(){
           override fun onBinding(
               position: Int,
               binding: ContentMatchRecordBinding,
               item: DataItem,
           ) {
               binding.bindMatch(item.row)
           }
       })
        addItemType(ViewType.Outright.ordinal,object :OnMultiItemAdapterListener<DataItem,ContentMatchRecordBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentMatchRecordBinding,
                item: DataItem,
            ) {
                binding.bindOutRight(item.row)
            }
        })
        addItemType(ViewType.Parlay.ordinal,object :OnMultiItemAdapterListener<DataItem,ContentParlayRecordBinding>(){
            override fun onBinding(
                position: Int,
                binding: ContentParlayRecordBinding,
                item: DataItem,
            ) {
                binding.bind(item.row)
            }
        })
    }

    override fun onItemType(position: Int): Int {
       return getItem(position).parlayType.toIntS(ViewType.Match.ordinal)
    }

    private fun ContentMatchRecordBinding.bindMatch(data: Row) {
        val matchOdds = data.matchOdds[0]
        contentPlay.setCompoundDrawablesRelative(null, null, null, null)
        ivCountry.setLeagueLogo(matchOdds.categoryIcon)
        titleLeagueName.text = matchOdds.leagueName.replace("\n", "")
        titleTeamName.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)

        //篮球 滚球 全场让分【欧洲盘】
        contentPlay.setGameType_MatchType_PlayCateName_OddsType(
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
            playContent.setPlayContent(
                matchOdds.playCateCode.getEndScorePlatCateName(context),
                matchOdds.spread,
                formatForOdd
            )
        else
            playContent.setPlayContent(
                matchOdds.playName, matchOdds.spread, formatForOdd
            )

        matchPlayTime.text =
            TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
        if (data.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (data.betConfirmTime
                ?: 0L)
        ) {
            val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
            object : CountDownTimer(leftTime ?: 0, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    tvBetResult.text = String.format(
                        context.getString(R.string.pending),
                        TimeUtil.longToSecond(millisUntilFinished)
                    )
                    tvPrint.gone()
                }

                override fun onFinish() {
                    tvBetResult.setBetReceiptStatus(data.status)
                    tvPrint.visible()
                }
            }.start()
        } else {
            tvBetResult.visibility = View.GONE
        }

        contentBetAmount.text = TextUtil.format(data.totalAmount)
        contentWinnableAmount.text = TextUtil.format(data.winnable)
        contentOrderNo.text = data.orderNo
        contentTimeType.text = TimeUtil.timeFormat(data.addTime, TimeUtil.DM_HM_FORMAT)
        when (data.gameType) {
            GameType.FT.key, GameType.BK.key -> {
                if (matchOdds.rtScore?.isNotEmpty() == true) tvScore.text =
                    "(${matchOdds.rtScore})"
            }
        }

        val singleTitle = context.getString(R.string.bet_record_single) + "-${
            GameType.getGameTypeString(
                context, data.gameType
            )
        }"
        tvMatchTitle.text = singleTitle

        if (data.status != 0) tvBetResult.setBetReceiptStatus(
            data.status, data.cancelledBy
        )
        tvBetResult.isVisible = data.status != 7

        llCopyBetOrder.setOnClickListener {
            context.copyToClipboard(data.orderNo)
        }
        includeBetRecordEndscore.apply {
            root.isVisible = data.matchOdds.firstOrNull()?.playCateCode.isEndScoreType()
            if (linEndscore.isVisible) {
                val sortList = data.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode }
                    ?: listOf()
                contentPlay.setCompoundDrawablesWithIntrinsicBounds(null,
                    null,
                    context.getDrawable(R.drawable.ic_right_arrow_gray),
                    null)
                this@bindMatch.root.onClick {
                    val intent = Intent(context, BetDetailsActivity::class.java)
                    intent.putExtra("data", data)
                    context?.startActivity(intent)
                }
                val listData = if (sortList.size > 6) {
                    sortList.subList(0, 6)
                } else {
                    sortList
                }
                if (rvEndscoreInfo.adapter == null) {
                    rvEndscoreInfo.layoutManager =
                        LinearLayoutManager(rvEndscoreInfo.context,
                            RecyclerView.HORIZONTAL,
                            false)
                    rvEndscoreInfo.addItemDecoration(SpaceItemDecoration(context,
                        R.dimen.margin_4))
                    val scoreAdapter = BetRecordEndScoreAdapter()
                    rvEndscoreInfo.adapter = scoreAdapter
                    scoreAdapter.setList(listData)
                } else {
                    (rvEndscoreInfo.adapter as BetRecordEndScoreAdapter).setList(listData)
                }
                tvMore?.let {
                    tvMore.isVisible = sortList.size > 6
                }
            }
        }

    }

    fun ContentMatchRecordBinding.bindOutRight(data: Row) {
        val matchOdds = data.matchOdds[0]
        ivCountry.setLeagueLogo(matchOdds.categoryIcon)
//                title_league_name.text = "${matchOdds.leagueName} - ${matchOdds.playCateName}"
        titleLeagueName.text = matchOdds.leagueName
        titleTeamName.text = matchOdds.leagueName

        //篮球 滚球 全场让分【欧洲盘】
        contentPlay.setGameType_MatchType_PlayCateName_OddsType(
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
        playContent.setPlayContent(
            playName, matchOdds.spread, formatForOdd
        )
        matchOdds.startTime?.let {
            matchPlayTime.text = TimeUtil.timeFormat(it, TimeUtil.DM_HM_FORMAT)
        }
        matchPlayTime.isVisible = data.parlayType != ParlayType.OUTRIGHT.key
        contentBetAmount.text = TextUtil.format(data.totalAmount)
        contentWinnableAmount.text = TextUtil.format(data.winnable)
        contentOrderNo.text = data.orderNo
        contentTimeType.text = TimeUtil.timeFormat(data.addTime, TimeUtil.DM_HM_FORMAT)

        val singleTitle = context.getString(R.string.bet_record_single) + "-${
            GameType.getGameTypeString(
                context, data.gameType
            )
        }"
        tvMatchTitle.text = singleTitle

        tvBetResult.setBetReceiptStatus(data.status, data.cancelledBy)
        tvBetResult.isVisible = data.status != 7

        llCopyBetOrder.setOnClickListener {
            context.copyToClipboard(data.orderNo)
        }
    }

    fun ContentParlayRecordBinding.bind(data: Row) {
        val contentParlayMatchAdapter by lazy { ContentParlayMatchAdapter(data, viewModel) }

        getParlayStringRes(data.parlayType)?.let { parlayTypeStringResId ->
            //盡量避免直接使用 MultiLanguagesApplication.appContext.getString 容易出現語系顯示錯誤
//                    title_parlay_type.text = itemView.context.getString(parlayTypeStringResId)
            val parlayTitle = context.getString(R.string.bet_record_parlay) + "(${
                context.getString(parlayTypeStringResId)
            })" + "-${GameType.getGameTypeString(context, data.gameType)}"
            titleParlayType.text = parlayTitle
        }
        rvParlayMatch.apply {
            adapter = contentParlayMatchAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
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

        contentParlayBetAmount.text = TextUtil.format(data.totalAmount)
        contentParlayWinnableAmount.text = TextUtil.format(data.winnable)
        contentParlayOrderNo.text = data.orderNo
        contentParlayTimeType.text = TimeUtil.timeFormat(data.addTime, TimeUtil.DM_HM_FORMAT)

        if (data.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (data.betConfirmTime
                ?: 0L)
        ) {
            val leftTime = data.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
            object : CountDownTimer(leftTime ?: 0, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    tvBetResultParlay.text = String.format(
                        context.getString(R.string.pending),
                        TimeUtil.longToSecond(millisUntilFinished)
                    )
                }

                override fun onFinish() {
                    tvBetResultParlay.setBetReceiptStatus(data.status)
                }
            }.start()
        } else {
            tvBetResultParlay.visibility = View.GONE
        }

        if (data.status != 0) tvBetResultParlay.setBetReceiptStatus(
            data.status, data.cancelledBy
        )
        tvBetResultParlay.isVisible = data.status != 7

        llCopyBetOrderParlay.setOnClickListener {
            context.copyToClipboard(data.orderNo)
        }
    }

}

@KeepMembers
data class DataItem(
    val row: Row,
    var parlayType: String? = row.parlayType,
    var orderNo: String? = row.orderNo,
)