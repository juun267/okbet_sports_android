package org.cxct.sportlottery.ui.game.publicity

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemPublicityRecommendBinding
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeWhiteIcon
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.SvgUtil
import timber.log.Timber

class PublicityRecommendViewHolder(val binding: ItemPublicityRecommendBinding) : RecyclerView.ViewHolder(binding.root) {
    enum class MatchType

    fun bind(data: Recommend) {
        with(binding) {
            //GameTypeView
            with(gameTypeView) {
                tvSportType.text = getGameTypeString(root.context, data.gameType)
                tvSportType.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        root.context,
                        getGameTypeWhiteIcon(data.gameType)
                    ), null, null, null
                )
                data.matchType?.resId?.let { matchTypeRes ->
                    tvGameType.text = root.context.getString(matchTypeRes)
                }
                tvMatchNum.text = data.matchNum.toString()
            }

            //LeagueView
            with(leagueView) {
                tvTest.text = "${data.leagueId} - ${data.id}"
                GameConfigManager.getTitleBarBackground(data.gameType)?.let { titleRes ->
                    root.setBackgroundResource(titleRes)
                }
                tvLeagueName.text = data.leagueName
                ivFlag.setImageDrawable(SvgUtil.getSvgDrawable(itemView.context, data.categoryIcon))
            }

        }
    }
}