package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemPublicityRecommendBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeWhiteIcon
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueOddAdapter2
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.SvgUtil

class PublicityRecommendViewHolder(
    val binding: ItemPublicityRecommendBinding,
    private val publicityAdapterListener: GamePublicityAdapter.PublicityAdapterListener
) : RecyclerView.ViewHolder(binding.root) {
    private var leagueOddAdapter: LeagueOddAdapter2? = null

    @SuppressLint("SetTextI18n")
    fun bind(data: Recommend, oddsType: OddsType) {
        leagueOddAdapter = LeagueOddAdapter2(data.matchType ?: MatchType.EARLY).apply {
            isTimerEnable =
                (data.gameType == GameType.FT.key || data.gameType == GameType.BK.key || data.matchType == MatchType.PARLAY || data.matchType == MatchType.AT_START || data.matchType == MatchType.MY_EVENT)
            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, matchInfoList, _ ->
                    publicityAdapterListener.onClickPlayTypeListener(
                        gameType = data.gameType,
                        matchType = data.matchType,
                        matchId = matchId,
                        matchInfoList = matchInfoList
                    )
                },
                clickListenerBet = { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    publicityAdapterListener.onClickBetListener(
                        data.gameType,
                        data.matchType ?: MatchType.EARLY,
                        matchInfo,
                        odd,
                        playCateCode,
                        playCateName,
                        betPlayCateNameMap,
                        data.menuList.firstOrNull()?.code
                    )
                },
                clickListenerQuickCateTab = { _, _ ->
                    //do nothing
                },
                clickListenerQuickCateClose = {
                    //do nothing
                },
                clickListenerFavorite = {
                    it?.let{ matchId ->
                        publicityAdapterListener.onClickFavoriteListener(matchId)
                    }
                },
                clickListenerStatistics = {
                    data.matchInfo?.id?.let { matchId ->
                        publicityAdapterListener.onClickStatisticsListener(matchId)
                    }
                }, refreshListener = {
                    //do nothing
                })
        }

        with(binding) {
            //GameTypeView
            with(gameTypeView) {
                tvSportType.text = getGameTypeString(root.context, data.gameType)
                ivSportType.setImageResource(getGameTypeWhiteIcon(data.gameType))
                data.matchType?.resId?.let { matchTypeRes ->
                    tvGameType.text = root.context.getString(matchTypeRes)
                }
                tvMatchNum.text = data.matchNum.toString()
            }

            //LeagueView
            with(leagueView) {
                GameConfigManager.getTitleBarBackground(data.gameType)?.let { titleRes ->
                    root.setBackgroundResource(titleRes)
                }
                tvLeagueName.text = data.leagueName
                ivFlag.setImageDrawable(SvgUtil.getSvgDrawable(itemView.context, data.categoryIcon))
            }

            //region 測試 - 資料結構與其他處不同
            val matchOddList = transferMatchOddList(data)
            //endregion

            with(rvLeagueList) {
                layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = leagueOddAdapter
                leagueOddAdapter?.setData(matchOddList, oddsType)
            }

        }
    }

    fun updateLeagueOddList(recommend: Recommend, oddsType: OddsType) {
        leagueOddAdapter?.oddsType = oddsType
        val leagueOddData = leagueOddAdapter?.data
        if (leagueOddData?.isNullOrEmpty() == true) {
            //若沒有資料則直接產生新的
            leagueOddAdapter?.data = transferMatchOddList(recommend)
        } else {
            //更新現有的oddsMap
            with(leagueOddData.first()) {
                oddsMap = recommend.oddsMap
                playCateNameMap = recommend.playCateNameMap
                betPlayCateNameMap = recommend.betPlayCateNameMap
            }
        }
        leagueOddAdapter?.update()
    }

    private fun itemClickEvent() {
        publicityAdapterListener.onItemClickListener()
    }

    private fun transferMatchOddList(recommend: Recommend): MutableList<MatchOdd> {
        with(recommend) {
            return mutableListOf(
                MatchOdd(
                    matchInfo = matchInfo,
                    oddsMap = oddsMap,
                    playCateNameMap = playCateNameMap,
                    betPlayCateNameMap = betPlayCateNameMap
                )
            )
        }
    }
}