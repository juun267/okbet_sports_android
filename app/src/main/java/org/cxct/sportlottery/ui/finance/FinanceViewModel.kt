package org.cxct.sportlottery.ui.finance

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class FinanceViewModel(private val androidContext: Context) : BaseViewModel() {

    val userMoneyResult: LiveData<UserMoneyResult?>
        get() = _userMoneyResult

    private val _userMoneyResult = MutableLiveData<UserMoneyResult?>()

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoneyResult.postValue(userMoneyResult)
        }
    }

}