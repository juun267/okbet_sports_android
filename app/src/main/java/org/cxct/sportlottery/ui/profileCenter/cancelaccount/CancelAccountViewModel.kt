package org.cxct.sportlottery.ui.profileCenter.cancelaccount

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class CancelAccountViewModel(
    androidContext: Application,
) : BaseSocketViewModel(
    androidContext
)  {
    //请求结果回调给activity
    val cancelResult: LiveData<CancelAccountResult>
        get() = _cancelResult
    private val _cancelResult = MutableLiveData<CancelAccountResult>()
    //注销账号接口
    fun cancelAccount(password: String){
        viewModelScope.launch {
             doNetwork(androidContext) {
                OneBoSportApi.indexService.cancelAccount(password)
            }?.let {
                 _cancelResult.postValue(it)
             }

        }
    }

}