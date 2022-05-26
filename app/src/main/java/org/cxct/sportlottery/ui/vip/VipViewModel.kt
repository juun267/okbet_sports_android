package org.cxct.sportlottery.ui.vip

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi.thirdGameService
import org.cxct.sportlottery.network.OneBoSportApi.vipService
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.vip.LoadingResult
import org.cxct.sportlottery.network.vip.growth.LevelGrowthResult
import org.cxct.sportlottery.network.vip.thirdRebates.Debate
import org.cxct.sportlottery.network.vip.thirdRebates.ThirdRebatesResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class VipViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    val userLevelGrowthResult: LiveData<LevelGrowthResult>
        get() = _userLevelGrowthResult
    private val _userLevelGrowthResult = MutableLiveData<LevelGrowthResult>()

    val userInfoResult: LiveData<UserInfoResult>
        get() = _userInfoResult
    private val _userInfoResult = MutableLiveData<UserInfoResult>()

    val thirdRebatesReformatDataList: LiveData<List<Debate>>
        get() = _thirdRebatesReformatDataList
    private val _thirdRebatesReformatDataList = MutableLiveData<List<Debate>>()

    val loadingResult: LiveData<LoadingResult>
        get() = _loadingResult
    private val _loadingResult = MutableLiveData<LoadingResult>(LoadingResult())

    //第三方遊戲資料
    val getThirdGamesFirmMap: LiveData<List<GameFirmValues>>
        get() = _getThirdGamesFirmMap
    private val _getThirdGamesFirmMap = MutableLiveData<List<GameFirmValues>>()

    private fun getUserInfo() {
        _loadingResult.value = _loadingResult.value?.apply { userInfoLoading = true }
        viewModelScope.launch {
            doNetwork(androidContext) {
                userInfoRepository.getUserInfo()
            }?.let { result ->
                if (result.success) {
                    _userInfoResult.value = result
                }
                _loadingResult.value = _loadingResult.value?.apply { userInfoLoading = false }
            }
        }
    }

    fun getUserLevelGrowth() {
        _loadingResult.value = _loadingResult.value?.apply { userGrowthLoading = true }
        viewModelScope.launch {
            doNetwork(androidContext) {
                vipService.getUserLevelGrowth()
            }?.let { result ->
                if (result.success) {
                    result.config?.userLevelConfigs?.forEachIndexed { index, config ->
                        Level.values()[index].apply {
                            levelRequirement.growthRequirement = config.growthThreshold
                            levelRequirement.levelId = config.id
                            levelRequirement.levelName = config.name //TODO Bill 請後端回傳多語系
                        }
                    }
                    Level.values().forEach {
                        it.levelRequirement.growthRequirement
                    }
                    _userLevelGrowthResult.value = result
                    getUserInfo()
                }
                _loadingResult.value = _loadingResult.value?.apply { userGrowthLoading = false }
            }

        }
    }

    fun getThirdRebates(firmCode: String, firmType: String) {
        _loadingResult.value = _loadingResult.value?.apply { thirdRebatesLoading = true }
        viewModelScope.launch {
            doNetwork(androidContext) {
                vipService.getThirdRebates(firmCode, firmType)
            }?.let { result ->
                if (result.success) {
                    _thirdRebatesReformatDataList.value = reorganizeThirdRebatesData(result)
                }
                _loadingResult.value = _loadingResult.value?.apply { thirdRebatesLoading = false }
            }
        }
    }

    /**
     * 獲取第三方遊戲列表
     */
    fun getThirdGamesFirmMap() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                thirdGameService.getThirdGames()
            }?.let { result ->
                if (result.success) {
                    result.t?.gameFirmMap?.let {
                        //過濾掉禁用的第三方遊戲
                        val gameFirmList = mutableListOf<GameFirmValues>()
                        it.toList().sortedBy { list -> list.second.sort }.forEach { pair ->
                            if (pair.second.open == 1) {
                                gameFirmList.add(pair.second)
                            }
                        }
                        _getThirdGamesFirmMap.value = gameFirmList
                    }
                }
            }
        }
    }

    /**
     * 重組獲取的第三方反水資料至單層list
     */
    private fun reorganizeThirdRebatesData(thirdRebatesResult: ThirdRebatesResult): List<Debate> {
        val reformattedDataList = mutableListOf<Debate>()
        thirdRebatesResult.thirdRebates?.thirdDebateBeans?.forEach { thirdDebateBeans ->
            if (thirdDebateBeans.debateList.isEmpty()) {
                thirdDebateBeans.apply {
                    val emptyItem = Debate(null, 0.0, null, null, null, userLevelId, userLevelName).apply {
                        isNullTail = true
                    }
                    reformattedDataList.add(emptyItem)
                }
            } else {
                thirdDebateBeans.apply {
                    val emptyItem = Debate(null, 0.0, null, null, null, userLevelId, userLevelName).apply {
                        isTitle = true
                    }
                    reformattedDataList.add(emptyItem)
                }
            }

            thirdDebateBeans.debateList.forEachIndexed { index, debate ->
                //處理是否為該層級的第一筆或最後一筆反水資料
                if (thirdDebateBeans.debateList.lastIndex == index) {
                    if (debate.id == 0.0)
                        debate.isNullTail = true
                    else
                        debate.isLastContent = true
                }
                debate.levelIndex = index - 1

                reformattedDataList.add(debate)
            }

        }

        return reformattedDataList
    }
}