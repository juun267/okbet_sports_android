package org.cxct.sportlottery.ui.results.vh

import android.view.View
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemMatchResultMatchNewBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.results.MatchItemClickListener
import org.cxct.sportlottery.ui.results.MatchResultData
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.util.TimeUtil

class MatchViewHolder(val binding: ItemMatchResultMatchNewBinding,val position:Int) {

    val bottomLine: View = binding.bottomLine

    fun bind(
        gameType: String,
        item: MatchResultData,
        matchItemClickListener: MatchItemClickListener
    ) {
        setupView(item)
        setupViewType(gameType)
        setupResultItem(item)
        setupEvent(matchItemClickListener)
    }

    private fun setupView(item: MatchResultData) = binding.run {
        if (item.matchExpanded) {
            ivSwitch.setImageResource(R.drawable.icon_more_on)
            llGameDetail.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_gray)
        } else {
            ivSwitch.setImageResource(R.drawable.icon_more)
            if (item.isLastMatchData) {
                llGameDetail.setBackgroundResource(R.drawable.bg_shape_bottom_8dp_gray_stroke_no_top_stroke)
            } else {
                llGameDetail.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_gray)
            }
        }
    }

    private fun setupViewType(gameType: String) = binding.run {
        val params = llScore.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f

        when (gameType) {
            GameType.FT.key -> { //上半場, 全場
                tvFirstHalfScore.visibility = View.VISIBLE
                tvSecondHalfScore.visibility = View.GONE
                tvEndGameScore.visibility = View.GONE
                tvFullGameScore.visibility = View.VISIBLE
            }
            GameType.BK.key -> { //上半場, 下半場, 賽果
                tvFirstHalfScore.visibility = View.VISIBLE
                tvSecondHalfScore.visibility = View.VISIBLE
                tvEndGameScore.visibility = View.VISIBLE
                tvFullGameScore.visibility = View.GONE
                params.weight = 2.2f
            }
            else -> { //賽果 (比照iOS，其他都顯示賽果)
                tvFirstHalfScore.visibility = View.GONE
                tvSecondHalfScore.visibility = View.GONE
                tvEndGameScore.visibility = View.VISIBLE
                tvFullGameScore.visibility = View.GONE
            }
        }
        llScore.layoutParams = params
    }

    private fun setupResultItem(item: MatchResultData) = binding.run {

        val matchData = item.matchData
        val matchInfo = item.matchData?.matchInfo

        matchInfo?.let {
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
            tvTime.text = TimeUtil.timeFormat(it.startTime, TimeUtil.YMD_HM_FORMAT_2)
        }

        matchData?.let {
            val firstHalf = it.getMatch(StatusType.FIRST_HALF)
            val secondHalf = it.getMatch(StatusType.SECOND_HALF)
            //110: 加時, 有加時先取加時
            val endGame = it.getMatch(StatusType.OVER_TIME) ?: it.getMatch(StatusType.END_GAME)
            val fullGame = it.getMatch(StatusType.OVER_TIME) ?: it.getMatch(StatusType.END_GAME)

            tvFirstHalfScore.text = firstHalf?.let { filteredItem -> "${filteredItem.homeScore}-${filteredItem.awayScore}" }
            tvSecondHalfScore.text = secondHalf?.let { filteredItem -> "${filteredItem.homeScore}-${filteredItem.awayScore}" }
            tvEndGameScore.text = endGame?.let { filteredItem -> "${filteredItem.homeScore}-${filteredItem.awayScore}" }
            tvFullGameScore.text = fullGame?.let { filteredItem -> "${filteredItem.homeScore}-${filteredItem.awayScore}" }
        }
    }

    private fun setupEvent(matchItemClickListener: MatchItemClickListener) {
        binding.llGameDetail.setOnClickListener {
            matchItemClickListener.matchClick(position)
        }
    }
}