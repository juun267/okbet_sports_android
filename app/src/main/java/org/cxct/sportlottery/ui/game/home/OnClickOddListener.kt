package org.cxct.sportlottery.ui.game.home

import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd

interface OnClickOddListener {
    fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateName: String, playName: String)
}