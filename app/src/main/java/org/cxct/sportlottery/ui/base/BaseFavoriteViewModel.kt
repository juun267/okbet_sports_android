package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchRequest
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.util.Event
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

    val leftNotifyLogin: LiveData<Event<Boolean>>
        get() = _leftNotifyLogin
    private val _leftNotifyLogin = MutableLiveData<Event<Boolean>>()

    val leftNotifyFavorite: LiveData<Event<Int?>>
        get() = _leftNotifyFavorite
    private val _leftNotifyFavorite = MutableLiveData<Event<Int?>>()

    val notifyMyFavorite = myFavoriteRepository.favorNotify

    val favorMatchOddList: LiveData<Event<List<LeagueOdd>>>
        get() = mFavorMatchOddList
    protected val mFavorMatchOddList = MutableLiveData<Event<List<LeagueOdd>>>()

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
            mFavorMatchOddList.postValue(Event(listOf()))
            return
        }

        getMyFavoriteMatch(gameType, playCateMenu, playCateCode)
    }

    private fun getMyFavoriteMatch(
        gameType: String,
        playCateMenu: String,
        playCateCode: String?,
    ) {
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
                                val oddsMap = matchOdd.oddsMap
                                    ?.filter { odds -> odds.key == it }
                                    ?.toMutableFormat()

                                matchOdd.oddsMap?.clear()
                                if (oddsMap != null) {
                                    matchOdd.oddsMap?.putAll(oddsMap)
                                }
                            }
                            matchOdd.playCateMappingList = playCateMappingList
                        }
                    }

                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.setupOddDiscount()
                        matchOdd.matchInfo?.let { matchInfo ->
                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "dd/MM")

                            matchInfo.startTimeDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

                            matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)

                            /* #1 將賽事狀態(先前socket回傳取得)放入當前取得的賽事 */
                            val mInfo = mFavorMatchOddList.value?.peekContent()?.find { lo ->
                                lo.league.id == leagueOdd.league.id
                            }?.matchOdds?.find { mo ->
                                mo.matchInfo?.id == matchInfo.id
                            }?.matchInfo

                            matchInfo.socketMatchStatus = mInfo?.socketMatchStatus
                            matchInfo.statusName18n = mInfo?.statusName18n
                            matchInfo.homeScore = mInfo?.homeScore
                            matchInfo.awayScore = mInfo?.awayScore
                        }
                        matchOdd.playCateMappingList = playCateMappingList
                    }
                }
                mFavorMatchOddList.postValue(Event(it.updateMatchType()))
            }
        }
    }

    private fun MatchOdd.setupOddDiscount() {
        val discount = userInfo.value?.discount ?: 1.0F
        this.oddsMap?.forEach { (key, value) ->
            value?.forEach { odd ->
                if (key == PlayCate.EPS.value)
                    odd?.setupEPSDiscount(discount)
                else
                    odd?.setupDiscount(discount)
            }
        }
    }

    /**
     * 設置大廳所需顯示的玩法 (api未回傳的玩法需以“—”表示)
     */
    private fun MatchOdd.setupPlayCate() {
        val sortOrder = this.oddsSort?.split(",")
        this.oddsMap?.let { oddsMap ->
            sortOrder?.forEach {
                if (!oddsMap.keys.contains(it))
                    oddsMap[it] = mutableListOf(null, null, null)
            }
        }
    }

    private fun MatchOdd.sortOdd() {
        val sortOrder = this.oddsSort?.split(",")
        val oddsMap = this.oddsMap?.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it)
            oddsIndex
        }.thenBy { it })

        this.oddsMap?.let { it ->
            it.clear()
            if (oddsMap != null) {
                it.putAll(oddsMap)
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
                        val list = mFavorMatchOddList.value?.peekContent()?.removeFavorMatchOdd(content)?.removeFavorLeague()
                        mFavorMatchOddList.postValue(Event(list ?: listOf()))
                    }

                    else -> {
                    }
                }
            }
        }
    }

    fun leftPinFavorite(gameType: String?, addOrRemove: Int?) {
        if (isLogin.value != true) {
            _leftNotifyLogin.postValue(Event(true))
            return
        }

        pinFavorite(
            FavoriteType.SPORT,
            gameType
        )

        _leftNotifyFavorite.postValue(Event(addOrRemove))
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
                val oddsMap = matchOdd.oddsMap?.toSortedMap(compareBy<String> {
                    val oddsIndex = sortOrder?.indexOf(it)
                    oddsIndex
                }.thenBy { it })

                matchOdd.oddsMap?.clear()
                if (oddsMap != null) {
                    matchOdd.oddsMap?.putAll(oddsMap)
                }
            }
        }
    }

    private fun List<LeagueOdd>.updateMatchType(): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->

                matchOdd.matchInfo?.isInPlay =
                    System.currentTimeMillis() > matchOdd.matchInfo?.startTime ?: 0

                matchOdd.matchInfo?.isAtStart =
                    TimeUtil.isTimeAtStart(matchOdd.matchInfo?.startTime)
            }
        }
        return this
    }
}