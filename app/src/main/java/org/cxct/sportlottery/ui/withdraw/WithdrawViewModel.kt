package org.cxct.sportlottery.ui.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.MyResult
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.ui.base.BaseViewModel

class WithdrawViewModel : BaseViewModel() {

    val bankCardList: LiveData<MyResult>
        get() = _bankCardList
    private var _bankCardList = MutableLiveData<MyResult>()

    private fun getBankCardList() {
        viewModelScope.launch {
            doNetwork {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result -> _bankCardList.value = result }
        }
    }

    fun addBankCard(bankAddRequest: BankAddRequest) {
        viewModelScope.launch {
            doNetwork {
                OneBoSportApi.bankService.bankAdd(
                    bankAddRequest
                )
            }
        }
    }

    fun initBankListFragment() {
        getBankCardList()
    }
}