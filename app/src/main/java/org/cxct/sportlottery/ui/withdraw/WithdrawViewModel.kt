package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.MyResult
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.repository.sUserInfo
import org.cxct.sportlottery.ui.base.BaseViewModel

class WithdrawViewModel(private val androidContext: Context) : BaseViewModel() {

    val needToUpdateWithdrawPassword: LiveData<Boolean>
        get() = _needToUpdateWithdrawPassword
    private var _needToUpdateWithdrawPassword = MutableLiveData<Boolean>(false)
    val checkBankCardOrNot: LiveData<Boolean>
        get() = _checkBankCardOrNot
    private var _checkBankCardOrNot = MutableLiveData<Boolean>(false)

    val bankCardList: LiveData<MyResult>
        get() = _bankCardList
    private var _bankCardList = MutableLiveData<MyResult>()

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
                OneBoSportApi.bankService.bankAdd(
                    bankAddRequest
                )
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
}