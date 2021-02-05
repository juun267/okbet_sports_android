package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.AllTransferOutResult
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class MoneyTransferViewModel(
        private val androidContext: Context,
        private val userInfoRepository: UserInfoRepository,
        private val loginRepository: LoginRepository,
        betInfoRepository: BetInfoRepository,
) : BaseOddButtonViewModel(betInfoRepository) {


    val allBalanceResult: LiveData<GetAllBalanceResult> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _allBalanceResult

    val recycleAllMoneyResult: LiveData<AllTransferOutResult> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _recycleAllMoneyResult

    val userMoney: LiveData<Double?>
        get() = _userMoney


    private val _userMoney = MutableLiveData<Double?>()
    private var _allBalanceResult = MutableLiveData<GetAllBalanceResult>()
    private var _recycleAllMoneyResult = MutableLiveData<AllTransferOutResult>()


    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }

            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun getAllBalance() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getAllBalance()
            }?.let { result ->
                _allBalanceResult.postValue(result)
            }
        }
    }

    fun recycleAllMoney() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.allTransferOut()
            }?.let { result ->
                _recycleAllMoneyResult.value = result
            }
        }
    }
}