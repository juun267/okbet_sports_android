package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.SingleLiveEvent


abstract class BaseFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val myFavoriteRepository: MyFavoriteRepository
) : BaseNoticeViewModel(
    androidContext, userInfoRepository, loginRepository, betInfoRepository, infoCenterRepository
) {
    //TODO add notify login ui to activity/fragment
    val notifyLogin: SingleLiveEvent<Boolean>
        get() = mNotifyLogin
    private val mNotifyLogin = SingleLiveEvent<Boolean>()

    val notifyMyFavorite = myFavoriteRepository.favorNotify
    val detailNotifyMyFavorite = myFavoriteRepository.detailFavorNotify


    val favorSportList = myFavoriteRepository.favorSportList

    val favorLeagueList = myFavoriteRepository.favorLeagueList

    val favorMatchList: LiveData<Set<String>> = myFavoriteRepository.favorMatchList

    val favorPlayCateList = myFavoriteRepository.favorPlayCateList

    val favoriteOutrightList = myFavoriteRepository.favoriteOutrightList

    val _sportCodeSpinnerList = MutableLiveData<List<StatusSheetData>>() //當前啟用球種篩選清單
    val sportCodeList: LiveData<List<StatusSheetData>>
        get() = _sportCodeSpinnerList

    fun getFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }

        doRequest({ myFavoriteRepository.getFavorite() }) { }
    }


    protected fun MatchOdd.setupOddDiscountFixed() {

        val discount = userInfo.value?.discount ?: 1.0F

        this.oddsMap?.forEach { (key, value) ->
            value?.forEach { odd ->
                if (odd != null) {
                    if (key == PlayCate.EPS.value) odd.setupEPSDiscount(discount)
                    else setupOddDiscount(odd, discount)
                }
            }
        }
    }

    private fun setupOddDiscount(odd: Odd, discount: Float) {
        odd.odds = odd.odds?.applyDiscount(discount)
        odd.hkOdds = odd.hkOdds?.applyHKDiscount(discount)
    }

    fun clearFavorite() {
        myFavoriteRepository.clearFavorite()
    }

    fun notifyFavorite(type: FavoriteType) {
        myFavoriteRepository.notifyFavorite(type)
    }


    fun pinFavorite(
        type: FavoriteType, content: String?, gameType: String? = null
    ) {
        if (!LoginRepository.isLogined()) {
            mNotifyLogin.postValue(true)
            return
        }

        doRequest({ myFavoriteRepository.pinFavorite(type, content, gameType) }) {

        }

    }

//    fun leftPinFavorite(gameType: String?, addOrRemove: Int?) {
//        if (isLogin.value != true) {
//            _leftNotifyLogin.postValue(Event(true))
//            return
//        }
//
//        pinFavorite(FavoriteType.SPORT, gameType)
//        _leftNotifyFavorite.postValue(Event(addOrRemove))
//    }



    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    protected fun List<LeagueOdd>.sortOdds() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                val sortOrder = matchOdd.oddsSort?.split(",")
                val oddsMap = matchOdd.oddsMap?.toSortedMap(compareBy<String> {
                    sortOrder?.indexOf(it)
                }.thenBy { it })

                matchOdd.oddsMap?.clear()
                if (oddsMap != null) {
                    matchOdd.oddsMap?.putAll(oddsMap)
                }
            }
        }
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
    protected fun List<LeagueOdd>.getPlayCateNameMap() {
        this.onEach { LeagueOdd ->
            LeagueOdd.matchOdds.onEach { matchOdd ->
                matchOdd.playCateNameMap =
                    PlayCateMenuFilterUtils.filterList?.get(matchOdd.matchInfo?.gameType)
                        ?.get(MenuCode.MAIN.code)?.playCateNameMap
            }
        }
    }

}