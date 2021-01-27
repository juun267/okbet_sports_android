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
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.finance.data.RechargeChannel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.ui.finance.data.RechargeState
import org.cxct.sportlottery.ui.finance.data.RechargeTime
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult

    val userRechargeListResult: LiveData<RechargeListResult?>
        get() = _userRechargeResult

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    val recordCalendarRange: LiveData<Pair<Calendar, Calendar>>
        get() = _recordCalendarRange

    val recordCalendarStartDate: LiveData<RechargeTime>
        get() = _recordCalendarStartDate

    val recordCalendarEndDate: LiveData<RechargeTime>
        get() = _recordCalendarEndDate

    val rechargeStateList: LiveData<List<RechargeState>>
        get() = _rechargeStateList

    val rechargeChannelList: LiveData<List<RechargeChannel>>
        get() = _rechargeChannelList

    val recordType: LiveData<String>
        get() = _recordType

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _userRechargeResult = MutableLiveData<RechargeListResult?>()

    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()
    private val _recordType = MutableLiveData<String>()

    private val _recordCalendarRange = MutableLiveData<Pair<Calendar, Calendar>>()
    private val _recordCalendarStartDate = MutableLiveData<RechargeTime>()
    private val _recordCalendarEndDate = MutableLiveData<RechargeTime>()

    private val _rechargeStateList = MutableLiveData<List<RechargeState>>()
    private val _rechargeChannelList = MutableLiveData<List<RechargeChannel>>()


    fun setRecordType(recordType: String) {
        _recordType.postValue(recordType)
    }

    fun setRecordTimeRange(start: Calendar, end: Calendar? = null) {
        val startDate =
            RechargeTime(TimeUtil.timeFormat(start.timeInMillis, "yyyy-MM-dd"), start.timeInMillis)
        _recordCalendarStartDate.postValue(startDate)

        end?.let {
            val endDate =
                RechargeTime(TimeUtil.timeFormat(it.timeInMillis, "yyyy-MM-dd"), end.timeInMillis)
            _recordCalendarEndDate.postValue(endDate)
        }
    }

    fun setRechargeState(position: Int) {
        val list = _rechargeStateList.value

        list?.forEach {
            it.isSelected = (list.indexOf(it) == position)
        }

        _rechargeStateList.postValue(list ?: listOf())
    }

    fun setRechargeChannel(position: Int) {
        val list = _rechargeChannelList.value

        list?.forEach {
            it.isSelected = (list.indexOf(it) == position)
        }

        _rechargeChannelList.postValue(list ?: listOf())
    }

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }

            userMoneyResult?.displayMoney = ArithUtil.toMoneyFormat(userMoneyResult?.money)

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

        val list = rechargeStateList.map {
            when (it) {
                androidContext.getString(R.string.recharge_state_processing) -> {
                    RechargeState(Status.PROCESSING.code, it)
                }
                androidContext.getString(R.string.recharge_state_success) -> {
                    RechargeState(Status.SUCCESS.code, it)

                }
                androidContext.getString(R.string.recharge_state_failed) -> {
                    RechargeState(Status.FAILED.code, it)
                }
                else -> {
                    RechargeState(null, it).apply { isSelected = true }
                }
            }
        }

        _rechargeStateList.postValue(list)
    }

    fun getRechargeChannel() {
        val rechargeChannelList =
            androidContext.resources.getStringArray(R.array.recharge_channel_array)

        val list = rechargeChannelList.map {
            when (it) {
                androidContext.getString(R.string.recharge_channel_online) -> {
                    RechargeChannel(RechType.ONLINE_PAYMENT.type, it)
                }
                androidContext.getString(R.string.recharge_channel_bank) -> {
                    RechargeChannel(RechType.BANK_TRANSFER.type, it)
                }
                androidContext.getString(R.string.recharge_channel_alipay) -> {
                    RechargeChannel(RechType.ALIPAY.type, it)
                }
                androidContext.getString(R.string.recharge_channel_weixin) -> {
                    RechargeChannel(RechType.WEIXIN.type, it)
                }
                androidContext.getString(R.string.recharge_channel_cft) -> {
                    RechargeChannel(RechType.CFT.type, it)
                }
                androidContext.getString(R.string.recharge_channel_admin) -> {
                    RechargeChannel(RechType.ADMIN_ADD_MONEY.type, it)
                }
                else -> {
                    RechargeChannel(null, it).apply { isSelected = true }
                }
            }
        }

        _rechargeChannelList.postValue(list)
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
                    Status.SUCCESS.code -> androidContext.getString(R.string.recharge_state_success)
                    Status.FAILED.code -> androidContext.getString(R.string.recharge_state_failed)
                    Status.PROCESSING.code -> androidContext.getString(R.string.recharge_state_processing)
                    Status.RECHARGING.code -> androidContext.getString(R.string.recharge_state_recharging)
                    else -> ""
                }

                it.rechDateStr = TimeUtil.timeFormat(it.rechTime, "yyyy-MM-dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.rechTime, "HH:mm:ss")

                it.displayMoney = ArithUtil.toMoneyFormat(it.rechMoney)
            }

            _userRechargeResult.postValue(result)
        }
    }
}