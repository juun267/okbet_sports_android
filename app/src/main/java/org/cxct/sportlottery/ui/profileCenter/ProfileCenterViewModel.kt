package org.cxct.sportlottery.ui.profileCenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import timber.log.Timber
import java.util.*

class ProfileCenterViewModel(private val androidContext: Context, private val userInfoRepository: UserInfoRepository, private val loginRepository: LoginRepository) : BaseViewModel() {

    val userInfo = userInfoRepository.userInfo.asLiveData()
    val token = loginRepository.token

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    private var _needToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val needToUpdateWithdrawPassword: LiveData<Boolean> //是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _needToUpdateWithdrawPassword

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun logout() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.logout()
            }.apply {
                loginRepository.clear()
                //TODO change timber to actual logout ui to da
                Timber.d("logout result is ${this?.success} ${this?.code} ${this?.msg}")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoRepository.getUserInfo()
        }
    }

    fun sayHello(): String? {
        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        return when {
            hour < 6 -> androidContext.getString(R.string.good_midnight) + ", "
            hour < 9 -> androidContext.getString(R.string.good_morning) + ", "
            hour < 12 -> androidContext.getString(R.string.good_beforenoon) + ", "
            hour < 14 -> androidContext.getString(R.string.good_noon) + ", "
            hour < 17 -> androidContext.getString(R.string.good_afternoon) + ", "
            hour < 19 -> androidContext.getString(R.string.good_evening) + ", "
            hour < 22 -> androidContext.getString(R.string.good_night) + ", "
            hour < 24 -> androidContext.getString(R.string.good_dreams) + ", "
            else -> null
        }
    }
    
}