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

    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()

    init {
        if (!loginRepository.isCheckToken) {
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

    fun getSettlementNotification(event: OrderSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code -> {
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
}