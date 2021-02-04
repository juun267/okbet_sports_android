package org.cxct.sportlottery.ui.profileCenter.money_transfer

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResponse
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import java.util.*

class MoneyTransferViewModel(
        private val androidContext: Context,
        private val userInfoRepository: UserInfoRepository,
        private val loginRepository: LoginRepository,
        betInfoRepository: BetInfoRepository,
) : BaseOddButtonViewModel(betInfoRepository) { //TODO Cheryl 為啥要觀察這個Ｒ


    val allBalanceResult: LiveData<GetAllBalanceResponse> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _allBalanceResult

    val userMoney: LiveData<Double?>
        get() = _userMoney


    private val _userMoney = BroadcastRepository().instance().userMoney //TODO Cheryl: UserMoney取得的正確方式?
    private var _allBalanceResult = MutableLiveData<GetAllBalanceResponse>()

    fun getAllBalance() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getAllBalance()
            }?.let { result ->
                _allBalanceResult.postValue(result)
            }
        }
    }
}