package org.cxct.sportlottery.ui.betRecord.detail

import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
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
                    binding.rvBetRecord.isVisible = false
                    binding.includeEndscore.root.isVisible = true
                    setupEndScoreInfo(row, row.matchOdds.first())

                } else {
                    (activity as BetDetailsActivity).setTitleName(getString(R.string.bet_details_title))
                    binding.rvBetRecord.isVisible = true
                    binding.includeEndscore.root.isVisible = false
                    detailAdapter.setupBetList(row)
                    binding.rvBetRecord.adapter = detailAdapter
                }
            }

            detailRow != null -> {
                (activity as BetDetailsActivity).setTitleName(getString(R.string.commission_detail))
                binding.rvBetRecord.isVisible = false
                binding.includeEndscore.root.isVisible = true
                detailRow.matchOdds?.first()?.let {
                    setupDetailEndScoreInfo(detailRow, it)
                }
            }
        }
    }


    private fun setupEndScoreInfo(row: Row, matchOdds: MatchOdd) {
        binding.includeEndscore.apply {
            ivCountry.setLeagueLogo(matchOdds.categoryIcon)
            titleLeagueName.text = matchOdds.leagueName.replace("\n", "")
            titleTeamName.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)
            contentPlay.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            //篮球 滚球 全场让分【欧洲盘】
            contentPlay.setGameType_MatchType_PlayCateName_OddsType(
                row.gameType, row.matchType, matchOdds.playCateName, matchOdds.oddsType
            )

            tvPrint.gone()


            val formatForOdd =
                if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                    matchOdds.odds - 1
                ) else TextUtil.formatForOdd(matchOdds.odds)
            val playName =
                if (matchOdds.playCateCode.isEndScoreType()) matchOdds.playCateCode.getEndScorePlatCateName(requireContext())
                else matchOdds.playName
            playContent.setPlayContent(
                playName, matchOdds.spread, formatForOdd
            )

            matchPlayTime.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
            if (row.betConfirmTime?.toInt() != 0 && System.currentTimeMillis() < (row.betConfirmTime
                    ?: 0L)
            ) {
                val leftTime = row.betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                object : CountDownTimer(leftTime ?: 0, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        tvBetResult.text = String.format(
                            getString(R.string.pending),
                            TimeUtil.longToSecond(millisUntilFinished)
                        )
                        tvPrint.gone()
                    }

                    override fun onFinish() {
                        tvBetResult.setBetReceiptStatus(row.status)
                        tvPrint.visible()
                    }
                }.start()
            } else {
                tvBetResult.visibility = View.GONE
            }

            contentBetAmount.text = TextUtil.format(row.totalAmount)
            contentWinnableAmount.text = TextUtil.format(row.winnable)
            contentOrderNo.text = row.orderNo
            contentTimeType.text =
                TransactionRecordDiffAdapter.getTimeFormatFromDouble(row.addTime)
            when (row.gameType) {
                GameType.FT.key, GameType.BK.key -> {
                    if (matchOdds.rtScore?.isNotEmpty() == true) tvScore.text =
                        "(${matchOdds.rtScore})"
                }
            }

            val singleTitle = getString(R.string.bet_record_single) + "-${
                GameType.getGameTypeString(
                    requireContext(), row.gameType
                )
            }"
            tvMatchTitle.text = singleTitle

            if (row.status != 0) tvBetResult.setBetReceiptStatus(
                row.status, row.cancelledBy
            )
            tvBetResult.isVisible = row.status != 7

            llCopyBetOrder.setOnClickListener {
                requireContext().copyToClipboard(row.orderNo)
            }
            includeBetRecordEndscore.apply {
                linEndscore.isVisible = row.matchOdds.firstOrNull()?.playCateCode.isEndScoreType()
                if (linEndscore.isVisible) {
                    if (rvEndscoreInfo.adapter == null) {
                        rvEndscoreInfo.layoutManager = GridLayoutManager(rvEndscoreInfo.context, 8)
                        rvEndscoreInfo.addItemDecoration(GridSpacingItemDecoration(8, 4.dp, false))
                        val scoreAdapter = BetRecordEndScoreAdapter()
                        rvEndscoreInfo.adapter = scoreAdapter
                        scoreAdapter.setList(row.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode }
                            ?: listOf())
                    } else {
                        (rvEndscoreInfo.adapter as BetRecordEndScoreAdapter).setList(row.matchOdds.firstOrNull()?.multiCode?.sortedBy { it.playCode })
                    }
                    tvMore.gone()
                }
            }
        }
    }

    private fun setupDetailEndScoreInfo(
        row: org.cxct.sportlottery.network.bet.settledDetailList.Row,
        matchOdds: org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd,
    ) {
        binding.includeEndscore.apply {
            ivCountry.setLeagueLogo(matchOdds.categoryIcon)
            titleLeagueName.text = matchOdds.leagueName?.replace("\n", "")
            titleTeamName.setTeamsNameWithVS(matchOdds.homeName, matchOdds.awayName)
            contentPlay.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            //篮球 滚球 全场让分【欧洲盘】
            contentPlay.setGameType_MatchType_PlayCateName_OddsType(
                row.gameType, row.matchType, matchOdds.playCateName, matchOdds.oddsType
            )

            tvPrint.gone()


            val formatForOdd =
                if (matchOdds.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                    matchOdds.odds ?: 0 - 1
                ) else TextUtil.formatForOdd(matchOdds.odds ?: 0)
            playContent.setPlayContent(
                matchOdds.playCateCode.getEndScorePlatCateName(requireContext()),
                matchOdds.spread,
                formatForOdd
            )

            matchPlayTime.text = TimeUtil.timeFormat(matchOdds.startTime, TimeUtil.DM_HM_FORMAT)
            tvBetResult.setBetReceiptStatus(row.status)
//            tvPrint.visible()
            contentBetAmount.text = TextUtil.format(row.totalAmount ?: 0)
            contentWinnableAmount.text = TextUtil.format(row.winnable ?: 0)
            contentOrderNo.text = row.orderNo
            contentTimeType.text =
                TransactionRecordDiffAdapter.getTimeFormatFromDouble(row.addTime ?: 0)
            when (row.gameType) {
                GameType.FT.key, GameType.BK.key -> {
                    if (matchOdds.rtScore?.isNotEmpty() == true) tvScore.text =
                        "(${matchOdds.rtScore})"
                }
            }

            val singleTitle = getString(R.string.bet_record_single) + "-${
                GameType.getGameTypeString(
                    requireContext(), row.gameType
                )
            }"
            tvMatchTitle.text = singleTitle

            if (row.status != 0) tvBetResult.setBetReceiptStatus(
                row.status, row.cancelledBy
            )
            tvBetResult.isVisible = row.status != 7

            llCopyBetOrder.setOnClickListener {
                requireContext().copyToClipboard(row.orderNo ?: "")
            }
            includeBetRecordEndscore.apply {
                linEndscore.isVisible =
                    row.matchOdds?.firstOrNull()?.playCateCode.isEndScoreType()
                if (linEndscore.isVisible) {
                    if (rvEndscoreInfo.adapter == null) {
                        rvEndscoreInfo.layoutManager = GridLayoutManager(rvEndscoreInfo.context, 8)
                        rvEndscoreInfo.addItemDecoration(GridSpacingItemDecoration(8, 4.dp, false))
                        val scoreAdapter = BetRecordEndScoreAdapter()
                        rvEndscoreInfo.adapter = scoreAdapter
                        scoreAdapter.setList(row.matchOdds?.firstOrNull()?.multiCode ?: listOf())
                    } else {
                        (rvEndscoreInfo.adapter as BetRecordEndScoreAdapter).setList(row.matchOdds?.firstOrNull()?.multiCode)
                    }
                    tvMore.gone()
                }  
            }
        }
    }
}