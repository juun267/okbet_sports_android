package org.cxct.sportlottery.ui.money.recharge

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ONLINE_PAY
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.*
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.JumpUtil.toExternalWeb
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.QueryUtil.toUrlParamsFormat
import org.cxct.sportlottery.util.VerifyConstUtil

class MoneyRechViewModel(
    private val androidContext: Context,
    private val moneyRepository: MoneyRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

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
    fun rechargeSubmit(moneyAddRequest: MoneyAddRequest, rechType: String?, rechConfig: MoneyRechCfg.RechConfig?) {
        checkAll(moneyAddRequest, rechType, rechConfig)
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
    fun rechargeOnlinePay(context: Context, mSelectRechCfgs: MoneyRechCfg.RechConfig?, depositMoney: Int, bankCode: String?) {
        checkRcgOnlineAmount(depositMoney.toString(), mSelectRechCfgs)
        if (onlinePayInput()) {
            var url = Constants.getBaseUrl() + USER_RECHARGE_ONLINE_PAY
            val queryMap = hashMapOf(
                "x-session-token" to (loginRepository.token ?: ""),
                "rechCfgId" to (mSelectRechCfgs?.id ?: "").toString(),
                "bankCode" to (bankCode ?: ""),
                "depositMoney" to depositMoney.toString()
            )
            url += toUrlParamsFormat(queryMap)
            toExternalWeb(context, url)

            _onlinePaySubmit.value = depositMoney.toLong() //金額帶入result
        }
    }

    //轉帳支付 - 送出前判斷全部
    private fun checkAll(moneyAddRequest: MoneyAddRequest, rechType: String?, rechConfig: MoneyRechCfg.RechConfig?) {
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
        checkRechargeAmount(moneyAddRequest.depositMoney.toString(), rechConfig)
    }

    //充值金額驗證
    fun checkRechargeAmount(rechargeAmount: String, rechConfig: MoneyRechCfg.RechConfig?) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeAmountMsg.value = when {
            rechargeAmount.isEmpty() || rechargeAmount == "0" -> {
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) -> {
                androidContext.getString(R.string.error_recharge_amount)
            }
            else -> {
                ""
            }
        }
    }

    //在線充值金額
    fun checkRcgOnlineAmount(rechargeAmount: String, rechConfig: MoneyRechCfg.RechConfig?) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeOnlineAmountMsg.value = when {
            rechargeAmount.isEmpty() || rechargeAmount == "0" -> {
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) -> {
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
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyWeChat(
                wxID
            ) -> {
                androidContext.getString(R.string.error_incompatible_format)
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
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyFullName(
                userName
            ) -> {
                androidContext.getString(R.string.error_incompatible_format)
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
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyNickname(
                userName
            ) -> {
                androidContext.getString(R.string.error_incompatible_format)
            }
            else -> {
                ""
            }
        }
    }

    //銀行卡號認證
    fun checkBankID(bankId: String) {
        _bankIDErrorMsg.value = when {
            bankId.isEmpty() -> {
                androidContext.getString(R.string.error_input_empty)
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
        _rechargeAmountMsg.value = ""
        _wxErrorMsg.value = ""
        _nameErrorMsg.value = ""
        _bankIDErrorMsg.value = ""
        _nickNameErrorMsg.value = ""
        _rechargeOnlineAmountMsg.value = ""
    }
}
