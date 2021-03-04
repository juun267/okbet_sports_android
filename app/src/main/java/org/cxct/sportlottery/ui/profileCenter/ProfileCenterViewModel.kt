package org.cxct.sportlottery.ui.profileCenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.util.TextUtil

class ProfileCenterViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val userInfo = userInfoRepository.userInfo.asLiveData()
    val token = loginRepository.token

    private val _userMoney = MutableLiveData<String?>()
    val userMoney: LiveData<String?>
        get() = _userMoney

    private var _needToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val needToUpdateWithdrawPassword: LiveData<Boolean> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _needToUpdateWithdrawPassword

    private var _needToCompleteProfileInfo = MutableLiveData<Boolean>()
    val needToCompleteProfileInfo: LiveData<Boolean> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = _needToCompleteProfileInfo


    private var _settingNeedToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val settingNeedToUpdateWithdrawPassword: LiveData<Boolean> //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _settingNeedToUpdateWithdrawPassword

    private var _needToBindBankCard = MutableLiveData<Boolean>()
    val needToBindBankCard: LiveData<Boolean>
        get() = _needToBindBankCard //提款頁面是否需要新增銀行卡 true: 需要, false:不需要

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }

            val formatMoney = userMoneyResult?.money?.let {
                TextUtil.format(it)
            }

            _userMoney.postValue(formatMoney)
        }
    }

    fun logout() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.logout()
            }.apply {
                loginRepository.clear()
                betInfoRepository.clear()
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoRepository.getUserInfo()
        }
    }

    private fun checkNeedUpdatePassWord(): Boolean? {
        return when (userInfo.value?.updatePayPw) {
            1 -> true
            0 -> false
            else -> null
        }
    }

    //提款判斷權限
    fun withdrawCheckPermissions() {
        this.checkNeedUpdatePassWord()?.let { _needToUpdateWithdrawPassword.value = it }
    }

    //提款設置判斷權限
    fun settingCheckPermissions() {
        this.checkNeedUpdatePassWord()?.let { _settingNeedToUpdateWithdrawPassword.value = it }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
        var complete = false
        sConfigData?.apply {
            if (enableWithdrawFullName == FLAG_OPEN && userInfo.value?.fullName.isNullOrBlank() ||
                enableWithdrawQQ == FLAG_OPEN && userInfo.value?.qq.isNullOrBlank() ||
                enableWithdrawEmail == FLAG_OPEN && userInfo.value?.email.isNullOrBlank() ||
                enableWithdrawPhone == FLAG_OPEN && userInfo.value?.phone.isNullOrBlank() ||
                enableWithdrawWechat == FLAG_OPEN && userInfo.value?.wechat.isNullOrBlank()
            ) {
                complete = true
            }
        }
        _needToCompleteProfileInfo.value = complete
    }

    fun checkBankCardPermissions() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result ->
                _needToBindBankCard.value = result.bankCardList.isNullOrEmpty()
            }
        }
    }
}