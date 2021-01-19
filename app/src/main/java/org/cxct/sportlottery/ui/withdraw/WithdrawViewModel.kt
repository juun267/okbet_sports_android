package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseViewModel

class WithdrawViewModel(private val androidContext: Context) : BaseViewModel() {

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

                clearBankCardFragmentStatus()
            }
        }
    }

    fun deleteBankCard(id: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.bankService.bankDelete(id)
            }?.let { result ->
                _bankDeleteResult.value = result

                clearBankCardFragmentStatus()
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

    private fun clearBankCardFragmentStatus() {
        //若不清除下一次進入編輯銀行卡頁面時會直接觸發觀察判定編輯成功
        _bankDeleteResult = MutableLiveData()
        _bankAddResult = MutableLiveData()
    }
}