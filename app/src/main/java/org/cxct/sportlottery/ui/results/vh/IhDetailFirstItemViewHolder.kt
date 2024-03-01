package org.cxct.sportlottery.ui.results.vh

import android.view.View
import org.cxct.sportlottery.databinding.ContentGameDetailResultIhRvBinding
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.ui.results.StatusType

//冰球第一筆
    class IhDetailFirstItemViewHolder(val binding: ContentGameDetailResultIhRvBinding) {

        fun bind(detailData: Match?) {
            setupIhDetailFirstItem(detailData)
        }

        private fun setupIhDetailFirstItem(detailData: Match?) {
            val matchStatus = detailData?.matchStatusList
            val matchInfo = detailData?.matchInfo
            val firstSection = matchStatus?.find { it.status == StatusType.FIRST_SECTION }
            val secondSection = matchStatus?.find { it.status == StatusType.SECOND_SECTION }
            val thirdSection = matchStatus?.find { it.status == StatusType.THIRD_SECTION }
            val overSection = matchStatus?.find { it.status == StatusType.OVER_TIME }
            val regularSection = matchStatus?.find { it.status == StatusType.END_GAME }

            binding.apply {
                llGameDetailFirstItem.visibility = View.VISIBLE

                matchInfo?.let {
                    tvHomeName.text = it.homeName
                    tvAwayName.text = it.awayName
                }

                //第一節
                tvHomeFirst.text = firstSection?.homeScore?.toString() ?: "-"
                tvAwayFirst.text = firstSection?.awayScore?.toString() ?: "-"

                //第二節
                tvHomeSecond.text = secondSection?.homeScore?.toString() ?: "-"
                tvAwaySecond.text = secondSection?.awayScore?.toString() ?: "-"

                //第三節
                tvHomeThird.text = thirdSection?.homeScore?.toString() ?: "-"
                tvAwaySecond.text = thirdSection?.awayScore?.toString() ?: "-"

                //常规比分
                tvHomeRegular.text = regularSection?.homeScore?.toString() ?: "-"
                tvAwayRegular.text = regularSection?.awayScore?.toString() ?: "-"

                //总分含加时
                tvHomeScoreWithOvertime.text = overSection?.homeScore?.toString() ?: "-"
                tvAwayScoreWithOvertime.text = overSection?.awayScore?.toString() ?: "-"
            }
        }
    }