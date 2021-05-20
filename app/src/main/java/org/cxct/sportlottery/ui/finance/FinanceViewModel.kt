package org.cxct.sportlottery.ui.finance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.list.RechargeListRequest
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.network.withdraw.list.WithdrawListRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

const val pageSize = 20

class FinanceViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val allTag = "ALL"

    val isLoading: LiveData<Boolean> //使用者餘額
        get() = _isLoading

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val userRechargeListResult: LiveData<MutableList<Row>?>
        get() = _userRechargeListResult

    val userWithdrawListResult: LiveData<MutableList<org.cxct.sportlottery.network.withdraw.list.Row>?>
        get() = _userWithdrawResult

    val recordList: LiveData<List<Pair<String, Int>>>
        get() = _recordList

    val recordType: LiveData<String>
        get() = _recordType

    val withdrawLogDetail: LiveData<Event<org.cxct.sportlottery.network.withdraw.list.Row>>
        get() = _withdrawLogDetail

    val rechargeLogDetail: LiveData<Event<Row>>
        get() = _rechargeLogDetail

    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage

    private val _isLoading = MutableLiveData<Boolean>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _userRechargeListResult = MutableLiveData<MutableList<Row>?>()
    private val _userWithdrawResult = MutableLiveData<MutableList<org.cxct.sportlottery.network.withdraw.list.Row>?>()

    private val _recordList = MutableLiveData<List<Pair<String, Int>>>()
    private val _recordType = MutableLiveData<String>()

    private val _withdrawLogDetail = MutableLiveData<Event<org.cxct.sportlottery.network.withdraw.list.Row>>()
    private val _rechargeLogDetail = MutableLiveData<Event<Row>>()

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


    private val rechargeLogList = mutableListOf<Row>()

    fun getUserRechargeList(
        isFirstFetch: Boolean,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        status: String? = null,
        rechType: String? = null,
    ) {
        if (!isFirstFetch && isFinalPage.value == true) return

        loading()

        val filter = { item: String? -> if (item == allTag) null else item }

        when {
            isFirstFetch -> {
                rechargeLogList.clear()
                _isFinalPage.postValue(false)
                page = 1
            }
            else -> {
                if (isFinalPage.value == false) {
                    page++
                }
            }
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.moneyService.getUserRechargeList(RechargeListRequest(rechType = filter(rechType), status = filter(status)?.toIntOrNull(), startTime = startTime, endTime = endTime, page = page, pageSize = pageSize))
            }

            result?.rows?.map {
                it.rechState = when (it.status) {
                    Status.SUCCESS.code -> androidContext.getString(R.string.recharge_state_success)
                    Status.FAILED.code -> androidContext.getString(R.string.recharge_state_failed)
                    Status.PROCESSING.code, Status.RECHARGING.code -> androidContext.getString(R.string.recharge_state_processing)
                    else -> ""
                }

                it.rechTypeDisplay = when (it.rechType) {
                    RechType.ONLINE_PAYMENT.type -> androidContext.getString(R.string.recharge_channel_online)
                    RechType.ADMIN_ADD_MONEY.type -> androidContext.getString(R.string.recharge_channel_admin)
                    RechType.CFT.type -> androidContext.getString(R.string.recharge_channel_cft)
                    RechType.WEIXIN.type -> androidContext.getString(R.string.recharge_channel_weixin)
                    RechType.ALIPAY.type -> androidContext.getString(R.string.recharge_channel_alipay)
                    RechType.BANK_TRANSFER.type -> androidContext.getString(R.string.recharge_channel_bank)
                    RechType.CRYPTO.type -> androidContext.getString(R.string.recharge_channel_crypto)
                    else -> ""
                }

                it.rechDateAndTime = TimeUtil.timeFormat(it.addTime, "yyyy-MM-dd HH:mm:ss")
                it.rechDateStr = TimeUtil.timeFormat(it.addTime, "yyyy-MM-dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.addTime, "HH:mm:ss")

                it.displayMoney = TextUtil.formatMoney(it.rechMoney)
            }

            result?.rows?.let {
                rechargeLogList.addAll(it)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            if (result?.success == true) {
                _userRechargeListResult.postValue(rechargeLogList)
            }

            hideLoading()
        }
    }


    private val withdrawLogList = mutableListOf<org.cxct.sportlottery.network.withdraw.list.Row>()

    fun getUserWithdrawList(
        isFirstFetch: Boolean,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        checkStatus: String? = null,
        uwType: String? = null,
    ) {
        if (!isFirstFetch && isFinalPage.value == true) return
        loading()
        val filter = { item: String? -> if (item == allTag) null else item }
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.getWithdrawList(WithdrawListRequest(checkStatus = filter(checkStatus)?.toIntOrNull(), uwType = filter(uwType), startTime = startTime, endTime = endTime))
            }

            result.apply {
                when {
                    isFirstFetch -> {
                        withdrawLogList.clear()
                        _isFinalPage.postValue(false)
                        page = 1
                    }
                    else -> {
                        if (isFinalPage.value == false) {
                            page++
                        }
                    }
                }
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
                    UWType.CRYPTO.type -> androidContext.getString(R.string.withdraw_log_crypto_transfer)
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

                it.displayMoney = TextUtil.formatMoney(it.applyMoney ?: 0.0)
            }

            result?.rows?.let {
                withdrawLogList.addAll(it)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            if (result?.success == true) {
                _userWithdrawResult.postValue(withdrawLogList)
            }

            hideLoading()
        }
    }

    fun setLogDetail(row: Event<Row>) {
        _rechargeLogDetail.postValue(row)
    }

    fun setWithdrawLogDetail(row: Event<org.cxct.sportlottery.network.withdraw.list.Row>) {
        _withdrawLogDetail.postValue(row)
    }
}