package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.*

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