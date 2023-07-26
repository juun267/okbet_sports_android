package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
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

    private val _promoteNoticeResult = MutableLiveData<Event<MessageListResult>>()
    private val _countByInPlay = MutableLiveData<Int>()
    val countByInPlay: LiveData<Int>
        get() = _countByInPlay

    private val _countByToday = MutableLiveData<Int>()
    val countByToday: LiveData<Int>
        get() = _countByToday

    private val _inplayList = MutableLiveData<List<Item>>()
    val inplayList: LiveData<List<Item>>
        get() = _inplayList
    private val _liveRoundCount = MutableLiveData<String>()
    val liveRoundCount: LiveData<String>
        get() = _liveRoundCount

    //獲取系統公告及跑馬燈
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result ->
                _promoteNoticeResult.postValue(Event(result))
            }
        }
    }

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

    fun getSportList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }?.sportMenuData?.let { sportMenuList ->
                _countByToday.postValue(sportMenuList.menu.today.num)
                val sportCodeList = mutableListOf<StatusSheetData>()
                sportMenuList.menu.early.items.forEach {
                    sportCodeList.add(
                        StatusSheetData(
                            it.code,
                            GameType.getGameTypeString(
                                LocalUtils.getLocalizedContext(),
                                it.code
                            )
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    _sportCodeSpinnerList.value = sportCodeList
                }
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
                _countByInPlay.postValue(sportMenuList.menu.inPlay.num)
            }
        }
    }


}