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
import org.cxct.sportlottery.ui.common.Paging
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*
import java.util.concurrent.TimeUnit

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
), Paging {

    val loading: LiveData<Boolean>
        get() = _loading

    val remainDay: LiveData<String>
        get() = _remainDay

    val userCreditCircleHistory: LiveData<List<Row>>
        get() = _userCreditCircleHistory

    val quotaAmount: LiveData<String>
        get() = _quotaAmount

    private val _loading = MutableLiveData<Boolean>()
    private val _userCreditCircleHistory = MutableLiveData<List<Row>>()
    private val _remainDay = MutableLiveData<String>()
    private val _quotaAmount = MutableLiveData<String>()

    override var pageSize: Int = DEFAULT_PAGE_SIZE
    override var pageSizeLoad: Int = 0
    override var pageSizeTotal: Int = 0


    fun getCreditRecordNext(
        visibleItemCount: Int,
        firstVisibleItemPosition: Int,
        totalItemCount: Int
    ) {
        if (_loading.value != true && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= pageSize) {
                getCreditRecord(pageIndex = getPageIndex() + 1)
            }
        }
    }

    fun getCreditRecord(pageIndex: Int = 1) {
        _loading.postValue(true)

        if (pageIndex == 1) {
            initPage()
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.getUserCreditCircleHistory(
                    CreditCircleHistoryRequest(pageIndex, pageSize)
                )
            }

            result?.rows?.let {
                pageSizeLoad += it.size

                it.mapRecordPeriod()
                it.mapAllBalance()

                if (_userCreditCircleHistory.value.isNullOrEmpty()) {
                    it.postRemainDay()
                    _userCreditCircleHistory.postValue(it)
                } else {
                    val currentList =
                        _userCreditCircleHistory.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(it)

                    _userCreditCircleHistory.postValue(currentList)
                }
            }

            result?.other?.postQuotaAmount()

            pageSizeTotal = result?.total ?: 0

            _loading.postValue(false)
        }
    }

    private fun List<Row>.postRemainDay() {
        this.firstOrNull()?.endTime?.let { endTime ->
            val endTimeMillis = Calendar.getInstance().apply {
                timeInMillis = endTime
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 59)
            }.timeInMillis

            val currentTimeMillis = Calendar.getInstance().timeInMillis

            _remainDay.postValue(
                TimeUnit.MILLISECONDS.toDays(endTimeMillis - currentTimeMillis).toString()
            )
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

    private fun Row.postQuotaAmount() {
        this.reward?.let { reward ->
            _quotaAmount.postValue(TextUtil.formatMoney(reward))
        }
    }
}