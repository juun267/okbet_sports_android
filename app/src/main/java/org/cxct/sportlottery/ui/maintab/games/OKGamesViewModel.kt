package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class OKGamesViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    sportMenuRepository: SportMenuRepository,
    private val thirdGameRepository: ThirdGameRepository,
): MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
    sportMenuRepository,
) {
    val collectOkGamesResult: LiveData<Pair<Int, Boolean>>
        get() = _collectOkGamesResult
    private val _collectOkGamesResult = MutableLiveData<Pair<Int, Boolean>>()

    fun collectOKGames(id: Int) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.collectOkGames(id)
            }
            result?.let {
                _collectOkGamesResult.postValue(Pair(id, it.success))
            }
        }
    }


}