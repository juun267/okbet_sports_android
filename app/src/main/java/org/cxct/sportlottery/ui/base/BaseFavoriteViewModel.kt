package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchRequest
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
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

    val intoFavoritePage: LiveData<Event<Boolean>>
        get() = _intoFavoritePage
    val _intoFavoritePage = MutableLiveData<Event<Boolean>>()
    val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單
    val sportCodeList: LiveData<List<StatusSheetData>>
        get() = _sportCodeSpinnerList

    fun navFavoritePage(isFromLeftMenu: Boolean = false) {
        if (isLogin.value != true) {
            if (isFromLeftMenu) {
                _leftNotifyLogin.postValue(Event(true))
            } else {
                mNotifyLogin.postValue(true)
            }
            return
        }

        _intoFavoritePage.value = Event(true)
    }

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

    fun getFavoriteMatch(
        gameType: String? = null,
        playCateMenu: String? = MenuCode.MAIN.code,
        playCateCode: String? = null,
    ) {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }
        if (gameType.isNullOrEmpty()) {
            getMyFavoriteAllMatch(
                playCateMenu ?: MenuCode.MAIN.code,
                playCateCode)

        } else {
            getMyFavoriteMatch(gameType,
                playCateMenu ?: MenuCode.MAIN.code,
                playCateCode)
        }

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

            result?.rows?.sortOdds()

            result?.rows?.let {
                it.getPlayCateNameMap()
                it.forEach { leagueOdd ->
                    leagueOdd.apply {
                        this.gameType = GameType.getGameType(gameType)
                        if (this.gameType == null) {
                            this.gameType = GameType.getGameType(matchOdds[0].matchInfo?.gameType!!)
                        }
                        this.matchOdds.forEach { matchOdd ->
                            matchOdd.matchInfo?.isFavorite = true
                            matchOdd.oddsSort =
                                PlayCateMenuFilterUtils.filterOddsSort(matchOdd.matchInfo?.gameType,
                                    MenuCode.MAIN.code)
                            playCateCode?.let {
                                val oddsMap = matchOdd.oddsMap
                                    ?.filter { odds -> odds.key == it }
                                    ?.toMutableFormat()

                                matchOdd.oddsMap?.clear()
                                if (oddsMap != null) {
                                    matchOdd.oddsMap?.putAll(oddsMap)
                                }
                            }
                        }
                    }

                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.setupOddDiscount()
                        matchOdd.matchInfo?.let { matchInfo ->
                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

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
                    }
                }
                mFavorMatchOddList.postValue(Event(it.updateMatchType()))
            }
        }
    }

    private fun getMyFavoriteAllMatch(
        playCateMenu: String,
        playCateCode: String?,
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.favoriteService.getMyFavoriteQueryAll(
                    MyFavoriteMatchRequest(null, playCateMenu)
                )
            }
            var leagueOddList = mutableListOf<LeagueOdd>()
            val sportCodeList = mutableListOf<StatusSheetData>()
            //第一項為全部球種
            sportCodeList.add(StatusSheetData("", LocalUtils.getString(R.string.all_sport)))
            //根據api回傳的球類添加進當前啟用球種篩選清單
            result?.rows?.forEach {
                leagueOddList.addAll(it.leagueOddsList)
                sportCodeList.add(
                    StatusSheetData(
                        it.gameType,
                        GameType.getGameTypeString(
                            LocalUtils.getLocalizedContext(),
                            it.gameType
                        )
                    )
                )
            }
            withContext(Dispatchers.Main) {
                _sportCodeSpinnerList.value = sportCodeList
            }
            leagueOddList.sortOdds()
            leagueOddList.let {
                it.getPlayCateNameMap()
                it.forEach { leagueOdd ->
                    leagueOdd.apply {
                        this.gameType = GameType.getGameType(matchOdds[0].matchInfo?.gameType!!)
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
                        }
                    }

                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.setupOddDiscountFixed()
                        matchOdd.matchInfo?.let { matchInfo ->
                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

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
                    }
                }
                mFavorMatchOddList.postValue(Event(it.updateMatchType()))
            }
        }
    }

    private fun MatchOdd.setupOddDiscountFixed() {

        val discount = userInfo.value?.discount ?: 1.0F

        this.oddsMap?.forEach { (key, value) ->
            value?.forEach { odd ->
                if (odd != null) {
                    if (key == PlayCate.EPS.value)
                        odd.setupEPSDiscount(discount)
                    else
                        setupOddDiscount(odd, discount)
                }
            }
        }
    }

    private fun setupOddDiscount(odd: Odd, discount: Float) {
        odd.odds = odd.odds?.applyDiscount(discount)
        odd.hkOdds = odd.hkOdds?.applyHKDiscount(discount)
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
    private fun List<LeagueOdd>.sortOdds() {
        this.forEach { leagueOdd ->
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

    /**
     * 檢查當前登入狀態, 若未登入則跳請登入提示
     * @return true: 已登入, false: 未登入
     */
    open fun checkLoginStatus(): Boolean {
        return if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            false
        } else {
            true
        }
    }

    /**
     * 更新翻譯
     */
    private fun List<LeagueOdd>.getPlayCateNameMap() {
        this.onEach { LeagueOdd ->
            LeagueOdd.matchOdds.onEach { matchOdd ->
                matchOdd.playCateNameMap =
                    PlayCateMenuFilterUtils.filterList?.get(matchOdd.matchInfo?.gameType)
                        ?.get(MenuCode.MAIN.code)?.playCateNameMap
            }
        }
    }

}