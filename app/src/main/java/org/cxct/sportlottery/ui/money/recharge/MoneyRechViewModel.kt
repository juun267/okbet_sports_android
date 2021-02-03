package org.cxct.sportlottery.ui.money.recharge

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants.BASE_URL
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ONLINE_PAY
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.*
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.JumpUtil.toExternalWeb
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.QueryUtil.toUrlParamsFormat
import org.cxct.sportlottery.util.VerifyConstUtil

class MoneyRechViewModel(
    private val androidContext: Context,
    private val moneyRepository: MoneyRepository,
    private val loginRepository: LoginRepository,
    betInfoRepo: BetInfoRepository
) : BaseViewModel() {

    init {
        betInfoRepository = betInfoRepo
    }

    val rechargeConfigs: LiveData<MoneyRechCfgData?>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData?>()

    //在線支付
    val onlinePayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _onlinePayList
    private var _onlinePayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    //轉帳支付
    val transferPayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _transferPayList
    private var _transferPayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    //Submit後API回傳
    val apiResult: LiveData<MoneyAddResult>
        get() = _apiResult
    private var _apiResult = MutableLiveData<MoneyAddResult>()

    //在線充值提交申請
    val onlinePaySubmit: LiveData<Long>
        get() = _onlinePaySubmit
    private var _onlinePaySubmit = MutableLiveData<Long>()

    //充值金額錯誤訊息
    val rechargeAmountMsg: LiveData<String>
        get() = _rechargeAmountMsg
    private var _rechargeAmountMsg = MutableLiveData<String>()

    //微信錯誤訊息
    val wxErrorMsg: LiveData<String>
        get() = _wxErrorMsg
    private var _wxErrorMsg = MutableLiveData<String>()

    //姓名錯誤訊息
    val nameErrorMsg: LiveData<String>
        get() = _nameErrorMsg
    private var _nameErrorMsg = MutableLiveData<String>()

    //銀行卡號錯誤訊息
    val bankIDErrorMsg: LiveData<String>
        get() = _bankIDErrorMsg
    private var _bankIDErrorMsg = MutableLiveData<String>()

    //暱稱錯誤訊息
    val nickNameErrorMsg: LiveData<String>
        get() = _nickNameErrorMsg
    private var _nickNameErrorMsg = MutableLiveData<String>()

    //線上支付充值金額
    val rechargeOnlineAmountMsg: LiveData<String>
        get() = _rechargeOnlineAmountMsg
    private var _rechargeOnlineAmountMsg = MutableLiveData<String>()

    //使用者餘額
    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult
    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()


    //獲取充值的基礎配置
    fun getRechCfg() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                moneyRepository.getRechCfg()
            }
            result?.rechCfg?.let { _rechargeConfigs.value = result.rechCfg }
            result?.rechCfg?.rechCfgs?.let { filterBankList(it) }
        }
    }

    //篩選List要顯示的資料
    private fun filterBankList(rechConfigList: List<MoneyRechCfg.RechConfig>) {
        try {

            val onlineData: MutableList<MoneyPayWayData> = mutableListOf()
            val transferData: MutableList<MoneyPayWayData> = mutableListOf()

            val dataList: MutableList<MoneyPayWayData> = mutableListOf()
            MoneyManager.getMoneyPayWayList()?.forEach { moneyPayWay ->
                if (rechConfigList.firstOrNull {
                        it.rechType == "onlinePayment" && it.onlineType == moneyPayWay.onlineType
                                || it.rechType != "onlinePayment" && it.rechType == moneyPayWay.rechType
                    } != null) {
                    dataList.add(moneyPayWay)
                }
            }


            dataList.forEach {
                when (it.rechType) {
                    "onlinePayment" -> onlineData.add(it)
                    else -> transferData.add(it)
                }
            }

            _onlinePayList.value = onlineData
            _transferPayList.value = transferData

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //充值頁面[轉帳充值]-[按鈕]提交申請
    fun rechargeSubmit(moneyAddRequest: MoneyAddRequest, rechType: String?) {
        checkAll(moneyAddRequest, rechType)
        if (checkTransferPayInput()) {
            rechargeAdd(moneyAddRequest)
        }
    }

    //轉帳支付充值
    private fun rechargeAdd(moneyAddRequest: MoneyAddRequest) {
        if (checkTransferPayInput()) {
            viewModelScope.launch {
                if (checkTransferPayInput()) {
                    doNetwork(androidContext) {
                        moneyRepository.rechargeAdd(moneyAddRequest)
                    }.let {
                        it?.result = moneyAddRequest.depositMoney.toString()//金額帶入result
                        _apiResult.value = it
                    }
                }
            }
        }
    }

    //在線支付
    fun rechargeOnlinePay(context: Context, id: Int, depositMoney: Int, bankCode: String?) {
        if (onlinePayInput()) {
            var url = BASE_URL + USER_RECHARGE_ONLINE_PAY
            val queryMap = hashMapOf(
                "x-session-token" to (loginRepository.token ?: ""),
                "rechCfgId" to id.toString(),
                "bankCode" to (bankCode ?: ""),
                "depositMoney" to depositMoney.toString()
            )
            url += toUrlParamsFormat(queryMap)
            toExternalWeb(context, url)

            _onlinePaySubmit.value = depositMoney.toLong() //金額帶入result
        }
    }

    //送出前判斷全部
    private fun checkAll(moneyAddRequest: MoneyAddRequest, rechType: String?) {
        when (rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                checkUserName(moneyAddRequest.payerName)
                checkBankID(moneyAddRequest.payer ?: "")
            }
            MoneyType.WX_TYPE.code -> {
                checkWX(moneyAddRequest.payerName)
            }
            MoneyType.ALI_TYPE.code -> {
                checkNickName(moneyAddRequest.payerName)
                checkUserName(moneyAddRequest.payerInfo ?: "")
            }
        }
        checkRechargeAmount(moneyAddRequest.depositMoney.toString())
    }

    //充值金額驗證
    fun checkRechargeAmount(rechargeAmount: String) {
        _rechargeAmountMsg.value = when {
            rechargeAmount.isEmpty() -> {
                androidContext.getString(R.string.error_recharge_amount_empty)
            }
            !VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                0,
                9999999
//                rechargeConfigs.value?.rechCfgs?.get(dataIndex)?.minMoney?.toLong()?:0,
//                rechargeConfigs.value?.rechCfgs?.get(dataIndex)?.maxMoney?.toLong()
            ) -> {// TODO Bill
                androidContext.getString(R.string.error_recharge_amount)
            }
            else -> {
                ""
            }
        }
    }

    //在線充值金額
    fun checkRcgOnlineAmount(rechargeAmount: String) {
        _rechargeOnlineAmountMsg.value = when {
            rechargeAmount.isEmpty() -> {
                androidContext.getString(R.string.error_recharge_amount)
            }
            !VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                0,
                9999999
//                rechargeConfigs.value?.rechCfgs?.get(dataIndex)?.minMoney?.toLong()?:0,
//                rechargeConfigs.value?.rechCfgs?.get(dataIndex)?.maxMoney?.toLong()
            ) -> {// TODO Bill
                androidContext.getString(R.string.error_recharge_amount)
            }
            else -> {
                ""
            }
        }
    }

    //微信認證
    fun checkWX(wxID: String) {
        _wxErrorMsg.value = when {
            wxID.isEmpty() -> {
                androidContext.getString(R.string.error_wx)
            }
            !VerifyConstUtil.verifyWeChat(
                wxID
            ) -> {
                androidContext.getString(R.string.error_wx)
            }
            else -> {
                ""
            }
        }
    }

    //姓名認證
    fun checkUserName(userName: String) {
        _nameErrorMsg.value = when {
            userName.isEmpty() -> {
                androidContext.getString(R.string.error_user_name)
            }
            !VerifyConstUtil.verifyNickname(
                userName
            ) -> {
                androidContext.getString(R.string.error_user_name)
            }
            else -> {
                ""
            }
        }
    }

    //暱稱認證
    fun checkNickName(userName: String) {
        _nickNameErrorMsg.value = when {
            userName.isEmpty() -> {
                androidContext.getString(R.string.error_user_name)
            }
            !VerifyConstUtil.verifyNickname(
                userName
            ) -> {
                androidContext.getString(R.string.error_user_name)
            }
            else -> {
                ""
            }
        }
    }

    //銀行卡號認證
    fun checkBankID(bankId: String) {
        _bankIDErrorMsg.postValue(
            when {
                bankId.isEmpty() -> {
                    androidContext.getString(R.string.error_bank_id)
                }
                !VerifyConstUtil.verifyBankCardNumber(
                    bankId
                ) -> {
                    androidContext.getString(R.string.error_bank_id)
                }
                else -> {
                    ""
                }
            }
        )
    }

    //獲取使用者餘額
    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoneyResult.postValue(userMoneyResult)
        }
    }

    private fun checkTransferPayInput(): Boolean {
        if (!rechargeAmountMsg.value.isNullOrEmpty())
            return false
        if (!wxErrorMsg.value.isNullOrEmpty())
            return false
        if (!nameErrorMsg.value.isNullOrEmpty())
            return false
        if (!bankIDErrorMsg.value.isNullOrEmpty())
            return false
        if (!nickNameErrorMsg.value.isNullOrEmpty())
            return false
        return true
    }

    private fun onlinePayInput(): Boolean {
        if (!_rechargeOnlineAmountMsg.value.isNullOrEmpty())
            return false
        return true
    }

    fun clearnRechargeStatus() {
        _rechargeAmountMsg = MutableLiveData()
        _wxErrorMsg = MutableLiveData()
        _nameErrorMsg = MutableLiveData()
        _bankIDErrorMsg = MutableLiveData()
        _nickNameErrorMsg = MutableLiveData()
        _rechargeOnlineAmountMsg = MutableLiveData()
    }
}
