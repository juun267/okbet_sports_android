package org.cxct.sportlottery.ui.game.home

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd

interface OnSubscribeChannelHallListener {
    fun subscribeChannel(gameType: String?, cateMenuCode: String?, eventId: String?)
}