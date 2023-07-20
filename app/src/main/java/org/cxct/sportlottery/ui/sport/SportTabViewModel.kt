package org.cxct.sportlottery.ui.sport

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.TimeUtil

class SportTabViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    sportMenuRepository: SportMenuRepository,
) : SportListViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
    sportMenuRepository
) {

    val sportMenuResult: LiveData<ApiResult<SportMenuData>>
        get() = _sportMenuResult
    private val _sportMenuResult = SingleLiveEvent<ApiResult<SportMenuData>>()

    fun getMatchData() {
        callApi({
            SportRepository.getSportMenu(
                TimeUtil.getNowTimeStamp().toString(),
                TimeUtil.getTodayStartTimeStamp().toString())
        }) {
            if (it.succeeded()) {
                it.getData()?.sortSport()
                _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
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

    fun setSportMenuResult(sportMenuResult: ApiResult<SportMenuData>) {
        _sportMenuResult.postValue(sportMenuResult)
    }
}