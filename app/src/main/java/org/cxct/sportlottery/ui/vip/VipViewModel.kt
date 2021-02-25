package org.cxct.sportlottery.ui.vip

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi.vipService
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.vip.growth.LevelGrowthResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel

class VipViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) :
    BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val userLevelGrowthResult: LiveData<LevelGrowthResult>
        get() = _userLevelGrowthResult
    private val _userLevelGrowthResult = MutableLiveData<LevelGrowthResult>()

    val userInfoResult: LiveData<UserInfoResult>
        get() = _userInfoResult
    private val _userInfoResult = MutableLiveData<UserInfoResult>()

    private fun getUserInfo() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                userInfoRepository.getUserInfo()
            }?.let { result ->
                if (result.success) {
                    _userInfoResult.value = result
                }
            }
        }
    }

    fun getUserLevelGrowth() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                vipService.getUserLevelGrowth()
            }?.let { result ->
                if (result.success) {
                    result.config?.userLevelConfigs?.forEachIndexed { index, config ->
                        Level.values()[index].apply {
                            levelRequirement.growthRequirement = config.growthThreshold
                            levelRequirement.levelId = config.id
                            levelRequirement.levelName = config.name
                        }
                    }
                    Level.values().forEach {
                        it.levelRequirement.growthRequirement
                    }
                    _userLevelGrowthResult.value = result
                    getUserInfo()
                }
            }

        }
    }
}