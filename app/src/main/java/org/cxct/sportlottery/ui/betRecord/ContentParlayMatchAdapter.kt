package org.cxct.sportlottery.ui.betRecord

import android.content.Intent
import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ContentParlayMatchBinding
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.onClick


class ContentParlayMatchAdapter(val rowData: Row, val viewModel: AccountHistoryViewModel) :
    BindingAdapter<MatchOdd,ContentParlayMatchBinding>() {

    var gameType: String = ""
    var betConfirmTime: Long? = 0
    var matchType: String? = null

    fun setupMatchData(
        gameType: String, dataList: List<MatchOdd>, betConfirmTime: Long?, matchType: String?
    ) {
        this.gameType = gameType
        this.betConfirmTime = betConfirmTime
        this.matchType = matchType
        setList(dataList)
    }
    override fun onBinding(position: Int, binding: ContentParlayMatchBinding, item: MatchOdd) {
        ///串关详情跳转
        binding.root.onClick {
            val intent = Intent(context, BetDetailsActivity::class.java)
            intent.putExtra("data", rowData)
            context?.startActivity(intent)
        }
        binding.topLine.isVisible = position != 0
//                content_play.text = "$gameTypeName ${data.playCateName}"
        //篮球 滚球 全场让分【欧洲盘】
        binding.contentPlay.setGameType_MatchType_PlayCateName_OddsType(
            gameType, matchType, item.playCateName, item.oddsType
        )

//                tv_team_names.setTeamNames(15, data.homeName, data.awayName)
        binding.titleTeamNameParlay.setTeamsNameWithVS(item.homeName, item.awayName)

        binding.parlayPlayContent.setPlayContent(
            item.playName, item.spread, TextUtil.formatForOdd(item.odds)
        )

        binding.parlayPlayTime.text = TimeUtil.timeFormat(item.startTime, TimeUtil.DM_HM_FORMAT)
        binding.ivCountry.setLeagueLogo(item.categoryIcon)
        binding.contentLeague.text = item.leagueName
    }

}