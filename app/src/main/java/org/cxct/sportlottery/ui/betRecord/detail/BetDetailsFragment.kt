package org.cxct.sportlottery.ui.betRecord.detail

import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.fragment_bet_details.*
import kotlinx.android.synthetic.main.include_bet_record_endscore.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentBetDetailsBinding
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.ui.betRecord.BetRecordEndScoreAdapter
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.ui.betRecord.TransactionRecordDiffAdapter
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScorePlatCateName
import org.cxct.sportlottery.util.DisplayUtil.dp

class BetDetailsFragment : BaseFragment<BetListViewModel,FragmentBetDetailsBinding>() {
    //复制的注单列表的适配器
    private val detailAdapter by lazy { TransactionRecordDetailAdapter() }

    override fun onInitView(view: View) {
        val row = arguments?.get("data") as Row?
        val detailRow =
            arguments?.get("detailRow") as org.cxct.sportlottery.network.bet.settledDetailList.Row?
        when {
            row != null -> {
                if (row.parlayType == ParlayType.SINGLE.key) {
                    (activity as BetDetailsActivity).setTitleName(getString(R.string.commission_detail))
                    rv_bet_record.isVisible = false
                    include_endscore.isVisible = true
                    setupEndScoreInfo(row, row.matchOdds.first())

                } else {
                    (activity as BetDetailsActivity).setTitleName(getString(R.string.bet_details_title))
                    rv_bet_record.isVisible = true
                    include_endscore.isVisible = false
                    detailAdapter.setupBetList(row)
                    rv_bet_record.adapter = detailAdapter
                }
            }

            detailRow != null -> {
                (activity as BetDetailsActivity).setTitleName(getString(R.string.commission_detail))
                rv_bet_record.isVisible = false
                include_endscore.isVisible = true
                detailRow.matchOdds?.first()?.let {
                    setupDetailEndScoreInfo(detailRow, it)
                }
            }
        }
    }


    private fun setupEndScoreInfo(row: Row, matchOdds: MatchOdd) {
        include_endscore.apply {
            iv_country.setLeagueLogo(matchOdds.categoryIcon)
            title_league_name.text = matchOdds.leagueName.replace("\n", "")
            title_team_name.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)
            content_play.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            //篮球 滚球 全场让分【欧洲盘】
            content_play.setGameType_MatchType_PlayCateName_OddsType(
                row.gameType, row.matchType, matchOdds.playCateName, matchOdds.oddsType
            )

            tvPrint.gone()


            val formatForOdd =
                if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                    matchOdds.odds - 1
                ) else TextUtil.formatForOdd(matchOdds.odds)
            val playName =
                if (matchOdds.playCateCode.isEndScoreType()) matchOdds.playCateCode.getEndScorePlatCateName(context)
                else matchOdds.playName
            play_content.setPlayContent(
                playName, matchOdds.spread, formatForOdd
            )

            match_play_time.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
            if (row.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (row.betConfirmTime
                    ?: 0L)
            ) {
                val leftTime = row.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                object : CountDownTimer(leftTime ?: 0, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        tv_bet_result.text = String.format(
                            context.getString(R.string.pending),
                            TimeUtil.longToSecond(millisUntilFinished)
                        )
                        tvPrint.gone()
                    }

                    override fun onFinish() {
                        tv_bet_result.setBetReceiptStatus(row.status)
                        tvPrint.visible()
                    }
                }.start()
            } else {
                tv_bet_result.visibility = View.GONE
            }

            content_bet_amount.text = TextUtil.format(row.totalAmount)
            content_winnable_amount.text = TextUtil.format(row.winnable)
            content_order_no.text = row.orderNo
            content_time_type.text =
                TransactionRecordDiffAdapter.getTimeFormatFromDouble(row.addTime)
            when (row.gameType) {
                GameType.FT.key, GameType.BK.key -> {
                    if (matchOdds.rtScore?.isNotEmpty() == true) tv_score.text =
                        "(${matchOdds.rtScore})"
                }
            }

            val singleTitle = context.getString(R.string.bet_record_single) + "-${
                GameType.getGameTypeString(
                    context, row.gameType
                )
            }"
            tv_match_title.text = singleTitle

            if (row.status != 0) tv_bet_result.setBetReceiptStatus(
                row.status, row.cancelledBy
            )
            tv_bet_result.isVisible = row.status != 7

            ll_copy_bet_order.setOnClickListener {
                context.copyToClipboard(row.orderNo)
            }
            lin_endscore.isVisible =
                row.matchOdds.firstOrNull()?.playCateCode.isEndScoreType()
            if (lin_endscore.isVisible) {
                if (rv_endscore_info.adapter == null) {
                    rv_endscore_info.layoutManager = GridLayoutManager(rv_endscore_info.context, 8)
                    rv_endscore_info.addItemDecoration(GridSpacingItemDecoration(8, 4.dp, false))
                    val scoreAdapter = BetRecordEndScoreAdapter()
                    rv_endscore_info.adapter = scoreAdapter
                    scoreAdapter.setList(row.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode }
                        ?: listOf())
                } else {
                    (rv_endscore_info.adapter as BetRecordEndScoreAdapter).setList(row.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode })
                }
                tv_more.gone()
            }
        }
    }

    private fun setupDetailEndScoreInfo(
        row: org.cxct.sportlottery.network.bet.settledDetailList.Row,
        matchOdds: org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd,
    ) {
        include_endscore.apply {
            iv_country.setLeagueLogo(matchOdds.categoryIcon)
            title_league_name.text = matchOdds.leagueName?.replace("\n", "")
            title_team_name.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)
            content_play.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            //篮球 滚球 全场让分【欧洲盘】
            content_play.setGameType_MatchType_PlayCateName_OddsType(
                row.gameType, row.matchType, matchOdds.playCateName, matchOdds.oddsType
            )

            tvPrint.gone()


            val formatForOdd =
                if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                    matchOdds.odds ?: 0 - 1
                ) else TextUtil.formatForOdd(matchOdds.odds ?: 0)
            play_content.setPlayContent(
                matchOdds.playCateCode.getEndScorePlatCateName(context),
                matchOdds.spread,
                formatForOdd
            )

            match_play_time.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
            tv_bet_result.setBetReceiptStatus(row.status)
//            tvPrint.visible()
            content_bet_amount.text = TextUtil.format(row.totalAmount ?: 0)
            content_winnable_amount.text = TextUtil.format(row.winnable ?: 0)
            content_order_no.text = row.orderNo
            content_time_type.text =
                TransactionRecordDiffAdapter.getTimeFormatFromDouble(row.addTime ?: 0)
            when (row.gameType) {
                GameType.FT.key, GameType.BK.key -> {
                    if (matchOdds.rtScore?.isNotEmpty() == true) tv_score.text =
                        "(${matchOdds.rtScore})"
                }
            }

            val singleTitle = context.getString(R.string.bet_record_single) + "-${
                GameType.getGameTypeString(
                    context, row.gameType
                )
            }"
            tv_match_title.text = singleTitle

            if (row.status != 0) tv_bet_result.setBetReceiptStatus(
                row.status, row.cancelledBy
            )
            tv_bet_result.isVisible = row.status != 7

            ll_copy_bet_order.setOnClickListener {
                context.copyToClipboard(row.orderNo ?: "")
            }
            lin_endscore.isVisible =
                row.matchOdds?.firstOrNull()?.playCateCode.isEndScoreType()
            if (lin_endscore.isVisible) {
                if (rv_endscore_info.adapter == null) {
                    rv_endscore_info.layoutManager = GridLayoutManager(rv_endscore_info.context, 8)
                    rv_endscore_info.addItemDecoration(GridSpacingItemDecoration(8, 4.dp, false))
                    val scoreAdapter = BetRecordEndScoreAdapter()
                    rv_endscore_info.adapter = scoreAdapter
                    scoreAdapter.setList(row.matchOdds?.firstOrNull()?.multiCode ?: listOf())
                } else {
                    (rv_endscore_info.adapter as BetRecordEndScoreAdapter).setList(row.matchOdds?.firstOrNull()?.multiCode)
                }
                tv_more.gone()
            }
        }
    }
}