package org.cxct.sportlottery.ui.sport.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.setupQuickPlayCate
import org.cxct.sportlottery.util.sortQuickPlayCate


class FavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {
    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    val sportQueryData: LiveData<Event<SportQueryData?>>
        get() = _sportQueryData
    private val _sportQueryData = MutableLiveData<Event<SportQueryData?>>()
//    private val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單
//    val sportCodeList: LiveData<List<StatusSheetData>>
//        get() = _sportCodeSpinnerList

    val favoriteRepository = myFavoriteRepository
    val lastSportType = myFavoriteRepository.lastSportType


    /**
     * 將新資料配置原本玩法篩選的狀態
     * @see org.cxct.sportlottery.network.sport.query.Play isSelected 選中狀態 isLocked 可下拉篩選是否有被點選過的狀態
     * @see org.cxct.sportlottery.network.sport.query.PlayCate isSelected 可下拉篩選被選中的玩法
     */
    private fun org.cxct.sportlottery.network.sport.query.Item?.setupPlayState() {
        val selectedPlayCate =
            sportQueryData.value?.peekContent()?.items?.find { it.code == this?.code }
        this?.play?.forEach { newSportPlay ->
            val oldSportPlay =
                selectedPlayCate?.play?.find { oldSportPlay -> newSportPlay.code == oldSportPlay.code }
            newSportPlay.isSelected = oldSportPlay?.isSelected ?: false
            oldSportPlay?.let { oldPlay ->
                newSportPlay.isLocked = oldPlay.isLocked
            }

            newSportPlay.playCateList?.forEach { newPlayCate ->
                val oldPlayCate =
                    oldSportPlay?.playCateList?.find { oldPlayCate -> oldPlayCate.code == newPlayCate.code }
                newPlayCate.isSelected = oldPlayCate?.isSelected ?: false
            }
        }
    }

    fun getQuickList(matchId: String?) {
        if (matchId == null) return

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getQuickList(
                    QuickListRequest(matchId)
                )
            }

            result?.quickListData?.let { quickListData ->
                val discount = userInfo.value?.discount ?: 1.0F
                quickListData.quickOdds?.forEach { (_, quickOddsValue) ->
                    quickOddsValue.forEach { (key, value) ->
                        value?.forEach { odd ->
                            odd?.odds = odd?.odds?.applyDiscount(discount)
                            odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)

                            if (key == QuickPlayCate.QUICK_EPS.value) {
                                odd?.extInfo =
                                    odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                            }
                        }
                    }
                }

                val list = mFavorMatchOddList.value?.peekContent()
                    ?.updatePlayCate(matchId, quickListData, quickListData.playCateNameMap)
                mFavorMatchOddList.postValue(Event(list ?: listOf()))
            }
        }
    }


    fun switchGameType(item: Item) {
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updateGameTypeSelected(item).apply {
                    val curPlayList = this?.items?.find { it.isSelected }?.play

                    curPlayList?.forEach {
                        it.isSelected = (curPlayList.indexOf(it) == 0)
                    }
                }
            )
        )
        favoriteRepository.setLastSportType(item)
        getFavoriteMatch(item.code, playCateMenu = MenuCode.MAIN.code)

    }



    private fun SportQueryData.updateGameTypeSelected(item: Item): SportQueryData {
        this.items?.forEach {
            it.isSelected = (it.code == item.code)
        }
        return this
    }

    private fun SportQueryData.updatePlaySelected(play: Play): SportQueryData {
        this.items?.find { it.isSelected }?.play?.forEach {
            it.isSelected = (it == play)
        }
        return this
    }

    private fun SportQueryData.updatePlayCateSelected(playCateCode: String?): SportQueryData {
        this.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList?.forEach {
            it.isSelected = (it.code == playCateCode)
        }
        return this
    }

    private fun List<LeagueOdd>.updatePlayCate(
        matchId: String,
        quickListData: QuickListData,
        quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    val quickOddsApi = when (quickPlayCate.code) {
                        QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                        else -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                    }?.apply {
                        setupQuickPlayCate(quickPlayCate.code ?: "")
                        sortQuickPlayCate(quickPlayCate.code ?: "")
                    }

                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds.putAll(
                        quickOddsApi?.toMutableFormat() ?: mutableMapOf()
                    )
                }
                matchOdd.quickPlayCateNameMap = quickPlayCateNameMap
            }
        }
        return this
    }

    private fun List<LeagueOdd>.clearQuickPlayCateSelected(): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }
        return this
    }

    /**
     * 獲取當前可用球種清單
     */
    fun getSportList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportList(type = 1)
            }?.let { sportListResponse ->
                if (sportListResponse.success) {
                    val sportCodeList = mutableListOf<StatusSheetData>()
                    //第一項為全部球種
                    sportCodeList.add(StatusSheetData("", LocalUtils.getString(R.string.all_sport)))
                    //根據api回傳的球類添加進當前啟用球種篩選清單
                    sportListResponse.rows.sortedBy { it.sortNum }.map {
                        if (it.state == 1) { //僅添加狀態為啟用的資料
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
                    }

                    withContext(Dispatchers.Main) {
                        _sportCodeSpinnerList.value = sportCodeList
                    }
                }
            }
        }
    }
}