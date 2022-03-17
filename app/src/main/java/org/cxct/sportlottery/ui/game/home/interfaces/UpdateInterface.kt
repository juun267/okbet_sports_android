package org.cxct.sportlottery.ui.game.interfaces

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.common.OddButtonListener
import org.cxct.sportlottery.ui.menu.OddsType

// GameV3
interface UpdateLeagueInterface {
    fun doUpdate(item: LeagueOdd,
                 matchType: MatchType,
                 leagueListener: LeagueListener?,
                 leagueOddListener: LeagueOddListener?,
                 oddsType: OddsType)
}

interface UpdateLeagueOddInterface {
    fun doUpdate(matchType: MatchType,
                 item: MatchOdd,
                 leagueOddListener: LeagueOddListener?,
                 isTimerEnable: Boolean,
                 oddsType: OddsType,
                 matchInfoList: List<MatchInfo>)
}

interface UpdateOddButtonPagerInterface {
    fun doUpdate(matchInfo: MatchInfo?,
                 playCateNameMap: Map<String?, Map<String?, String?>?>?,
                 betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
                 odds: List<Pair<String?, List<Odd?>?>>?,
                 oddsType: OddsType,
                 oddButtonListener: OddButtonListener?)
}

interface UpdateOddButtonPairInterface {
    fun doUpdate(oddPair: List<IndexedValue<Odd?>>?,
                 oddsType: OddsType)
}

// Home
interface UpdateHighLightInterface {
    fun doUpdate(data: MatchOdd, lastData: MatchOdd, oddsType: OddsType)
}