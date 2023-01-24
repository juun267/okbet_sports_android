package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.lottery.LotteryInfo
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel

class MainTabViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val showBetUpperLimit = betInfoRepository.showBetUpperLimit
    val lotteryInfo: LiveData<LotteryInfo>
        get() = _lotteryInfo
    private val _lotteryInfo = MutableLiveData<LotteryInfo>()

    fun getLotteryInfo() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.lotteryService.getLotteryResult()
            }
            result?.t?.let {
                _lotteryInfo.postValue(it)
            }
        }
    }
}