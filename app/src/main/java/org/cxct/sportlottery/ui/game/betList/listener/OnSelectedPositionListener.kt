package org.cxct.sportlottery.ui.game.betList.listener

import org.cxct.sportlottery.ui.game.betList.adapter.BetListRefactorAdapter


interface OnSelectedPositionListener {
        fun onSelectChange(position: Int, single: BetListRefactorAdapter.BetViewType)
    }