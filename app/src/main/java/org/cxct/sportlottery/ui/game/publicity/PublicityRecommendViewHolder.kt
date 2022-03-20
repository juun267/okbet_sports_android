package org.cxct.sportlottery.ui.game.publicity

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemPublicityRecommendBinding
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeWhiteIcon
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueOddAdapter2
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.SvgUtil
import org.cxct.sportlottery.util.TimeUtil
import timber.log.Timber

class PublicityRecommendViewHolder(val binding: ItemPublicityRecommendBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: Recommend, oddsType: OddsType) {
        val leagueOddAdapter by lazy {
            LeagueOddAdapter2(data.matchType ?: MatchType.EARLY).apply {
                isTimerEnable = when (data.matchType) {
                    MatchType.IN_PLAY -> true
                    else -> false
                }
            }
        }
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

            //region 測試 - 資料結構與其他處不同
            val matchOddList = mutableListOf<MatchOdd>()
            matchOddList.add(
                with(data) {
                    MatchOdd(
                        matchInfo = MatchInfo(
                            gameType = gameType,
                            awayName = awayName,
                            homeName = homeName,
                            playCateNum = matchNum,
                            startTime = startTime,
                            status = status,
                            leagueId = leagueId,
                            leagueName = leagueName,
                            id = id,
                            endTime = 0
                        ).apply {
                            leagueTime = startTime?.toInt()
                            startDateDisplay = TimeUtil.timeFormat(startTime, "dd/MM")
                        }
                    )
                }
            )
            //endregion

            with(rvLeagueList) {
                layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = leagueOddAdapter
                leagueOddAdapter.setData(matchOddList, oddsType)
            }

        }
    }
}