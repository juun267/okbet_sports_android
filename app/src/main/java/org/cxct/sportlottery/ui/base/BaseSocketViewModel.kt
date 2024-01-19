package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.*

abstract class BaseSocketViewModel(
    androidContext: Application,
    protected val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    val favoriteRepository: MyFavoriteRepository
) : BaseUserViewModel(androidContext, loginRepository,userInfoRepository, betInfoRepository, infoCenterRepository) {
    val notifyMyFavorite = favoriteRepository.favorNotify
    val detailNotifyMyFavorite = favoriteRepository.detailFavorNotify

    val favorSportList = favoriteRepository.favorSportList

    val favorLeagueList = favoriteRepository.favorLeagueList

    val favorMatchList: LiveData<Set<String>> = favoriteRepository.favorMatchList

    val favorPlayCateList = favoriteRepository.favorPlayCateList

    val favoriteOutrightList = favoriteRepository.favoriteOutrightList

    val settlementNotificationMsg
        get() = betInfoRepository.settlementNotificationMsg

    fun updateMoney(money: Double?) {
        LoginRepository.updateMoney(money)
    }

    fun updateLockMoney(money: Double?) {
        mLockMoney.postValue(money)
    }

    fun getSettlementNotification(event: FrontWsEvent.BetSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.UN_CHECK.code, Status.UN_DONE.code, Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code,  Status.LOSE.code,  Status.LOSE_HALF.code,  Status.DRAW.code -> {
                    betInfoRepository.postSettlementNotificationMsg(it)
                }
            }
        }
    }

    fun updateDiscount(discountByGameTypeList: List<FrontWsEvent.DiscountByGameTypeVO>?) {
        viewModelScope.launch {
            if (discountByGameTypeList.isNullOrEmpty()) {
                viewModelScope.launch {
                    doNetwork { userInfoRepository.getUserInfo() }
                }
            } else {
                userInfo.value?.userId?.let { userInfoRepository.updateDiscount(discountByGameTypeList) }
            }
        }
    }
    fun getFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }
        doRequest({ favoriteRepository.getFavorite() }) { }
    }


    fun clearFavorite() {
        favoriteRepository.clearFavorite()
    }

    fun notifyFavorite(type: FavoriteType) {
        favoriteRepository.notifyFavorite(type)
    }

    fun pinFavorite(
        type: FavoriteType, content: String?, gameType: String? = null
    ) {
        if (!LoginRepository.isLogined()) {
            mNotifyLogin.postValue(true)
            return
        }

        doRequest({ favoriteRepository.pinFavorite(type, content, gameType) }) {

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
}