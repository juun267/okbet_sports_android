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
import org.cxct.sportlottery.network.withdraw.list.WithdrawListRequest
import org.cxct.sportlottery.network.withdraw.list.WithdrawListResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.finance.data.*
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*


const val pageSize = 20

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val userRechargeListResult: LiveData<RechargeListResult?>
        get() = _userRechargeResult

    val userWithdrawListResult: LiveData<WithdrawListResult?>
        get() = _userWithdrawResult

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

    val withdrawStateList: LiveData<List<WithdrawState>>
        get() = _withdrawStateList

    val withdrawTypeList: LiveData<List<WithdrawType>>
        get() = _withdrawTypeList

    val logDetail: LiveData<LogDetail>
        get() = _logDetail

    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _userMoney = BroadcastRepository().instance().userMoney
    private val _userRechargeResult = MutableLiveData<RechargeListResult?>()
    private val _userWithdrawResult = MutableLiveData<WithdrawListResult?>()

    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()
    private val _recordType = MutableLiveData<String>()

    private val _recordCalendarRange = MutableLiveData<Pair<Calendar, Calendar>>()
    private val _recordCalendarStartDate = MutableLiveData<RechargeTime>()
    private val _recordCalendarEndDate = MutableLiveData<RechargeTime>()

    private val _rechargeStateList = MutableLiveData<List<RechargeState>>()
    private val _rechargeChannelList = MutableLiveData<List<RechargeChannel>>()

    private val _withdrawStateList = MutableLiveData<List<WithdrawState>>()
    private val _withdrawTypeList = MutableLiveData<List<WithdrawType>>()

    private val _logDetail = MutableLiveData<LogDetail>()

    private val _isFinalPage = MutableLiveData<Boolean>().apply { value = false }
    private var page = 1


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

            _userMoney.postValue(userMoneyResult?.money)
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

    fun getUserRechargeList(isFirstFetch: Boolean) {
        when {
            isFirstFetch -> {
                _isFinalPage.postValue(false)
                page = 1
            }
            else -> {
                if (isFinalPage.value == false) {
                    page++
                }
            }
        }

        val rechType = _rechargeChannelList.value?.find {
            it.isSelected
        }?.type

        val status = _rechargeStateList.value?.find {
            it.isSelected
        }?.code

        val startTime = _recordCalendarStartDate.value?.date
        val endTime = _recordCalendarEndDate.value?.date

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.moneyService.getUserRechargeList(
                    RechargeListRequest(
                        rechType = rechType,
                        status = status,
                        startTime = startTime,
                        endTime = endTime,
                        page = page,
                        pageSize = pageSize
                    )
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

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            _userRechargeResult.postValue(result)
        }
    }

    fun setWithdrawState(position: Int) {
        val list = _withdrawStateList.value

        list?.forEach {
            it.isSelected = (list.indexOf(it) == position)
        }

        _withdrawStateList.postValue(list ?: listOf())
    }

    fun setWithdrawType(position: Int) {
        val list = _withdrawTypeList.value

        list?.forEach {
            it.isSelected = (list.indexOf(it) == position)
        }

        _withdrawTypeList.postValue(list ?: listOf())
    }

    fun getWithdrawState() {
        val withdrawStateList =
            androidContext.resources.getStringArray(R.array.withdraw_state_array)

        val list = withdrawStateList.map {
            when (it) {
                androidContext.getString(R.string.withdraw_log_state_processing) -> {
                    WithdrawState(CheckStatus.PROCESSING.code, it)
                }
                androidContext.getString(R.string.withdraw_log_state_pass) -> {
                    WithdrawState(CheckStatus.PASS.code, it)
                }
                androidContext.getString(R.string.withdraw_log_state_un_pass) -> {
                    WithdrawState(CheckStatus.UN_PASS.code, it)
                }
                else -> {
                    WithdrawState(null, it).apply { isSelected = true }
                }
            }
        }

        _withdrawStateList.postValue(list)
    }

    fun getWithdrawType() {
        val withdrawTypeList =
            androidContext.resources.getStringArray(R.array.withdraw_type_array)

        val list = withdrawTypeList.map {
            when (it) {
                androidContext.getString(R.string.withdraw_log_type_bank_trans) -> {
                    WithdrawType(UWType.BANK_TRANSFER.type, it)
                }
                androidContext.getString(R.string.withdraw_log_type_admin) -> {
                    WithdrawType(UWType.ADMIN_SUB_MONEY.type, it)
                }
                else -> {
                    WithdrawType(null, it).apply { isSelected = true }
                }
            }
        }

        _withdrawTypeList.postValue(list)
    }

    fun getUserWithdrawList(isFirstFetch: Boolean) {
        when {
            isFirstFetch -> {
                _isFinalPage.postValue(false)
                page = 1
            }
            else -> {
                if (isFinalPage.value == false) {
                    page++
                }
            }
        }

        val checkStatus = _withdrawStateList.value?.find {
            it.isSelected
        }?.code

        val uwType = _withdrawTypeList.value?.find {
            it.isSelected
        }?.type

        val startTime = _recordCalendarStartDate.value?.date
        val endTime = _recordCalendarEndDate.value?.date

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.getWithdrawList(
                    WithdrawListRequest(
                        checkStatus = checkStatus,
                        uwType = uwType,
                        startTime = startTime,
                        endTime = endTime,

                        )
                )
            }

            result?.rows?.map {
                it.withdrawState = when (it.checkStatus) {
                    CheckStatus.PROCESSING.code -> androidContext.getString(R.string.withdraw_log_state_processing)
                    CheckStatus.UN_PASS.code -> androidContext.getString(R.string.withdraw_log_state_un_pass)
                    CheckStatus.PASS.code -> androidContext.getString(R.string.withdraw_log_state_pass)
                    else -> ""
                }

                it.withdrawType = when (it.uwType) {
                    UWType.ADMIN_SUB_MONEY.type -> androidContext.getString(R.string.withdraw_log_type_admin)
                    UWType.BANK_TRANSFER.type -> androidContext.getString(R.string.withdraw_log_type_bank_trans)
                    else -> ""
                }

                it.withdrawDate = TimeUtil.timeFormat(it.applyTime, "yyyy-MM-dd")
                it.withdrawTime = TimeUtil.timeFormat(it.applyTime, "HH:mm:ss")

                it.displayMoney = ArithUtil.toMoneyFormat(it.applyMoney)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            _userWithdrawResult.postValue(result)
        }
    }

    fun setLogDetail(row: Row) {
        val logDetail = LogDetail(
            row.orderNo,
            TimeUtil.timeFormat(row.operatorTime, "yyyy-MM-dd HH:mm:ss"),
            row.rechName,
            row.displayMoney,
            row.rechState
        )

        _logDetail.postValue(logDetail)
    }

    fun setLogDetail(row: org.cxct.sportlottery.network.withdraw.list.Row) {
        val logDetail = LogDetail(
            row.orderNo,
            TimeUtil.timeFormat(row.operatorTime, "yyyy-MM-dd HH:mm:ss"),
            row.uwType,
            row.displayMoney,
            row.withdrawState
        )

        _logDetail.postValue(logDetail)
    }
}