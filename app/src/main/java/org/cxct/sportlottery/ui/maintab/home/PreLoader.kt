package org.cxct.sportlottery.ui.maintab.home

import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel

object PreLoader {

    fun startPreload() {
        SportRepository.loadSportMenu()
        OKGamesViewModel.preLoadOKLiveHall()
        OKGamesViewModel.preLoadOKGameHall()
    }

}