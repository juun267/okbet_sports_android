package org.cxct.sportlottery.ui.profileCenter.cancelaccount

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class CancelAccountViewModel(
    androidContext: Application,
    private val feedbackRepository: FeedbackRepository,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
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