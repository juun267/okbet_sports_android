package org.cxct.sportlottery.ui.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount


class MyFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    intentRepository: IntentRepository
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
    intentRepository
) {
    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    val sportQueryData: LiveData<Event<SportQueryData?>>
        get() = _sportQueryData
    private val _sportQueryData = MutableLiveData<Event<SportQueryData?>>()

    val favoriteRepository = myFavoriteRepository
    val lastSportType = myFavoriteRepository.lastSportType

    fun getSportQuery(getLastPick:Boolean? = false) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        MatchType.MY_EVENT.postValue,
                        matchIdList = favorMatchList.value
                    )
                )
            }



            result?.sportQueryData?.let { sportQueryData ->
                _sportQueryData.postValue(Event(
                    sportQueryData.apply {
                        if (!sportQueryData.items?.filter { it.code == lastSportType.value?.code }.isNullOrEmpty() && getLastPick == true){
                            sportQueryData.items?.find { it.code == lastSportType.value?.code }.apply {
                                this?.isSelected = true
                                this?.play?.firstOrNull()?.isSelected = true
                            }
                        }else{
                            sportQueryData.items?.firstOrNull()?.apply {
                                this.isSelected = true
                                this.play?.firstOrNull()?.isSelected = true
                            }
                        }
                    }
                ))

                val selectItem = sportQueryData.items?.find { it.isSelected }
                getFavoriteMatch(
                    selectItem?.code,
                    selectItem?.play?.firstOrNull()?.code
                )
            }
        }
    }

    //獲取體育篩選菜單
    fun getSportMenuFilter() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportListFilter()
            }

            result?.let {
                PlayCateMenuFilterUtils.filterList = it.t?.sportMenuList
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
                                odd?.extInfo = odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                            }
                        }
                    }
                }

                mFavorMatchOddList.postValue(
                    mFavorMatchOddList.value?.updatePlayCate(matchId, quickListData, quickListData.playCateNameMap)
                )
            }
        }
    }

    fun getFavoriteMatch() {
        val gameType = sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.code
        val playCateMenu =
            sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.play?.find { it.isSelected }?.code
        val playCateCode =
            sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList?.find { it.isSelected }?.code

        getFavoriteMatch(gameType, playCateMenu, playCateCode)
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
        getFavoriteMatch(
            item.code,
            _sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.play?.firstOrNull()?.code
        )
    }

    fun switchPlay(play: Play) {

        if (play.selectionType == SelectionType.SELECTABLE.code) {
            _sportQueryData.postValue(
                Event(
                    _sportQueryData.value?.peekContent()?.updatePlaySelected(play)
                )
            )
            val playCateCode =
                sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList?.find { it.isSelected }?.code

            switchPlayCategory(play,playCateCode)
        } else {
            _sportQueryData.postValue(
                Event(
                    _sportQueryData.value?.peekContent()?.updatePlaySelected(play).apply {
                        val curPlayCate =
                            this?.items?.find { it.isSelected }?.play?.find { it.isSelected }?.playCateList

                        curPlayCate?.forEach {
                            it.isSelected =
                                (curPlayCate.indexOf(it) == 0)
                                        && (this?.items?.find { item -> item.isSelected }?.play?.find { play -> play.isSelected }?.selectionType == SelectionType.SELECTABLE.code)
                        }
                    }
                )
            )
        }
    }

    fun switchPlayCategory(play: Play,playCateCode: String?) {
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updatePlaySelected(play)?.updatePlayCateSelected(playCateCode)
            )
        )
    }

    fun clearQuickPlayCateSelected() {
        mFavorMatchOddList.postValue(
            mFavorMatchOddList.value?.clearQuickPlayCateSelected()
        )
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
        quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
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

    fun getIsFastBetOpened(): Boolean {
        return betInfoRepository.getIsFastBetOpened()
    }

}