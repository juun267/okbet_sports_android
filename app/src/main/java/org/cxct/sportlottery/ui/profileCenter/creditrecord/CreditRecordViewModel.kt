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

    private val _loading = MutableLiveData<Boolean>()
    private val _userCreditCircleHistory = MutableLiveData<List<Row>>()
    private val _remainDay = MutableLiveData<String>()

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
            val response = OneBoSportApi.userService.getUserCreditCircleHistory(
                CreditCircleHistoryRequest(pageIndex, pageSize)
            )

            response.body()?.rows?.let {
                pageSizeLoad += it.size

                it.postRemainDay()
                it.mapRecordPeriod()
                it.mapAllBalance()

                _userCreditCircleHistory.postValue(it)
            }

            pageSizeTotal = response.body()?.total ?: 0

            _loading.postValue(false)
        }
    }

    private fun List<Row>.postRemainDay() {
        this.firstOrNull()?.endTime?.let { endTime ->
            _remainDay.postValue(TimeUtil.timeFormat(endTime - System.currentTimeMillis(), "dd"))
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