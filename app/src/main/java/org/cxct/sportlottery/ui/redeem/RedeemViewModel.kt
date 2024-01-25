package org.cxct.sportlottery.ui.redeem

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.RedeemCodeHistoryResponse
import org.cxct.sportlottery.network.money.RedeemCodeResponse
import org.cxct.sportlottery.network.money.list.SportBillListRequest
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.TimeUtil

class RedeemViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    var page = 1
    private val _newsResult = MutableLiveData<RedeemCodeResponse>()
    val newsResult: LiveData<RedeemCodeResponse>
        get() = _newsResult

    private val _codeHistory = MutableLiveData<RedeemCodeHistoryResponse>()
    val codeHistory: LiveData<RedeemCodeHistoryResponse>
        get() = _codeHistory

    fun redeemCode(code: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.moneyService.redeemCode(code)
            }?.let {
                _newsResult.postValue(it)
            }
        }
    }

    fun redeemCodeHistory(
        page: Int? = 1,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
    ) {
        if (page != null) {
            this.page = page
        } else {
            this.page = 1
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.moneyService.redeemCodeHistory(
                    SportBillListRequest(
                        startTime = startTime,
                        endTime = endTime,
                        page = page
                    )
                )
            }?.let {
                if (it.success) {
                    _codeHistory.postValue(it)
                }
            }
        }
    }

}