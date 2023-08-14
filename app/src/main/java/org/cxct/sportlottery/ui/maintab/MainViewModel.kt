package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.TimeUtil


class MainViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val token
        get() = loginRepository.token

    val userId = loginRepository.userId


    private val _inplayList = MutableLiveData<List<Item>>()
    val inplayList: LiveData<List<Item>>
        get() = _inplayList

    fun updateOddsChangeOption(option: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.oddsChangeOption(
                    OddsChangeOptionRequest(option)
                )
            }?.let { result ->
                userInfoRepository.updateOddsChangeOption(option)
            }
        }
    }


    fun getInPlayList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }?.sportMenuData?.let { sportMenuList ->
                _inplayList.postValue(sportMenuList.menu.inPlay.items)
            }
        }
    }


}