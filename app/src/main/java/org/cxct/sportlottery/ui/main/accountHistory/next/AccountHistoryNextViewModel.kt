package org.cxct.sportlottery.ui.main.accountHistory.next

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListRequest
import org.cxct.sportlottery.network.bet.settledDetailList.BetSettledDetailListResult
import org.cxct.sportlottery.network.bet.settledDetailList.Row
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.TimeUtil


class AccountHistoryNextViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseOddButtonViewModel(androidContext, loginRepository, betInfoRepository, infoCenterRepository) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val betDetailResult: LiveData<BetSettledDetailListResult>
        get() = _betDetailResult

    private val _loading = MutableLiveData<Boolean>()
    private val _betDetailResult = MutableLiveData<BetSettledDetailListResult>()

    private var mBetDetailRequest: BetSettledDetailListRequest? = null

    val emptyFilter = { item: String? ->
        if (item.isNullOrEmpty()) null else item
    }

    var isLastPage = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun searchBetRecord(gameType: String? = null, minusDate: String? = null) {

        val startTime = TimeUtil.getMinusDateTimeStamp(minusDate?.toIntOrNull()).startTime
        val endTime = TimeUtil.getMinusDateTimeStamp(minusDate?.toIntOrNull()).endTime

        mBetDetailRequest = BetSettledDetailListRequest(
            gameType = emptyFilter(gameType),
            startTime = startTime,
            endTime = endTime,
            page = 1,
            pageSize = PAGE_SIZE)
        mBetDetailRequest?.let { getBetList(it) }
    }

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetDetailRequest?.let {
                    mBetDetailRequest = BetSettledDetailListRequest(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        page = it.page?.plus(1),
                        pageSize = PAGE_SIZE)
                    getBetList(mBetDetailRequest!!)
                }
            }
        }

    }

    private fun getBetList(betDetailRequest: BetSettledDetailListRequest) {

        if (betDetailRequest.page == 1) {
            nowPage = 1
            recordDataList.clear()
        }

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetSettledDetailList(betDetailRequest)
            }?.let { result ->
                hideLoading()
                result.rows?.let { recordDataList.addAll(it) }
                isLastPage = (recordDataList.size >= (result.total ?: 0))
                _betDetailResult.value = result
            }
        }

    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }
}
