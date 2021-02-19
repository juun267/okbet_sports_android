package org.cxct.sportlottery.ui.bet.record

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.bet.record.search.BetTypeItemData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.dateToTimeStamp

data class BetListRequestState(var hasStatus: Boolean, var hasStartDate: Boolean, var hasEndDate: Boolean)

val statusNameMap = mapOf(0 to "未确认", 1 to "未结算", 2 to "全赢", 3 to "赢半", 4 to "全输", 5 to "输半", 6 to "和", 7 to "已取消")

class BetRecordViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    companion object {
        private const val PAGE_SIZE = 10
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val waitingResult: LiveData<Boolean>
        get() = _waitingResult

    val selectStatusNameList: LiveData<MutableList<BetTypeItemData>>
        get() = _selectStatusList

    val selectedBetStatus: LiveData<String?>
        get() = _selectedBetStatus

    val betListRequestState: LiveData<BetListRequestState>
        get() = _betListRequestState

    val betRecordResult: LiveData<Event<BetListResult>>
        get() = _betRecordResult

    private val _selectStatusList = MutableLiveData<MutableList<BetTypeItemData>>().apply {
        this.value = mutableListOf()
    }

    private val _loading = MutableLiveData<Boolean>()
    private val _waitingResult = MutableLiveData<Boolean>()
    private val _betListRequestState = MutableLiveData<BetListRequestState>()
    private val _betRecordResult = MutableLiveData<Event<BetListResult>>()
    private val _selectedBetStatus = MutableLiveData<String?>()

    private var mBetListRequest: BetListRequest? = null

    fun checkRequestState(startDate: String, endDate: String) {
        _betListRequestState.value = BetListRequestState(
            hasStatus = selectStatusNameList.value?.size ?: 0 > 0,
            hasStartDate = startDate.isNotEmpty(),
            hasEndDate = endDate.isNotEmpty()
        )
    }

    fun confirmSearch(betStatusList: List<BetTypeItemData>, isOutRight: Boolean, startDate: String, endDate: String) {
        val selectBetStatus = filterStatusList(betStatusList)
        if (checkInputFilter(selectBetStatus, startDate, endDate)) {
            _waitingResult.postValue(true)
            getBetRecord(isOutRight, selectBetStatus, startDate, endDate)
        }
    }

    private fun filterStatusList(betStatusList: List<BetTypeItemData>): List<Int> {
        return betStatusList.filter { it.isSelected }.map { it.code }
    }

    private fun checkInputFilter(betStatusList: List<Int>, startDate: String, endDate: String): Boolean {
        val inputBetStatus = betStatusList.isNotEmpty()
        val inputStartDate = startDate.isNotEmpty()
        val inputEndDate = endDate.isNotEmpty()
        _betListRequestState.value = BetListRequestState(
            hasStatus = inputBetStatus,
            hasStartDate = inputStartDate,
            hasEndDate = inputEndDate
        )
        return inputBetStatus && inputStartDate && inputEndDate
    }

    private fun getBetStatus(): String? {
        return if (selectStatusNameList.value?.size == statusNameMap.values.size) {
            androidContext.getString(R.string.all_order)
        } else {
            selectStatusNameList.value?.joinToString(",") { it.name }
        }
    }

    fun addSelectStatus(item: BetTypeItemData) {
        _selectStatusList.value?.add(item)
        _selectedBetStatus.value = getBetStatus()
        _selectStatusList.value = _selectStatusList.value
    }

    fun clearStatusList() {
        _selectStatusList.value?.clear()
        _selectedBetStatus.value = getBetStatus()
        _selectStatusList.value = _selectStatusList.value
    }

    fun deleteSelectStatus(item: BetTypeItemData) {
        _selectStatusList.value?.remove(item)
        _selectedBetStatus.value = getBetStatus()
        _selectStatusList.value = _selectStatusList.value
    }

    private fun getBetRecord(isChampionChecked: Boolean, statusList: List<Int>, startDate: String, endDate: String) {
        val championOnly = if (isChampionChecked) 1 else 0
        mBetListRequest = BetListRequest(
            championOnly = championOnly,
            statusList = statusList,
            startTime = dateToTimeStamp(startDate, TimeUtil.TimeType.START_OF_DAY).toString(),
            endTime = dateToTimeStamp(endDate, TimeUtil.TimeType.END_OF_DAY).toString(),
            page = 1,
            pageSize = 10
        )
        mBetListRequest?.let { getBetList(it) }
    }

    var isLastPage = false
    private var isLoading = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                isLoading = true
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
                Event(result).let { eventResult ->
                    hideLoading()
                    isLoading = false
                    _betRecordResult.postValue(eventResult)
                    _waitingResult.postValue(false)
                    recordDataList.addAll(result.rows as List<Row>)
                }
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
