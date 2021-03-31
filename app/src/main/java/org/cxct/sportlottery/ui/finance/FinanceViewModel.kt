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
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.component.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TimeUtil

const val pageSize = 20

class FinanceViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val isLoading: LiveData<Boolean> //使用者餘額
        get() = _isLoading

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val userRechargeListResult: LiveData<RechargeListResult?>
        get() = _userRechargeResult

    val userWithdrawListResult: LiveData<WithdrawListResult?>
        get() = _userWithdrawResult

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    val recordType: LiveData<String>
        get() = _recordType

    val withdrawStateList = androidContext.resources.getStringArray(R.array.withdraw_state_array).map {
        when (it) {
            androidContext.getString(R.string.withdraw_log_state_processing) -> {
                StatusSheetData(CheckStatus.PROCESSING.code.toString(), it)
            }
            androidContext.getString(R.string.withdraw_log_state_pass) -> {
                StatusSheetData(CheckStatus.PASS.code.toString(), it)
            }
            androidContext.getString(R.string.withdraw_log_state_un_pass) -> {
                StatusSheetData(CheckStatus.UN_PASS.code.toString(), it)
            }
            else -> {
                StatusSheetData(null, it).apply { isChecked = true }
            }
        }
    }

    val withdrawTypeList = androidContext.resources.getStringArray(R.array.withdraw_type_array).map {
        when (it) {
            androidContext.getString(R.string.withdraw_log_type_bank_trans) -> {
                StatusSheetData(UWType.BANK_TRANSFER.type, it)
            }
            androidContext.getString(R.string.withdraw_log_type_admin) -> {
                StatusSheetData(UWType.ADMIN_SUB_MONEY.type, it)
            }
            else -> {
                StatusSheetData(null, it).apply { isChecked = true }
            }
        }
    }

    val withdrawLogDetail: LiveData<org.cxct.sportlottery.network.withdraw.list.Row>
        get() = _withdrawLogDetail

    val rechargeLogDetail: LiveData<Row>
        get() = _rechargeLogDetail

    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage

    private val _isLoading = MutableLiveData<Boolean>()
    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _userRechargeResult = MutableLiveData<RechargeListResult?>()
    private val _userWithdrawResult = MutableLiveData<WithdrawListResult?>()

    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()
    private val _recordType = MutableLiveData<String>()

    val rechargeStateList = androidContext.resources.getStringArray(R.array.recharge_state_array).map {
        when (it) {
            androidContext.getString(R.string.recharge_state_processing) -> {
                StatusSheetData(Status.PROCESSING.code.toString(), it)
            }
            androidContext.getString(R.string.recharge_state_success) -> {
                StatusSheetData(Status.SUCCESS.code.toString(), it)

            }
            androidContext.getString(R.string.recharge_state_failed) -> {
                StatusSheetData(Status.FAILED.code.toString(), it)
            }
            else -> {
                StatusSheetData(null, it).apply { isChecked = true }
            }
        }
    }


    val rechargeChannelList = androidContext.resources.getStringArray(R.array.recharge_channel_array).map {
        when (it) {
            androidContext.getString(R.string.recharge_channel_online) -> {
                StatusSheetData(RechType.ONLINE_PAYMENT.type, it)
            }
            androidContext.getString(R.string.recharge_channel_bank) -> {
                StatusSheetData(RechType.BANK_TRANSFER.type, it)
            }
            androidContext.getString(R.string.recharge_channel_alipay) -> {
                StatusSheetData(RechType.ALIPAY.type, it)
            }
            androidContext.getString(R.string.recharge_channel_weixin) -> {
                StatusSheetData(RechType.WEIXIN.type, it)
            }
            androidContext.getString(R.string.recharge_channel_cft) -> {
                StatusSheetData(RechType.CFT.type, it)
            }
            androidContext.getString(R.string.recharge_channel_admin) -> {
                StatusSheetData(RechType.ADMIN_ADD_MONEY.type, it)
            }
            else -> {
                StatusSheetData(null, it).apply { isChecked = true }
            }
        }
    }

    private val _withdrawLogDetail = MutableLiveData<org.cxct.sportlottery.network.withdraw.list.Row>()
    private val _rechargeLogDetail = MutableLiveData<Row>()

    private val _isFinalPage = MutableLiveData<Boolean>().apply { value = false }
    private var page = 1


    fun setRecordType(recordType: String) {
        _recordType.postValue(recordType)
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

    private fun loading() {
        _isLoading.postValue(true)
    }

    private fun hideLoading() {
        _isLoading.postValue(false)
    }

    fun getUserRechargeList(isFirstFetch: Boolean, startTime: String? = TimeUtil.getDefaultTimeStamp().startTime, endTime: String? = TimeUtil.getDefaultTimeStamp().endTime) {
        loading()
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

        val rechType = rechargeChannelList.find {
            it.isChecked
        }?.code

        val status = rechargeStateList.find {
            it.isChecked
        }?.code?.toIntOrNull()

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

                it.rechTypeDisplay = when (it.rechType) {
                    RechType.ONLINE_PAYMENT.type -> androidContext.getString(R.string.recharge_channel_online)
                    RechType.ADMIN_ADD_MONEY.type -> androidContext.getString(R.string.recharge_channel_admin)
                    RechType.CFT.type -> androidContext.getString(R.string.recharge_channel_cft)
                    RechType.WEIXIN.type -> androidContext.getString(R.string.recharge_channel_weixin)
                    RechType.ALIPAY.type -> androidContext.getString(R.string.recharge_channel_alipay)
                    RechType.BANK_TRANSFER.type -> androidContext.getString(R.string.recharge_channel_bank)
                    else -> ""
                }

                it.rechDateAndTime = TimeUtil.timeFormat(it.rechTime, "yyyy-MM-dd HH:mm:ss")
                it.rechDateStr = TimeUtil.timeFormat(it.rechTime, "yyyy-MM-dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.rechTime, "HH:mm:ss")

                it.displayMoney = ArithUtil.toMoneyFormat(it.rechMoney)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            _userRechargeResult.postValue(result)
            hideLoading()
        }
    }

    fun getUserWithdrawList(
        isFirstFetch: Boolean,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime
    ) {
        loading()
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

        val checkStatus = withdrawStateList.find {
            it.isChecked
        }?.code?.toIntOrNull()

        val uwType = withdrawTypeList.find {
            it.isChecked
        }?.code

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.getWithdrawList(
                    WithdrawListRequest(
                        checkStatus = checkStatus,
                        uwType = uwType,
                        startTime = startTime,
                        endTime = endTime
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

                it.applyTime?.let { nonNullApTime ->
                    it.withdrawDateAndTime = TimeUtil.timeFormat(nonNullApTime, "yyyy-MM-dd HH:mm:ss")
                    it.withdrawDate = TimeUtil.timeFormat(nonNullApTime, "yyyy-MM-dd")
                    it.withdrawTime = TimeUtil.timeFormat(nonNullApTime, "HH:mm:ss")
                }

                it.operatorTime?.let { nonNullOpTime ->
                    it.operatorDateAndTime = TimeUtil.timeFormat(nonNullOpTime, "yyyy-MM-dd HH:mm:ss")
                }

                it.displayMoney = ArithUtil.toMoneyFormat(it.applyMoney)
                it.displayFee = ArithUtil.toMoneyFormat(it.fee)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            _userWithdrawResult.postValue(result)
            hideLoading()
        }
    }

    fun setLogDetail(row: Row) {
        _rechargeLogDetail.postValue(row)
    }

    fun setWithdrawLogDetail(row: org.cxct.sportlottery.network.withdraw.list.Row) {
        _withdrawLogDetail.postValue(row)
    }
}