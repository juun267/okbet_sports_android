package org.cxct.sportlottery.ui.profileCenter.sportRecord

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
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.util.TimeUtil

data class BetListRequestState(var hasStatus: Boolean, var hasStartDate: Boolean, var hasEndDate: Boolean)


class BetRecordViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    private val statusNameMap = mapOf(0 to "未确认", 1 to "未结算", 2 to "全赢", 3 to "赢半", 4 to "全输", 5 to "输半", 6 to "和", 7 to "已取消")

    companion object {
        private const val PAGE_SIZE = 20
    }

    val loading: LiveData<Boolean>
        get() = _loading

    val statusSearchEnable: LiveData<Boolean>
        get() = _statusSearchEnable

    val selectedBetStatus: LiveData<String?>
        get() = _selectedBetStatus

    val betListRequestState: LiveData<BetListRequestState>
        get() = _betListRequestState

    val betRecordResult: LiveData<BetListResult>
        get() = _betRecordResult

    private val _loading = MutableLiveData<Boolean>()
    private val _statusSearchEnable = MutableLiveData<Boolean>()
    private val _betListRequestState = MutableLiveData<BetListRequestState>()
    private val _betRecordResult = MutableLiveData<BetListResult>()
    private val _selectedBetStatus = MutableLiveData<String?>()

    private var mBetListRequest: BetListRequest? = null
    private val selectedStatusList : List<SheetData>
            get() = betStatusList.filter { it.isChecked }

    val betStatusList by lazy {
        statusNameMap.map {
            SheetData(it.key, it.value).apply {
                this.isChecked = true
            }
        }.toMutableList()
    }

    fun searchBetRecord(isChampionChecked: Boolean?= false, startTime: String ?= TimeUtil.getDefaultTimeStamp().startTime, endTime: String ?= TimeUtil.getDefaultTimeStamp().endTime) {
        if (betStatusList.none { it.isChecked }) {
            _statusSearchEnable.value = false
        } else {
            _statusSearchEnable.value = true
            val statusList = selectedStatusList.map { it.code }
            val championOnly = if (isChampionChecked == true) 1 else 0
            mBetListRequest = BetListRequest(championOnly = championOnly, statusList = statusList, startTime = startTime, endTime = endTime, page = 1, pageSize = PAGE_SIZE)
            mBetListRequest?.let { getBetList(it) }
        }
    }

    fun getBetStatus(): String {

        return when (selectedStatusList.size) {
            statusNameMap.values.size -> {
                androidContext.getString(R.string.all_bet_status)
            }
            0 -> {
                androidContext.getString(R.string.please_choose_bet_state)
            }
            else -> {
                selectedStatusList.joinToString(",") { it.showName.toString() }
            }
        }
    }

    fun isAllCbChecked() : Boolean {
        return (selectedStatusList.size == statusNameMap.size)
    }


    fun clearStatusList() {
        betStatusList.forEach {
            it.isChecked = false
        }
    }

    fun addAllStatusList() {
        betStatusList.forEach {
            it.isChecked = true
        }
    }

    var isLastPage = false
    private var nowPage = 1
    val recordDataList = mutableListOf<Row>()

    fun getNextPage(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (_loading.value != true && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loading()
                mBetListRequest?.let {
                    mBetListRequest = BetListRequest(championOnly = it.championOnly, statusList = it.statusList, startTime = it.startTime, endTime = it.endTime, page = it.page?.plus(1), pageSize = PAGE_SIZE)
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
