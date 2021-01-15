package org.cxct.sportlottery.ui.odds

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class OddsDetailViewModel(
    private val androidContext: Context,
) : BaseViewModel() {

    private val _oddsDetailResult = MutableLiveData<OddsDetailResult?>()
    private val _playCateListResult = MutableLiveData<PlayCateListResult?>()

    val oddsDetailResult: LiveData<OddsDetailResult?>
        get() = _oddsDetailResult

    val playCateListResult: LiveData<PlayCateListResult?>
        get() = _playCateListResult


    fun getOddsDetail(matchId: String, oddsType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsDetail(OddsDetailRequest(matchId, oddsType))
            }
            _oddsDetailResult.postValue(result)
        }
    }

    fun getPlayCateList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.playCateListService.getPlayCateList(gameType)
            }
            _playCateListResult.postValue(result)
        }
    }

}