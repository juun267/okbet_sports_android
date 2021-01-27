package org.cxct.sportlottery.ui.finance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.list.RechargeListRequest
import org.cxct.sportlottery.network.money.list.RechargeListResult
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult

    val userRechargeListResult: LiveData<RechargeListResult?>
        get() = _userRechargeResult

    val userRechargeFilterList: LiveData<List<Row>>
        get() = _userRechargeFilterList

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    val recordCalendarRange: LiveData<Pair<Calendar, Calendar>>
        get() = _recordCalendarRange

    val recordCalendarStartDate: LiveData<String>
        get() = _recordCalendarStartDate

    val recordCalendarEndDate: LiveData<String>
        get() = _recordCalendarEndDate

    val rechargeStateList: LiveData<List<String>>
        get() = _rechargeStateList

    val rechargeState: LiveData<String>
        get() = _rechargeState

    val rechargeChannelList: LiveData<List<String>>
        get() = _rechargeChannelList

    val rechargeChannel: LiveData<String>
        get() = _rechargeChannel

    val recordType: LiveData<String>
        get() = _recordType

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _userRechargeResult = MutableLiveData<RechargeListResult?>()
    private val _userRechargeFilterList = MutableLiveData<List<Row>>()

    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()

    private val _recordCalendarRange = MutableLiveData<Pair<Calendar, Calendar>>()
    private val _recordCalendarStartDate = MutableLiveData<String>()
    private val _recordCalendarEndDate = MutableLiveData<String>()

    private val _rechargeStateList = MutableLiveData<List<String>>()
    private val _rechargeState = MutableLiveData<String>()

    private val _rechargeChannelList = MutableLiveData<List<String>>()
    private val _rechargeChannel = MutableLiveData<String>()

    private val _recordType = MutableLiveData<String>()


    fun setRecordType(recordType: String) {
        _recordType.postValue(recordType)
    }

    fun setRecordTimeRange(start: Calendar, end: Calendar? = null) {
        _recordCalendarStartDate.postValue(TimeUtil.timeFormat(start.timeInMillis, "yyyy-MM-dd"))
        end?.let {
            _recordCalendarEndDate.postValue(TimeUtil.timeFormat(it.timeInMillis, "yyyy-MM-dd"))
        }
    }

    fun setRechargeState(position: Int) {
        val rechargeStateList =
            androidContext.resources.getStringArray(R.array.recharge_state_array)

        var list = _userRechargeResult.value?.rows

        when (rechargeStateList[position]) {
            androidContext.getString(R.string.recharge_state_processing) -> {
                list = list?.filter {
                    it.status == RechargeState.PROCESSING.code
                } ?: listOf()
            }
            androidContext.getString(R.string.recharge_state_success) -> {
                list = list?.filter {
                    it.status == RechargeState.SUCCESS.code
                } ?: listOf()
            }

            androidContext.getString(R.string.recharge_state_failed) -> {
                list = list?.filter {
                    it.status == RechargeState.FAILED.code
                } ?: listOf()
            }
        }
        _rechargeState.postValue(rechargeStateList[position])
        _userRechargeFilterList.postValue(list ?: listOf())
    }

    fun setRechargeChannel(position: Int) {
        val rechargeChannelList =
            androidContext.resources.getStringArray(R.array.recharge_channel_array)

        _rechargeChannel.postValue(rechargeChannelList[position])
    }

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoneyResult.postValue(userMoneyResult)
        }
    }

    fun getRecordList() {
        val recordStrList = androidContext.resources.getStringArray(R.array.finance_array)
        val recordImgList = androidContext.resources.obtainTypedArray(R.array.finance_img_array)

        val recordList = recordStrList.map {
            it to recordImgList.getResourceId(recordStrList.indexOf(it), -1)
        }
        recordImgList.recycle()

        _recordList.postValue(recordList)
    }

    fun getCalendarRange() {
        val calendarToday = TimeUtil.getTodayEndTimeCalendar()
        val calendarPastMonth = TimeUtil.getTodayEndTimeCalendar()
        calendarPastMonth.add(Calendar.DATE, -30)

        _recordCalendarRange.postValue(calendarPastMonth to calendarToday)
    }

    fun getRechargeState() {
        val rechargeStateList =
            androidContext.resources.getStringArray(R.array.recharge_state_array)

        _rechargeStateList.postValue(rechargeStateList.asList())
    }

    fun getRechargeChannel() {
        val rechargeChannelList =
            androidContext.resources.getStringArray(R.array.recharge_channel_array)

        _rechargeChannelList.postValue(rechargeChannelList.asList())
    }

    fun getUserRechargeList() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.moneyService.getUserRechargeList(
                    RechargeListRequest()
                )
            }

            result?.rows?.map {
                it.rechState = when (it.status) {
                    RechargeState.SUCCESS.code -> androidContext.getString(R.string.recharge_state_success)
                    RechargeState.FAILED.code -> androidContext.getString(R.string.recharge_state_failed)
                    RechargeState.PROCESSING.code -> androidContext.getString(R.string.recharge_state_processing)
                    RechargeState.RECHARGING.code -> androidContext.getString(R.string.recharge_state_recharging)
                    else -> ""
                }

                it.rechDateStr = TimeUtil.timeFormat(it.rechTime, "yyyy-MM-dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.rechTime, "HH:mm:ss")
            }

            _userRechargeResult.postValue(result)
        }
    }
}