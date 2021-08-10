package org.cxct.sportlottery.ui.game.home

import org.cxct.sportlottery.network.odds.list.MatchOdd

interface OnClickMoreListener {
    fun onClickMore(matchOdd: MatchOdd)
}