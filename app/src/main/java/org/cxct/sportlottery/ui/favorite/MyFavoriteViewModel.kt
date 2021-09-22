package org.cxct.sportlottery.ui.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil


class MyFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository
) {
    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    val sportQueryData: LiveData<Event<SportQueryData?>>
        get() = _sportQueryData
    private val _sportQueryData = MutableLiveData<Event<SportQueryData?>>()

    val curPlay: LiveData<Play>
        get() = _curPlay
    private val _curPlay = MutableLiveData<Play>()

    fun getSportQuery() {
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

            result?.sportQueryData?.let {

                _sportQueryData.postValue(Event(
                    it.apply {
                        it.items?.firstOrNull()?.apply {
                            this.isSelected = true
                            this.play?.firstOrNull()?.isSelected = true
                        }
                    }
                ))

                getFavoriteMatch(
                    it.items?.firstOrNull()?.code,
                    it.items?.firstOrNull()?.play?.firstOrNull()?.code
                )
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
                mFavorMatchOddList.postValue(
                    mFavorMatchOddList.value?.updatePlayCate(matchId, quickListData)
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

        getFavoriteMatch(
            item.code,
            _sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.play?.firstOrNull()?.code
        )
    }

    fun switchPlay(play: Play) {
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

        if (play.selectionType == SelectionType.SELECTABLE.code) {
            _curPlay.postValue(play)
        } else {
            getFavoriteMatch(
                sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.code,
                play.code
            )
        }
    }

    fun switchPlayCategory(playCateCode: String?) {
        getFavoriteMatch(
            sportQueryData.value?.peekContent()?.items?.find { it.isSelected }?.code,
            MenuCode.MAIN.code,
            playCateCode
        )
        _sportQueryData.postValue(
            Event(
                _sportQueryData.value?.peekContent()?.updatePlayCateSelected(playCateCode)
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
        quickListData: QuickListData
    ): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds =
                        quickListData.quickOdds?.get(quickPlayCate.code)
                            ?.filterPlayCateSpanned(matchOdd.matchInfo?.gameType)?.splitPlayCate()?.sortPlayCate()
                }
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
}