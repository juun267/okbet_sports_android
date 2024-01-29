package org.cxct.sportlottery.ui.selflimit

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.user.passwordVerify.PasswordVerifyRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeResult
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitRequest
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event

class SelfLimitViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    val isShowToolbar: LiveData<Int>
        get() = _isShowToolbar
    private var _isShowToolbar = MutableLiveData<Int>().apply { this.value = View.VISIBLE }

    val toolbarName: LiveData<String>
        get() = _toolbarName
    val frozeResult: LiveData<FrozeResult>
        get() = _frozeResult
    val perBetLimitResult: LiveData<PerBetLimitResult>
        get() = _perBetLimitResult
    val userInfoResult: LiveData<UserInfoResult>
        get() = _userInfoResult
    private val _toolbarName = MutableLiveData<String>()
    private val _frozeResult = MutableLiveData<FrozeResult>()
    private val _perBetLimitResult = MutableLiveData<PerBetLimitResult>()
    private val _userInfoResult = MutableLiveData<UserInfoResult>()


    val isBetEditTextError: LiveData<Boolean>
        get() = _isBetEditTextError
    private val _isBetEditTextError = MutableLiveData<Boolean>()

    val isFrozeEditTextError: LiveData<Boolean>
        get() = _isFrozeEditTextError
    private val _isFrozeEditTextError = MutableLiveData<Boolean>()

    val passwordVerifyResult: LiveData<Event<NetResult>>
        get() = _passwordVerifyResult
    private val _passwordVerifyResult = MutableLiveData<Event<NetResult>>()

    //使用者ID
    var userID: Long? = null


    fun setToolbarName(name: String) {
        _toolbarName.value = name
    }

    fun getUserInfo() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                UserInfoRepository.getUserInfo()
            }
            userID = result?.userInfoData?.userId
            result?.let {
                _userInfoResult.postValue(it)
            }
        }
    }

    fun showToolbar(isShow: Boolean) {
        if (isShow) _isShowToolbar.value = View.VISIBLE
        else _isShowToolbar.value = View.GONE
    }

    fun setBetEditTextError(boolean: Boolean) {
        _isBetEditTextError.postValue(boolean)
    }

    fun setFrozeEditTextError(boolean: Boolean) {
        _isFrozeEditTextError.postValue(boolean)
    }

    fun passwordVerifyForFroze(password: String, day: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.passwordVerify(PasswordVerifyRequest(password))
            }?.let { passwordVerifyResult ->
                _passwordVerifyResult.value = Event(passwordVerifyResult)
                if (!passwordVerifyResult.success) return@launch

                doNetwork(androidContext) {
                    SelfLimitRepository.froze(FrozeRequest(day))
                }?.let {
                    _frozeResult.postValue(it)
                }
            }
        }
    }

    fun passwordVerifyForLimitBet(password: String, mount: Int) {
        viewModelScope.launch {
            doNetwork(androidContext, false) {
                OneBoSportApi.userService.passwordVerify(PasswordVerifyRequest(password))
            }?.let { passwordVerifyResult ->
                _passwordVerifyResult.value = Event(passwordVerifyResult)
                if (!passwordVerifyResult.success) return@launch

                doNetwork(androidContext) {
                    SelfLimitRepository.setPerBetLimit(PerBetLimitRequest(mount))
                }?.let {
                    _perBetLimitResult.postValue(it)
                }
            }
        }
    }
}