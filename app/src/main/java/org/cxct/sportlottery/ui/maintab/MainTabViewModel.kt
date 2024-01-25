package org.cxct.sportlottery.ui.maintab

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class MainTabViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val showBetUpperLimit = BetInfoRepository.showBetUpperLimit
    val showBetBasketballUpperLimit = BetInfoRepository.showBetBasketballUpperLimit
}