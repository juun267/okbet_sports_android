package org.cxct.sportlottery.ui.maintab.home.game.live

import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElecGamesFragement

class LiveGamesFragement: ElecGamesFragement<OKGamesViewModel, FragmentGamevenueBinding>() {

    override fun onInitData() {
        loading()
        viewModel.getOKLiveHall()
    }

}