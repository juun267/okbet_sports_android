package org.cxct.sportlottery.ui.maintab

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils

class MainTabViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val showBetUpperLimit = BetInfoRepository.showBetUpperLimit
    val showBetBasketballUpperLimit = BetInfoRepository.showBetBasketballUpperLimit
    //獲取體育篩選菜單
    fun getSportMenuFilter() {
        if (!PlayCateMenuFilterUtils.filterList.isNullOrEmpty()){
            return
        }
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportListFilter()
            }
            result?.let {
                PlayCateMenuFilterUtils.filterList = it.t?.sportMenuList
            }
        }
    }
    //获取三方游戏收藏数量
    fun getGameCollectNum() {
        callApiWithNoCancel({OKGamesRepository.getGameCollectNum()}){}
    }
    fun getThirdGames() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }?.let { result ->
                result.t?.gameFirmMap?.values?.toList().let {
                    OKGamesRepository.gameFiremEvent.postValue(it)
                }
            }
        }
    }
}