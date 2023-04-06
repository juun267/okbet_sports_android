package org.cxct.sportlottery.ui.betList.listener

import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter


interface OnSelectedPositionListener {
        fun onSelectChange(position: Int, single: BetListRefactorAdapter.BetViewType)
    }