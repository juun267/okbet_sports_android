package org.cxct.sportlottery.ui.results.vh

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemMatchResultFtBinding
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.ui.results.MatchItemClickListener
import org.cxct.sportlottery.ui.results.MatchResultData
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.util.TimeUtil

@SuppressLint("SetTextI18n")
class FtMatchViewHolder(val binding: ItemMatchResultFtBinding,val position: Int) {

    val bottomLine: View = binding.bottomLine

    init {
        binding.run {
            val context = root.context
            tvHalfOT.text = "${context.getString(R.string.first_half_game)}\n(OT)"
            tvFullOT.text = "${context.getString(R.string.full_game)}(OT)"

        }
        binding.hIndicator.binHorizontalScrollView(binding.scrollView)
    }

    fun bind(
        item: MatchResultData,
        matchItemClickListener: MatchItemClickListener
    ) {
        setupView(item)
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

    private fun setupResultItem(item: MatchResultData) = binding.run {

        val matchData = item.matchData
        val matchInfo = item.matchData?.matchInfo

        matchInfo?.let {
            tvHomeName.text = it.homeName
            tvAwayName.text = it.awayName
            tvTime.text = TimeUtil.timeFormat(it.startTime, TimeUtil.YMD_HM_FORMAT_2)
        }

        if (matchData == null) {
            bindScore(tvHalfScore, null)
            bindScore(tvFullScore, null)
            bindScore(tvHalfOPTScore, null)
            bindScore(tvFullOPTScore, null)
            bindScore(tvPSOScore, null)
            return@run
        }

        bindScore(tvHalfScore, matchData.getMatch(StatusType.FIRST_HALF))
        bindScore(tvFullScore, matchData.getMatch(StatusType.END_GAME))
        bindScore(tvHalfOPTScore, matchData.getMatch(StatusType.OPT_OPT_1))
        bindScore(tvFullOPTScore, matchData.getMatch(StatusType.OVER_TIME) ?: matchData.getMatch(StatusType.OPT_OPT_2))
        bindScore(tvPSOScore, matchData.getMatch(StatusType.PSO))
    }

    private fun bindScore(textView: TextView, matchStatus: MatchStatus?) {
        if (matchStatus == null) {
            textView.text = "-"
        } else {
            textView.text = "${matchStatus.homeScore}-${matchStatus.awayScore}"
        }
    }

    private fun setupEvent(matchItemClickListener: MatchItemClickListener) {
        binding.llGameDetail.setOnClickListener {
            matchItemClickListener.matchClick(position)
        }
    }
}