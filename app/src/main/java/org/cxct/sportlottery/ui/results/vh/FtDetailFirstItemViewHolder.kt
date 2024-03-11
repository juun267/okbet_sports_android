package org.cxct.sportlottery.ui.results.vh

import android.view.View
import org.cxct.sportlottery.databinding.ContentGameDetailResultFtRvBinding
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.list.MatchStatus
import org.cxct.sportlottery.ui.results.SituationType
import org.cxct.sportlottery.ui.results.StatusType

class FtDetailFirstItemViewHolder(val binding: ContentGameDetailResultFtRvBinding) {

    val bottomLine: View = binding.bottomLine
    val llRoot: View = binding.root

    fun bind(matchData: Match?) {
        val firstHalf = matchData?.getMatch(StatusType.FIRST_HALF)
        val fullGame = matchData?.getMatch(StatusType.END_GAME)
            ?: matchData?.getMatch(StatusType.OVER_TIME)

        binding.apply {
            tvFirstHalfCard.text = getSituation(firstHalf, SituationType.YELLOW_CARD)
            tvFullGameCard.text = getSituation(fullGame, SituationType.YELLOW_CARD)
            tvFirstHalfCorner.text = getSituation(firstHalf, SituationType.CORNER_KICK)
            tvFullGameCorner.text = getSituation(fullGame, SituationType.CORNER_KICK)
        }
    }

    private fun getSituation(matchStatus: MatchStatus?, situationType: SituationType): String {
        when (situationType) {
            SituationType.YELLOW_CARD -> {
                matchStatus.let {
                    return if (it?.homeCards == null || it.awayCards == null)
                        ""
                    else
                        "${it.homeCards} - ${it.awayCards}"
                }
            }
            SituationType.CORNER_KICK -> {
                matchStatus.let {
                    return if (it?.homeCornerKicks == null || it.awayCornerKicks == null)
                        ""
                    else
                        "${it.homeCornerKicks} - ${it.awayCornerKicks}"
                }
            }
        }
    }
}
