package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.repository.MoneyRepository
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.VerifyConstUtil

class WithdrawViewModel(private val androidContext: Context, private val moneyRepository: MoneyRepository) : BaseViewModel() {

    val needToUpdateWithdrawPassword: LiveData<Boolean>
        get() = _needToUpdateWithdrawPassword
    private var _needToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val checkBankCardOrNot: LiveData<Boolean>
        get() = _checkBankCardOrNot
    private var _checkBankCardOrNot = MutableLiveData<Boolean>()

    val bankCardList: LiveData<BankMyResult>
        get() = _bankCardList
    private var _bankCardList = MutableLiveData<BankMyResult>()

    val bankAddResult: LiveData<BankAddResult>
        get() = _bankAddResult
    private var _bankAddResult = MutableLiveData<BankAddResult>()

    val bankDeleteResult: LiveData<BankDeleteResult>
        get() = _bankDeleteResult
    private var _bankDeleteResult = MutableLiveData<BankDeleteResult>()

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

    fun addWithdraw(withdrawAddRequest: WithdrawAddRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.addWithdraw(withdrawAddRequest)
            }
        }
    }

    fun getBankCardList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result ->
                _bankCardList.value = result
                _checkBankCardOrNot.value = !result.bankCardList.isNullOrEmpty()
            }
        }
    }

    fun addBankCard(bankAddRequest: BankAddRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.bankService.bankAdd(bankAddRequest)
            }?.let { result ->
                _bankAddResult.value = result

            }
        }
    }

    fun deleteBankCard(id: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.bankService.bankDelete(id)
            }?.let { result ->
                _bankDeleteResult.value = result

            }
        }
    }


    fun checkPermissions() {
        //TODO Dean : 此處sUserInfo為寫死測試資料, 待api串接過後取得真的資料重新review
        if (sUserInfo.updatePayPw != 0 && needToUpdateWithdrawPassword.value == false) {
            _needToUpdateWithdrawPassword.value = true
        }
        getBankCardList()
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

    fun checkBankCardData(): Boolean {
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
}