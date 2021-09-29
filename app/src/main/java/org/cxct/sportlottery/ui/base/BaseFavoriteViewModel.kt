package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchRequest
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.util.TimeUtil


abstract class BaseFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val myFavoriteRepository: MyFavoriteRepository
) : BaseNoticeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
    //TODO add notify login ui to activity/fragment
    val notifyLogin: LiveData<Boolean>
        get() = mNotifyLogin
    protected val mNotifyLogin = MutableLiveData<Boolean>()

    val notifyMyFavorite = myFavoriteRepository.favorNotify

    val favorMatchOddList: LiveData<List<LeagueOdd>>
        get() = mFavorMatchOddList
    protected val mFavorMatchOddList = MutableLiveData<List<LeagueOdd>>()

    val favorSportList = myFavoriteRepository.favorSportList

    val favorLeagueList = myFavoriteRepository.favorLeagueList

    val favorMatchList = myFavoriteRepository.favorMatchList

    val favorPlayCateList = myFavoriteRepository.favorPlayCateList

    val favoriteOutrightList = myFavoriteRepository.favoriteOutrightList

    fun getFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        viewModelScope.launch {
            doNetwork(androidContext) {
                myFavoriteRepository.getFavorite()
            }
        }
    }

    fun getFavoriteMatch(gameType: String?, playCateMenu: String?, playCateCode: String? = null) {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        if (gameType == null || playCateMenu == null) {
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.favoriteService.getMyFavoriteMatch(
                    MyFavoriteMatchRequest(gameType, playCateMenu)
                )
            }

            result?.sortOdds()

            result?.rows?.let {
                it.forEach { leagueOdd ->
                    leagueOdd.apply {
                        this.gameType = GameType.getGameType(gameType)
                        this.matchOdds.forEach { matchOdd ->
                            matchOdd.matchInfo?.isFavorite = true
                            playCateCode?.let {
                                matchOdd.oddsMap =
                                    matchOdd.oddsMap
                                        .filter { odds -> odds.key == playCateCode }
                                        .toMutableFormat()
                            }
                            matchOdd.playCateMappingList = playCateMappingList
                        }
                    }

                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.matchInfo?.let { matchInfo ->
                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

                            matchOdd.matchInfo.startTimeDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

                            matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)
                        }

                        matchOdd.playCateMappingList = playCateMappingList
                    }
                }

                mFavorMatchOddList.postValue(it.updateMatchType())
            }
        }
    }

    fun clearFavorite() {
        myFavoriteRepository.clearFavorite()
    }

    fun notifyFavorite(type: FavoriteType) {
        myFavoriteRepository.notifyFavorite(type)
    }

    fun pinFavorite(
        type: FavoriteType,
        content: String?,
        gameType: String? = null
    ) {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                myFavoriteRepository.pinFavorite(type, content, gameType)
            }

            result?.t?.let {
                if (content == null) return@let

                when (type) {
                    FavoriteType.MATCH -> {
                        mFavorMatchOddList.postValue(
                            mFavorMatchOddList.value?.removeFavorMatchOdd(
                                content
                            )?.removeFavorLeague()
                        )
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun List<LeagueOdd>.removeFavorMatchOdd(matchId: String): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.remove(
                leagueOdd.matchOdds.find { matchOdd ->
                    matchOdd.matchInfo?.id == matchId
                }
            )
        }

        return this
    }

    private fun List<LeagueOdd>.removeFavorLeague(): List<LeagueOdd> {
        val list = this.toMutableList()

        list.remove(list.find {
            it.matchOdds.isNullOrEmpty()
        })

        return list.toList()
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun MyFavoriteMatchResult.sortOdds() {
        this.rows?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                val sortOrder = matchOdd.oddsSort?.split(",")
                matchOdd.oddsMap = matchOdd.oddsMap.toSortedMap(compareBy<String> {
                    val oddsIndex = sortOrder?.indexOf(it)
                    oddsIndex
                }.thenBy { it })
            }
        }
    }

    private fun List<LeagueOdd>.updateMatchType(): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->

                matchOdd.matchInfo?.isInPlay =
                    System.currentTimeMillis() > matchOdd.matchInfo?.startTime ?: 0

                matchOdd.matchInfo?.isAtStart =
                    TimeUtil.getRemainTime(matchOdd.matchInfo?.startTime) < 60 * 60 * 1000L
            }
        }
        return this
    }
}