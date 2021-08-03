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
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.Event

abstract class BaseSocketViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg

    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()

    init {
        if (!loginRepository.isCheckToken) {
            viewModelScope.launch {
                loginRepository.checkToken()
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
}