package org.cxct.sportlottery.ui.finance

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.QueryByBettingStationIdResult
import org.cxct.sportlottery.network.money.list.*
import org.cxct.sportlottery.network.withdraw.list.WithdrawListRequest
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.finance.df.*
import org.cxct.sportlottery.util.*

const val pageSize = 20

class FinanceViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    val allTag = "ALL"

    val isLoading: LiveData<Boolean> //使用者餘額
        get() = _isLoading

    val userWithdrawListResult: LiveData<MutableList<org.cxct.sportlottery.network.withdraw.list.Row>?>
        get() = _userWithdrawResult
    val userSportBillListResult: LiveData<SportBillResult>
        get() = _userSportBillListResult
    val recordType: LiveData<String>
        get() = _recordType

    val isFinalPage: LiveData<Boolean>
        get() = _isFinalPage

    val accountHistoryList: LiveData<List<SportBillResult.Row>>
        get() = _accountHistoryList

    private val _isLoading = MutableLiveData<Boolean>()
    private val _userWithdrawResult =
        MutableLiveData<MutableList<org.cxct.sportlottery.network.withdraw.list.Row>?>()
    private val _userSportBillListResult = MutableLiveData<SportBillResult>()

    private val _recordType = MutableLiveData<String>()

    private val _isFinalPage = MutableLiveData<Boolean>().apply { value = false }
    private var page = 1

    private val _accountHistoryList = MutableLiveData<List<SportBillResult.Row>>()


    private val _redEnvelopeListResult = MutableLiveData<MutableList<RedEnvelopeRow>?>()
    val redEnvelopeListResult: LiveData<MutableList<RedEnvelopeRow>?>
        get() = _redEnvelopeListResult

    private val _queryByBettingStationIdResult =
        MutableLiveData<Event<QueryByBettingStationIdResult>>()
    val queryByBettingStationIdResult: LiveData<Event<QueryByBettingStationIdResult>>
        get() = _queryByBettingStationIdResult

    fun setRecordType(recordType: String) {
        _recordType.postValue(recordType)
    }

    private fun loading() {
        _isLoading.postValue(true)
    }

    private fun hideLoading() {
        _isLoading.postValue(false)
    }

    val rechargeLogDataList = SingleLiveEvent<Triple<List<Row>?, Boolean, String?>>()
    private lateinit var rechargeReqTag: Any
    fun getUserRechargeList(
        pageIndex: Int,
        pageSizeNum: Int,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        status: String? = null,
        rechType: String? = null,
    ) {
        val tag = Any()
        rechargeReqTag = tag

        val filter = { item: String? -> if (item == allTag || item.isNullOrBlank()) null else item }

        doRequest({
            OneBoSportApi.moneyService.getUserRechargeList(
                RechargeListRequest(
                    rechType = filter(rechType),
                    status = filter(status)?.toIntOrNull(),
                    startTime = startTime,
                    endTime = endTime,
                    page = pageIndex,
                    pageSize = pageSizeNum
                )
            )
        }) { result ->

            if (tag != rechargeReqTag) {
                return@doRequest
            }

            if (result == null) {
                rechargeLogDataList.postValue(Triple(null, false, ""))
                return@doRequest
            }

            result.rows?.forEach {
                it.rechState = when (it.status) {
                    Status.SUCCESS.code -> LocalUtils.getString(R.string.recharge_state_success)
                    Status.FAILED.code -> LocalUtils.getString(R.string.recharge_state_failed)
                    Status.PROCESSING.code, Status.RECHARGING.code -> LocalUtils.getString(R.string.log_state_processing)
                    else -> ""
                }

                it.rechTypeDisplay = when (it.rechType) {
                    RechType.ONLINE_PAYMENT.type -> LocalUtils.getString(R.string.recharge_channel_online)
                    RechType.ADMIN_ADD_MONEY.type -> LocalUtils.getString(R.string.recharge_channel_admin)
                    RechType.CFT.type -> LocalUtils.getString(R.string.recharge_channel_cft)
                    RechType.WEIXIN.type -> LocalUtils.getString(R.string.recharge_channel_weixin)
                    RechType.ALIPAY.type -> LocalUtils.getString(R.string.recharge_channel_alipay)
                    RechType.BANK_TRANSFER.type -> LocalUtils.getString(R.string.recharge_channel_bank)
                    RechType.CRYPTO.type -> LocalUtils.getString(R.string.recharge_channel_crypto)
                    RechType.GCASH.type -> LocalUtils.getString(R.string.recharge_channel_gcash)
                    RechType.GRABPAY.type -> LocalUtils.getString(R.string.recharge_channel_grabpay)
                    RechType.PAYMAYA.type -> LocalUtils.getString(R.string.recharge_channel_paymaya)
                    RechType.BETTING_STATION.type -> LocalUtils.getString(R.string.betting_station_deposit)
                    RechType.BETTING_STATION_AGENT.type -> LocalUtils.getString(R.string.P183)
                    RechType.ACTIVITY.type -> LocalUtils.getString(R.string.text_account_history_activity)
                    RechType.REDEMTIONCODE.type -> LocalUtils.getString(R.string.P216)
                    else -> ""
                }

                it.rechDateAndTime = TimeUtil.stampToDateHMSByRecord(it.addTime)
                it.rechDateStr = TimeUtil.timeFormat(it.addTime, "yyyy/MM/dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.addTime, "HH:mm:ss")
                it.displayMoney = TextUtil.formatMoney(it.rechMoney)
            }

            rechargeLogDataList.postValue(Triple(result.rows, result.success, result.msg))
        }
    }

    fun getUserAccountHistory(
        isFirstFetch: Boolean = false,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
        tranTypeGroup: String? = "",
    ) {
        if (isFinalPage.value == true && !isFirstFetch) {
            return
        }

        if (isFirstFetch) {
            _isFinalPage.postValue(false)
            page = 1
        }

        loading()

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.moneyService.getBillList(
                    SportBillListRequest(
                        tranTypeGroup = tranTypeGroup,
                        startTime = startTime,
                        endTime = endTime,
                        page = page,
                        pageSize = pageSize
                    )
                )
            }?.let { result ->
                if (result.success) {
                    if (result.rows.size < pageSize) {
                        _isFinalPage.postValue(true)
                    }

                    page++

                    result.rows.map {
                        it.addTime = TimeUtil.timeFormat(it.addTime.toLong(), TimeUtil.YMD_HMS_FORMAT_CHANGE_LINE_2)
                        val split = it.addTime.split(" ")
                        it.rechDateStr = split[0]
                        it.rechTimeStr = split[1]
                    }

                    _accountHistoryList.postValue(
                        mutableListOf<SportBillResult.Row>().apply {
                            if (!isFirstFetch) {
                                addAll(accountHistoryList.value ?: listOf())
                            }
                            addAll(result.rows)
                        }
                    )

                    _userSportBillListResult.postValue(result)
                } else {
                    ToastUtil.showToastInCenter(
                        androidContext,
                        result.msg
                    )
                }

                hideLoading()
            }

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
                OneBoSportApi.withdrawService.getWithdrawList(
                    WithdrawListRequest(
                        checkStatus = filter(checkStatus)?.toIntOrNull(),
                        uwType = filter(uwType),
                        startTime = startTime,
                        endTime = endTime
                    )
                )
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
                    CheckStatus.PROCESSING_TWO.code,
                    CheckStatus.BetStation.code,
                    CheckStatus.PROCESSING.code,
                    -> LocalUtils.getString(R.string.log_state_processing)
                    CheckStatus.UN_PASS.code -> LocalUtils.getString(R.string.recharge_state_failed)
                    CheckStatus.PASS.code -> LocalUtils.getString(R.string.recharge_state_success)
                    else -> ""
                }

                it.withdrawType = when (it.uwType) {
                    UWType.ADMIN_SUB_MONEY.type -> LocalUtils.getString(R.string.withdraw_log_type_admin)
                    UWType.BANK_TRANSFER.type -> LocalUtils.getString(R.string.withdraw_log_type_bank_trans)
                    UWType.CRYPTO.type -> LocalUtils.getString(R.string.withdraw_log_crypto_transfer)
                    UWType.E_WALLET.type -> LocalUtils.getString(R.string.ewallet)
                    UWType.BETTING_STATION.type -> LocalUtils.getString(R.string.betting_station_reserve)
                    UWType.BETTING_STATION_ADMIN.type -> LocalUtils.getString(R.string.betting_station_withdraw)
                    else -> ""
                }

                it.applyTime?.let { nonNullApTime ->
                    it.withdrawDateAndTime =
                        TimeUtil.stampToDateHMSByRecord(nonNullApTime)
                    it.withdrawDate = TimeUtil.timeFormat(nonNullApTime, "yyyy/MM/dd")
                    it.withdrawTime = TimeUtil.timeFormat(nonNullApTime, "HH:mm:ss")
                }

                it.operatorTime?.let { nonNullOpTime ->
                    it.operatorDateAndTime =
                        TimeUtil.timeFormat(nonNullOpTime, "yyyy-MM-dd HH:mm:ss")
                }

                it.displayMoney = TextUtil.formatMoney(it.applyMoney ?: 0.0)

                it.withdrawDeductMoney = TextUtil.formatMoney(it.deductMoney ?: 0.0)
            }

            result?.rows?.let {
                withdrawLogList.clear()
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

    private val redEnvelopeLogList = mutableListOf<RedEnvelopeRow>()

    fun getRedEnvelopeHistoryList(
        isFirstFetch: Boolean,
        startTime: String? = TimeUtil.getDefaultTimeStamp().startTime,
        endTime: String? = TimeUtil.getDefaultTimeStamp().endTime,
    ) {
        if (!isFirstFetch && isFinalPage.value == true) return

        loading()

        when {
            isFirstFetch -> {
                redEnvelopeLogList.clear()
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
                OneBoSportApi.moneyService.getRedEnvelopeHistoryList(
                    RedEnvelopeListRequest(
                        startTime = startTime,
                        endTime = endTime,
                        page = page,
                        pageSize = pageSize
                    )
                )
            }

            result?.rows?.map {

                it.tranTypeDisplay = when (it.tranType) {
                    TranType.ENVELOPE_SEND.type -> LocalUtils.getString(R.string.redenvelope_trantype_send)
                    TranType.ENVELOPE_RECEIVE.type -> LocalUtils.getString(R.string.redenvelope_trantype_received)
                    else -> ""
                }

                it.rechDateAndTime = TimeUtil.stampToDateHMSByRecord(it.addTime)
                it.rechDateStr = TimeUtil.timeFormat(it.addTime, "yyyy/MM/dd")
                it.rechTimeStr = TimeUtil.timeFormat(it.addTime, "HH:mm:ss")
                it.displayMoney = TextUtil.formatMoney(it.money)
            }

            result?.rows?.let {
                redEnvelopeLogList.addAll(it)
            }

            result?.total?.let {
                _isFinalPage.postValue(page * pageSize >= it)
            }

            if (result?.success == true) {
                _redEnvelopeListResult.postValue(redEnvelopeLogList)
            }

            hideLoading()
        }

    }

    fun getQueryByBettingStationId(bettingStationId: Int?){
        viewModelScope.launch {
            doNetwork(androidContext){
                OneBoSportApi.bettingStationService.queryByBettingStationId(bettingStationId = bettingStationId)
            }?.let {
                _queryByBettingStationIdResult.postValue(Event(it))
            }
        }
    }

}