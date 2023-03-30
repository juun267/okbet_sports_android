package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.TimeUtil

class SportTabViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {
    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private var sportMenuData: SportMenuData? = null //球種菜單資料

    val curMatchType: LiveData<MatchType?>
        get() = _curMatchType
    private val _curMatchType = MutableLiveData<MatchType?>()

    fun firstSwitchMatch(matchType: MatchType?) {
        if (_sportMenuResult.value == null) {
            _curMatchType.postValue(matchType)
        }
    }


    fun getMatchData() {
        viewModelScope.launch {
            val sportMenuResult = getSportMenuAll()
            sportMenuResult?.let {
                if (it.success) {
                    _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
                }
            }
        }
    }

    //滾球、今日、早盤、冠軍、串關、(即將跟menu同一層)
    private suspend fun getSportMenuAll(): SportMenuResult? {
        return doNetwork(androidContext) {
            sportMenuRepository.getSportMenu(
                TimeUtil.getNowTimeStamp().toString(),
                TimeUtil.getTodayStartTimeStamp().toString()
            ).apply {
                if (isSuccessful && body()?.success == true) {
                    // 每次執行必做
                    body()?.sportMenuData?.sortSport().apply { sportMenuData = this }
                }
            }
        }
    }

    private fun SportMenuData.sortSport(): SportMenuData {
        this.menu.inPlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.today.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.early.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.cs.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.eps?.items?.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }

    fun setCurMatchType(matchType: MatchType?) {
        _curMatchType.postValue(matchType)
    }

    fun setSportMenuResult(sportMenuResult: SportMenuResult) {
        _sportMenuResult.postValue(sportMenuResult)
    }
}