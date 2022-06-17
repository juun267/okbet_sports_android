package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.util.Event

abstract class BaseSocketViewModel(
    androidContext: Application,
    protected val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseFavoriteViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg

    val isScrollDown: LiveData<Event<Boolean>>
        get() = _isScrollDown

    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()
    private val _isScrollDown = MutableLiveData<Event<Boolean>>()

    init {
        /* gotConfigData 判斷：避免進 WebViewActivity crash */
        if (!loginRepository.isCheckToken && gotConfigData) {
            viewModelScope.launch {
                loginRepository.checkToken()

                if (!userInfoRepository.checkedUserInfo && isLogin.value == true) {
                    userInfoRepository.getUserInfo()
                }
            }
        }
    }

    fun updatePlayQuota(playQuotaComData: PlayQuotaComData) {
        betInfoRepository.playQuotaComData = playQuotaComData
    }

    fun updateMoney(money: Double?) {
        mUserMoney.postValue(money)
    }

    fun updateLockMoney(money: Double?) {
        mLockMoney.postValue(money)
    }

    fun getSettlementNotification(event: OrderSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.UN_CHECK.code, Status.UN_DONE.code, Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code,  Status.LOSE.code,  Status.LOSE_HALF.code,  Status.DRAW.code -> {
                    _settlementNotificationMsg.value = Event(it)
                }
            }
        }
    }

    fun updateDiscount(discount: Double?) {
        viewModelScope.launch {
            if (discount == null) {
                viewModelScope.launch {
                    doNetwork(androidContext) { userInfoRepository.getUserInfo() }
                }
            } else {
                userInfo.value?.userId?.let { userInfoRepository.updateDiscount(it, discount.toFloat()) }
            }
        }
    }

    fun setIsScrollDown(isScrollDown: Boolean) {
        _isScrollDown.postValue(Event(isScrollDown))
    }
}