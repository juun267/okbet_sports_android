package org.cxct.sportlottery.ui.betRecord.detail

import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBetDetailMatchBinding
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOddsVO
import org.cxct.sportlottery.util.*

class ContentParlayDetailAdapter(val status: Int) :
    BindingAdapter<MatchOddsVO,ItemBetDetailMatchBinding>(){
    var gameType: String = ""
    var matchType: String? = null
    fun setupMatchData(gameType: String, dataList: List<MatchOddsVO>,  matchType: String?) {
        this.gameType = gameType
        this.matchType = matchType
        setList(dataList)
    }


    override fun onBinding(position: Int, binding: ItemBetDetailMatchBinding, item: MatchOddsVO)=binding.run {
        topLine.isVisible = position != 0
        //篮球 滚球 全场让分【欧洲盘】
        contentPlay.setGameType_MatchType_PlayCateName_OddsType(
            gameType,
            matchType,
            item.playCateName,
            item.oddsType
        )

        titleTeamNameParlay.setTeamsNameWithVS(item.homeName, item.awayName)

        parlayPlayContent.setPlayContent(
            item.playName,
            item.spread,
            TextUtil.formatForOdd(item.odds)
        )

        parlayPlayTime.text = TimeUtil.timeFormat(item.startTime, TimeUtil.DM_HM_FORMAT)
        ivCountry.setLeagueLogo(item.categoryIcon)
        contentLeague.text = item.leagueName
    }
}