package org.cxct.sportlottery.ui.game.home

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd

interface OnClickOddListener {
    fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateName: String, playName: String)
}