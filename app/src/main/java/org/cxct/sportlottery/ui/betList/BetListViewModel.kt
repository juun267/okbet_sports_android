package org.cxct.sportlottery.ui.betList

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class BetListViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = BetInfoRepository.parlayList

    fun updateOddsChangeOption(option: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.oddsChangeOption(
                    OddsChangeOptionRequest(option)
                )
            }?.let { result ->
                UserInfoRepository.updateOddsChangeOption(option)
            }
        }
    }
}