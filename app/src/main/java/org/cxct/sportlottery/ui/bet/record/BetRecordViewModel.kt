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
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.bet.record.search.BetTypeItemData
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.dateToTimeStamp

data class BetListRequestState(var hasStatus: Boolean, var hasStartDate: Boolean, var hasEndDate: Boolean)

val statusNameMap = mapOf(0 to "未确认", 1 to "未结算", 2 to "全赢", 3 to "赢半", 4 to "全输", 5 to "输半", 6 to "和", 7 to "已取消")

class BetRecordViewModel(private val androidContext: Context) : BaseViewModel() {

    val selectStatusNameList: LiveData<MutableList<BetTypeItemData>>
        get() = _selectStatusList

    val selectedBetStatus: LiveData<String?>
        get() = _selectedBetStatus

    val betListRequestState: LiveData<BetListRequestState>
        get() = _betListRequestState

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    private val _selectStatusList = MutableLiveData<MutableList<BetTypeItemData>>().apply {
        this.value = mutableListOf()
    }

    private val _betListRequestState = MutableLiveData<BetListRequestState>()
    private val _betRecordResult = MutableLiveData<BetListResult>()
    private val _selectedBetStatus = MutableLiveData<String?>()

    fun checkRequestState(startDate: String, endDate: String) {
        _betListRequestState.value = BetListRequestState(
            hasStatus = selectStatusNameList.value?.size ?: 0 > 0,
            hasStartDate = startDate.isNotEmpty(),
            hasEndDate = endDate.isNotEmpty()
        )
    }

    private fun getBetStatus (): String? {
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

    fun getBetList(isChampionChecked: Boolean, statusList: List<Int>, startDate: String, endDate: String) {
        val championOnly = if (isChampionChecked) 1 else 0

        viewModelScope.launch {
            val betListRequest = BetListRequest(championOnly = championOnly,
                                                statusList = statusList,
                                                startTime = dateToTimeStamp(startDate, TimeUtil.TimeType.START_OF_DAY).toString(),
                                                endTime = dateToTimeStamp(endDate, TimeUtil.TimeType.END_OF_DAY).toString())

            doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            }?.let { result ->
                _betRecordResult.postValue(result)
            }
        }
    }

}
