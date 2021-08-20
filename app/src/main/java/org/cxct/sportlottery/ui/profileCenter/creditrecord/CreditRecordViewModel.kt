package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.credit.CreditCircleHistoryRequest
import org.cxct.sportlottery.network.user.credit.Row
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

const val DEFAULT_PAGE_SIZE = 10

class CreditRecordViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val userCreditCircleHistory: LiveData<List<Row>>
        get() = _userCreditCircleHistory

    private val _userCreditCircleHistory = MutableLiveData<List<Row>>()

    fun getCreditRecord(pageIndex: Int = 1) {
        viewModelScope.launch {
            val response = OneBoSportApi.userService.getUserCreditCircleHistory(
                CreditCircleHistoryRequest(pageIndex, DEFAULT_PAGE_SIZE)
            )

            response.body()?.rows?.let {
                it.mapRecordPeriod()
                it.mapAllBalance()

                _userCreditCircleHistory.postValue(it)
            }
        }
    }

    private fun List<Row>.mapRecordPeriod() {
        this.forEach {
            it.period =
                TimeUtil.timeFormat(it.beginTime, "yyyy/MM/dd") +
                        " ~ " +
                        TimeUtil.timeFormat(it.endTime, "yyyy/MM/dd")
        }
    }

    private fun List<Row>.mapAllBalance() {
        this.forEach {
            it.creditBalance?.let { creditBalance ->
                it.formatCreditBalance = TextUtil.formatMoney(creditBalance)
            }
            it.balance?.let { balance ->
                it.formatBalance = TextUtil.formatMoney(balance)
            }
            it.reward?.let { reward ->
                it.formatReward = TextUtil.formatMoney(reward)
            }
        }
    }
}