package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteRequest
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.money.MoneyRechCfgData
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil

class WithdrawViewModel(
    private val androidContext: Context,
    private val moneyRepository: MoneyRepository,
    private val userInfoRepository: UserInfoRepository,
    betInfoRepo: BetInfoRepository
) : BaseViewModel() {

    init {
        betInfoRepository = betInfoRepo
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val userInfo = userInfoRepository.userInfo.asLiveData()

    private val _userMoney = BroadcastRepository().instance().userMoney
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    val bankCardList: LiveData<BankMyResult>
        get() = _bankCardList
    private var _bankCardList = MutableLiveData<BankMyResult>()

    val bankAddResult: LiveData<BankAddResult>
        get() = _bankAddResult
    private var _bankAddResult = MutableLiveData<BankAddResult>()

    val bankDeleteResult: LiveData<BankDeleteResult>
        get() = _bankDeleteResult
    private var _bankDeleteResult = MutableLiveData<BankDeleteResult>()

    val withdrawAddResult: LiveData<WithdrawAddResult>
        get() = _withdrawAddResult
    private var _withdrawAddResult = MutableLiveData<WithdrawAddResult>()

    //獲取資金設定
    val rechargeConfigs: LiveData<MoneyRechCfgData>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData>()

    //--銀行卡編輯頁面
    //開戶名錯誤訊息
    val createNameErrorMsg: LiveData<String>
        get() = _createNameErrorMsg
    private var _createNameErrorMsg = MutableLiveData<String>()

    //銀行卡號錯誤訊息
    val bankCardNumberMsg: LiveData<String>
        get() = _bankCardNumberMsg
    private var _bankCardNumberMsg = MutableLiveData<String>()

    //開戶網點錯誤訊息
    val networkPointMsg: LiveData<String>
        get() = _networkPointMsg
    private var _networkPointMsg = MutableLiveData<String>()

    //提款密碼錯誤訊息
    val withdrawPasswordMsg: LiveData<String>
        get() = _withdrawPasswordMsg
    private var _withdrawPasswordMsg = MutableLiveData<String>()

    //提款金額錯誤訊息
    val withdrawAmountMsg: LiveData<String>
        get() = _withdrawAmountMsg
    private var _withdrawAmountMsg = MutableLiveData<String>()

    //提款手續費提示
    val withdrawRateHint: LiveData<String>
        get() = _withdrawRateHint
    private var _withdrawRateHint = MutableLiveData<String>()

    fun addWithdraw(bankCardId: Long, applyMoney: String, withdrawPwd: String) {
        checkWithdrawAmount(applyMoney)
        checkWithdrawPassword(withdrawPwd)
        if (checkWithdrawData()) {
            loading()
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.withdrawService.addWithdraw(getWithdrawAddRequest(bankCardId, applyMoney, withdrawPwd))
                }?.let { result ->
                    _withdrawAddResult.value = result
                    hideLoading()
                }
            }
        }

    }

    private fun getWithdrawAddRequest(bankCardId: Long, applyMoney: String, withdrawPwd: String): WithdrawAddRequest {
        return WithdrawAddRequest(
            id = bankCardId,
            applyMoney = applyMoney.toLong(),
            withdrawPwd = MD5Util.MD5Encode(withdrawPwd)
        )
    }

    private fun checkWithdrawData(): Boolean {
        if (withdrawAmountMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    fun getBankCardList() {
        viewModelScope.launch {
            loading()
            doNetwork(androidContext) {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result ->
                _bankCardList.value = result
                hideLoading()
            }
        }
    }

    fun addBankCard(bankName: String, subAddress: String, cardNo: String, fundPwd: String, fullName: String, id: String?, uwType: String, bankCode: String) {
        checkInputBankCardData(fullName, cardNo, subAddress, fundPwd)
        if (checkBankCardData()) {
            viewModelScope.launch {
                loading()
                doNetwork(androidContext) {
                    val userId = userInfoRepository.userInfo.firstOrNull()?.userId.toString()
                    OneBoSportApi.bankService.bankAdd(createBankAddRequest(bankName, subAddress, cardNo, fundPwd, fullName, id, userId, uwType, bankCode))
                }?.let { result ->
                    _bankAddResult.value = result
                    hideLoading()
                }
            }
        }
    }

    private fun checkInputBankCardData(fullName: String, cardNo: String, subAddress: String, withdrawPassword: String) {
        checkCreateName(fullName)
        checkBankCardNumber(cardNo)
        checkNetWorkPoint(subAddress)
        checkWithdrawPassword(withdrawPassword)

    }

    private fun createBankAddRequest(
        bankName: String,
        subAddress: String,
        cardNo: String,
        fundPwd: String,
        fullName: String,
        id: String?,
        userId: String,
        uwType: String,
        bankCode: String
    ): BankAddRequest {
        return BankAddRequest(
            bankName = bankName,
            subAddress = subAddress,
            cardNo = cardNo,
            fundPwd = MD5Util.MD5Encode(fundPwd),
            fullName = fullName,
            id = id,
            userId = userId,
            uwType = uwType, //TODO Dean : 目前只有銀行一種, 還沒有UI可以做選擇, 先暫時寫死.
            bankCode = bankCode
        )
    }

    fun deleteBankCard(id: Long, fundPwd: String) {
        checkInputBankCardDeleteData(fundPwd)
        if (checkBankCardDeleteData()) {
            loading()
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.bankService.bankDelete(createBankDeleteRequest(id, MD5Util.MD5Encode(fundPwd)))
                }?.let { result ->
                    _bankDeleteResult.value = result
                    hideLoading()
                }
            }
        }
    }

    private fun checkInputBankCardDeleteData(fundPwd: String) {
        checkWithdrawPassword(fundPwd)
    }

    private fun createBankDeleteRequest(id: Long, fundPwd: String): BankDeleteRequest {
        return BankDeleteRequest(id = id, fundPwd = fundPwd)
    }

    fun getMoneyConfigs() {
        viewModelScope.launch {
            loading()
            doNetwork(androidContext) {
                moneyRepository.getRechCfg()
            }?.let { result ->
                result.rechCfg?.let { _rechargeConfigs.value = it }
                hideLoading()
            }
        }
    }

    fun getMoney() {
        loading()
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
            hideLoading()
        }
    }

    fun clearBankCardFragmentStatus() {
        //若不清除下一次進入編輯銀行卡頁面時會直接觸發觀察判定編輯成功
        _bankDeleteResult = MutableLiveData()
        _bankAddResult = MutableLiveData()
        _createNameErrorMsg = MutableLiveData()
        _bankCardNumberMsg = MutableLiveData()
        _networkPointMsg = MutableLiveData()
        _withdrawPasswordMsg = MutableLiveData()
    }

    private fun checkBankCardData(): Boolean {
        if (createNameErrorMsg.value != "")
            return false
        if (bankCardNumberMsg.value != "")
            return false
        if (networkPointMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    //TODO Dean : bank delete check
    private fun checkBankCardDeleteData(): Boolean {
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    fun checkCreateName(createName: String) {
        _createNameErrorMsg.value = when {
            createName.isEmpty() -> androidContext.getString(R.string.error_create_name_empty)
            !VerifyConstUtil.verifyCreateName(createName) -> {
                androidContext.getString(R.string.error_create_name)
            }
            else -> ""
        }
    }

    fun checkBankCardNumber(bankCardNumber: String) {
        _bankCardNumberMsg.value = when {
            bankCardNumber.isEmpty() -> androidContext.getString(R.string.error_bank_card_number_empty)
            !VerifyConstUtil.verifyBankCardNumber(bankCardNumber) -> {
                androidContext.getString(R.string.error_bank_card_number)
            }
            else -> ""
        }
    }

    fun checkNetWorkPoint(networkPoint: String) {
        _networkPointMsg.value = when {
            networkPoint.isEmpty() -> androidContext.getString(R.string.error_network_point_empty)
            !VerifyConstUtil.verifyNetworkPoint(networkPoint) -> {
                androidContext.getString(R.string.error_network_point)
            }
            else -> ""
        }
    }

    fun checkWithdrawPassword(withdrawPassword: String) {
        _withdrawPasswordMsg.value = when {
            withdrawPassword.isEmpty() -> androidContext.getString(R.string.error_withdraw_password_empty)
            !VerifyConstUtil.verifyWithdrawPassword(withdrawPassword) -> {
                androidContext.getString(R.string.error_withdraw_password)
            }
            else -> ""
        }
    }

    fun checkWithdrawAmount(withdrawAmount: String) {
        _withdrawAmountMsg.value = when {
            withdrawAmount.isEmpty() -> {
                androidContext.getString(R.string.error_withdraw_amount_empty)
            }
            !VerifyConstUtil.verifyWithdrawAmount(
                withdrawAmount,
                rechargeConfigs.value?.withdrawCfg?.withDrawBalanceLimit ?: 100,
                rechargeConfigs.value?.withdrawCfg?.maxWithdrawMoney,
                _userMoney.value,
                ArithUtil.toMoneyFormat((rechargeConfigs.value?.withdrawCfg?.wdRate)?.times(withdrawAmount.toLong())).toDouble()
            ) -> {// TODO Dean : 根據config獲取 但只有最小沒有最大
                getWithdrawRate(withdrawAmount.toLong())
                androidContext.getString(R.string.error_withdraw_amount)
            }
            else -> {
                getWithdrawRate(withdrawAmount.toLong())
                ""
            }
        }
    }

    fun getWithdrawRate(withdrawAmount: Long) {
        _withdrawRateHint.value = String.format(
            androidContext.getString(R.string.withdraw_handling_fee_hint),
            ArithUtil.toMoneyFormat(rechargeConfigs.value?.withdrawCfg?.wdRate?.times(100)),
            ArithUtil.toMoneyFormat((rechargeConfigs.value?.withdrawCfg?.wdRate)?.times(withdrawAmount))
        )
    }

    fun resetWithdrawPage() {
        _withdrawAmountMsg.value = ""
        _withdrawPasswordMsg.value = ""
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }
}