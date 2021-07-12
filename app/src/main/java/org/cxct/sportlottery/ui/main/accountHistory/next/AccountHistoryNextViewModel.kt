package org.cxct.sportlottery.ui.main.accountHistory.next

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.TimeUtil
import java.util.*


class AccountHistoryNextViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseOddButtonViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    private val _loading = MutableLiveData<Boolean>()
    private val _betRecordResult = MutableLiveData<BetListResult>()

    private var mBetListRequest: BetListRequest? = null

    fun searchBetRecord(
        isChampionChecked: Boolean? = false,
        gameType: String? = null,
        minusDate: String? = null,
        status: String? = null,
    ) {

        val startTime = TimeUtil.getMinusDateTimeStamp(minusDate?.toIntOrNull()).startTime
        val endTime = TimeUtil.getMinusDateTimeStamp(minusDate?.toIntOrNull()).endTime

        val statusFilter = { item: String? ->
            if (item.isNullOrEmpty()) listOf(1, 2, 3, 4, 5, 6, 7) else item.toList().map {
                Character.getNumericValue(it)
            }
        }

        val emptyFilter = { item: String? ->
            if (item.isNullOrEmpty()) null else item
        }

        val championOnly = if (isChampionChecked == true) 1 else 0

        mBetListRequest = BetListRequest(
            championOnly = championOnly,
            gameType = emptyFilter(gameType),
            statusList = statusFilter(status),
            startTime = startTime,
            endTime = endTime,
            page = 1,
            pageSize = PAGE_SIZE
        )
        mBetListRequest?.let { getBetList(it) }
    }

    var isLastPage = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetListRequest?.let {
                    mBetListRequest = BetListRequest(
                        championOnly = it.championOnly,
                        statusList = it.statusList,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        page = it.page?.plus(1),
                        pageSize = PAGE_SIZE
                    )
                    getBetList(mBetListRequest!!)
                }
            }
        }

    }

    private fun getBetList(betListRequest: BetListRequest) {

        if (betListRequest.page == 1) {
            nowPage = 1
            recordDataList.clear()
        }

        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }?.let { result ->
                hideLoading()
                result.rows?.let { recordDataList.addAll(it) }
                isLastPage = (recordDataList.size >= (result.total ?: 0))
                _betRecordResult.value = result
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
