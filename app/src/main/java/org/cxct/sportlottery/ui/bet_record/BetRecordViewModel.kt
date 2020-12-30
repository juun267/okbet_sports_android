package org.cxct.sportlottery.ui.bet_record

import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.bet_record.search.BetTypeItemData
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.dateToTimeStamp

data class BetListRequestState(var hasStatus: Boolean, var hasStartDate: Boolean, var hasEndDate: Boolean)

class BetRecordViewModel : BaseViewModel() {

    val selectStatusNameList: LiveData<MutableList<BetTypeItemData>>
    get() = _selectStatusList

    val betListRequestState: LiveData<BetListRequestState>
        get() = _betListRequestState

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    private val _selectStatusList = MutableLiveData<MutableList<BetTypeItemData>>()
    init {
        _selectStatusList.value = mutableListOf()
    }

    private val _betListRequestState = MutableLiveData<BetListRequestState>()
    private val _betRecordResult = MutableLiveData<BetListResult>()


    val statusNameMap = mapOf(0 to "未确认", 1 to "未结算", 2 to "全赢", 3 to "赢半", 4 to "全输", 5 to "输半", 6 to "和", 7 to "已取消")

    fun checkRequestState(startDate: String, endDate: String) {
        _betListRequestState.value = BetListRequestState(
            hasStatus = selectStatusNameList.value?.size?:0 > 0,
            hasStartDate = startDate.isNotEmpty(),
            hasEndDate = endDate.isNotEmpty()
        )
    }

    fun addSelectStatus(item: BetTypeItemData) {
        _selectStatusList.value?.add(item)
        _selectStatusList.value = _selectStatusList.value
    }

    fun clearStatusList() {
        _selectStatusList.value?.clear()
        _selectStatusList.value = _selectStatusList.value
    }

    fun deleteSelectStatus(item: BetTypeItemData) {
        _selectStatusList.value?.remove(item)
        _selectStatusList.value = _selectStatusList.value
    }

    private suspend fun getBetList(betListRequest: BetListRequest) {
        val betListResponse = OneBoSportApi.betService.getBetList(betListRequest)

        if (betListResponse.isSuccessful) {
          _betRecordResult.postValue(betListResponse.body())
        } else {
            val apiError = ErrorUtils.parseError(betListResponse)
            apiError?.let {
                    _betRecordResult.postValue(BetListResult(success = it.success, msg = it.msg, code = it.code, rows = listOf(), total = 0))
            }
        }
    }

    fun searchBetRecordHistory(statusList: List<Int>, startDate: String, endDate: String) {
        viewModelScope.launch {
            val betListRequest = BetListRequest(statusList = statusList,
                                                startTime = dateToTimeStamp(startDate, TimeUtil.TimeType.START).toString(),
                                                endTime = dateToTimeStamp(endDate, TimeUtil.TimeType.END).toString())
            getBetList(betListRequest)
        }
    }
}

class ListLiveData<T> : LiveData<MutableList<T>?>() {
    fun addAll(items: List<T>?) {
        if (value != null && items != null) {
            value!!.addAll(items)
            value = value
        }
    }

    fun add(items: T?) {
        if (value != null && items != null) {
            value!!.add(items)
            value = value
        }
    }

    fun clear() {
        if (value != null) {
            value!!.clear()
            value = value
        }
    }

    public override fun setValue(value: MutableList<T>?) {
        super.setValue(value)
    }

    @Nullable
    override fun getValue(): MutableList<T>? {
        return super.getValue()
    }
}