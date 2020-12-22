package org.cxct.sportlottery.ui.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.ui.base.BaseViewModel

class HomeViewModel : BaseViewModel() {

    //按赛事类型预加载各体育赛事
    fun getMatchPreload() {
        viewModelScope.launch {
            try {
                val earlyRequest = MatchPreloadRequest("EARLY")
                val earlyResponse = OneBoSportApi.matchService.getMatchPreload(earlyRequest)
                if (earlyResponse.isSuccessful) {
                    mBaseResult.postValue(earlyResponse.body())
                } else {
                    val result = ErrorUtils.parseError(earlyResponse)
                    mBaseResult.postValue(result)
                }

                val inPlayRequest = MatchPreloadRequest("INPLAY")
                val inPlayResponse = OneBoSportApi.matchService.getMatchPreload(inPlayRequest)
                if (inPlayResponse.isSuccessful) {
                    mBaseResult.postValue(inPlayResponse.body())
                } else {
                    val result = ErrorUtils.parseError(inPlayResponse)
                    mBaseResult.postValue(result)
                }

                val todayRequest = MatchPreloadRequest("TODAY")
                val todayResponse = OneBoSportApi.matchService.getMatchPreload(todayRequest)
                if (todayResponse.isSuccessful) {
                    mBaseResult.postValue(todayResponse.body())
                } else {
                    val result = ErrorUtils.parseError(todayResponse)
                    mBaseResult.postValue(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO simon test review API error handling
            }
        }
    }

}