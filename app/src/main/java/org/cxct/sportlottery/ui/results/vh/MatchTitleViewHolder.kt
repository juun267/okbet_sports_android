package org.cxct.sportlottery.ui.results.vh

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemMatchResultTitleBinding as TitleBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.results.MatchItemClickListener
import org.cxct.sportlottery.ui.results.MatchResultData
import splitties.systemservices.layoutInflater

class MatchTitleViewHolder(viewGroup: ViewGroup,
                           private val binding: TitleBinding = TitleBinding.inflate(viewGroup.layoutInflater, viewGroup, false))
: RecyclerView.ViewHolder(binding.root) {

    fun bind(
        gameType: String,
        item: MatchResultData,
        matchItemClickListener: MatchItemClickListener
    ) {
        setupData(gameType, item)
        setupEvent(item, matchItemClickListener)
    }

    private fun setupData(gameType: String, item: MatchResultData) = binding.run {
        titleArrowRotate(item.titleExpanded)

        val params = llTitleLayout.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f

        tvType.text = item.titleData?.name
        when (gameType) {
            GameType.FT.key -> { //上半場, 全場
                tvFirstHalf.visibility = View.GONE
                tvSecondHalf.visibility = View.GONE
                tvEndGame.visibility = View.GONE
                tvFullGame.visibility = View.GONE
            }
            GameType.BK.key -> { //上半場, 下半場, 賽果
                tvFirstHalf.visibility = View.VISIBLE
                tvSecondHalf.visibility = View.VISIBLE
                tvEndGame.visibility = View.VISIBLE
                tvFullGame.visibility = View.GONE

                tvEndGame.text = tvEndGame.context.getString(R.string.full_game)
                params.weight = 2.2f
            }
            else -> { //賽果 (比照iOS，其他都顯示賽果)
                tvFirstHalf.visibility = View.GONE
                tvSecondHalf.visibility = View.GONE
                tvEndGame.visibility = View.VISIBLE
                tvFullGame.visibility = View.GONE
            }
        }
        llTitleLayout.layoutParams = params
    }

    private fun setupEvent(item: MatchResultData, matchItemClickListener: MatchItemClickListener) {
        binding.root.setOnClickListener {
            matchItemClickListener.leagueTitleClick(adapterPosition)
            titleArrowRotate(item.titleExpanded)
        }
    }

    private fun titleArrowRotate(titleExpanded: Boolean) {
        if (titleExpanded) {
            binding.llTitleBackground.setBackgroundResource(R.drawable.bg_shape_top_8dp_blue_stroke_no_bottom_stroke)
            binding.ivArrow.rotation = 0f
        } else {
            binding.llTitleBackground.setBackgroundResource(R.drawable.bg_shape_8dp_blue_stroke)
            binding.ivArrow.rotation = 180f
        }
    }
}