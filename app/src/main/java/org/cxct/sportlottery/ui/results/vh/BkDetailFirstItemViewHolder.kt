package org.cxct.sportlottery.ui.results.vh

import android.view.View
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.databinding.ContentGameDetailResultBkRvBinding
import org.cxct.sportlottery.ui.results.ListType
import org.cxct.sportlottery.ui.results.MatchResultData
import org.cxct.sportlottery.ui.results.StatusType

// 篮球与美式足球
class BkDetailFirstItemViewHolder(val binding: ContentGameDetailResultBkRvBinding) {

    val bottomLine: View = binding.bottomLine
    val llRoot: View = binding.root

    fun bind(rvDataList: MatchResultData) = binding.run {
        val detailData = rvDataList.matchData
        val matchInfo = detailData?.matchInfo
        val firstSection = detailData?.getMatch(StatusType.FIRST_SECTION)
        val secondSection = detailData?.getMatch(StatusType.SECOND_SECTION)
        val thirdSection = detailData?.getMatch(StatusType.THIRD_SECTION)
        val fourthSection = detailData?.getMatch(StatusType.FOURTH_SECTION)
        val overSection = detailData?.getMatch(StatusType.OVER_TIME)
            ?: detailData?.getMatch(StatusType.END_GAME)

        val optSection = if (rvDataList.dataType == ListType.FIRST_ITEM_BK) detailData?.getMatch(StatusType.OPT_BK) else null

        matchInfo?.let {
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
        }

        //第一節
        firstSection?.let {
            tvHomeFirst.text = it.homeScore?.toString() ?: ""
            tvAwayFirst.text = it.awayScore?.toString() ?: ""
        }

        //第二節
        secondSection?.let {
            tvHomeSecond.text = it.homeScore?.toString() ?: ""
            tvAwaySecond.text = it.awayScore?.toString() ?: ""
        }

        //第三節
        thirdSection?.let {
            tvHomeThird.text = it.homeScore?.toString() ?: ""
            tvAwayThird.text = it.awayScore?.toString() ?: ""
        }

        //第四節
        fourthSection?.let {
            tvHomeFourth.text = it.homeScore?.toString() ?: ""
            tvAwayFourth.text = it.awayScore?.toString() ?: ""
        }

        //完場
        overSection?.let {
            tvHomeOverTime.text = it.homeScore?.toString() ?: ""
            tvAwayOverTime.text = it.awayScore?.toString() ?: ""
        }

        if (optSection == null) {
            setViewGone(tvOpt, vHomeOpt, tvHomeOpt, vAwayOpt, tvAwayOpt)
        } else {
            setViewVisible(tvOpt, vHomeOpt, tvHomeOpt, vAwayOpt, tvAwayOpt)
            tvHomeOpt.text = optSection.homeScore?.toString() ?: ""
            tvAwayOpt.text = optSection.awayScore?.toString() ?: ""
        }

    }


}