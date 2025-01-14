package org.cxct.sportlottery.network.money

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event

class RedEnveLopeModel(
    androidContext: Application,
) : BaseSocketViewModel(
    androidContext
) {
    private val _redEnvelopePrizeResult = MutableLiveData<Event<RedEnvelopePrizeResult?>>()
    val redEnvelopePrizeResult: LiveData<Event<RedEnvelopePrizeResult?>>
        get() = _redEnvelopePrizeResult

    fun getRedEnvelopePrize(redEnpId: Int?) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.moneyService.getRedEnvelopePrize(redEnpId)
            }?.let { result ->
                _redEnvelopePrizeResult.postValue(Event(result))
            }
        }
    }
}