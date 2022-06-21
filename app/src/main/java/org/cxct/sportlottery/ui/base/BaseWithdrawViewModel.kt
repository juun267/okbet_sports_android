package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseSecurityCodeResult
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.WithdrawRepository

/**
 * @author kevin
 * @create 2022/6/21
 * @description
 */
abstract class BaseWithdrawViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    @SuppressLint("StaticFieldLeak")
    private val androidContext = MultiLanguagesApplication.appContext

    val withdrawRepository = WithdrawRepository

    val withdrawSystemOperation = withdrawRepository.withdrawSystemOperation

    val rechargeSystemOperation = withdrawRepository.rechargeSystemOperation

    //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val needToUpdateWithdrawPassword = withdrawRepository.needToUpdateWithdrawPassword

    //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword = withdrawRepository.settingNeedToUpdateWithdrawPassword

    //提款設置頁面是否需要完善個人資料 true: 需要, false: 不需要
    val settingNeedToCompleteProfileInfo = withdrawRepository.settingNeedToCompleteProfileInfo

    //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToCompleteProfileInfo = withdrawRepository.needToCompleteProfileInfo

    //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
    val needToBindBankCard = withdrawRepository.needToBindBankCard

    //判斷是不是要進行手機驗證 true: 需要, false: 不需要
    val needToSendTwoFactor = withdrawRepository.showSecurityDialog

    //進入提款頁前判斷
    val intoWithdraw = withdrawRepository.intoWithdraw

    //需要完善個人資訊(缺電話號碼) needPhoneNumber
    val showPhoneNumberMessageDialog = withdrawRepository.hasPhoneNumber

    //發送簡訊碼之後60s無法再發送
    val twoFactorResult: LiveData<BaseSecurityCodeResult?>
        get() = _twoFactorResult
    private val _twoFactorResult = MutableLiveData<BaseSecurityCodeResult?>()

    //錯誤提示
    val errorMessageDialog: LiveData<String?>
        get() = _errorMessageDialog
    private val _errorMessageDialog = MutableLiveData<String?>()

    //認證成功
    val twoFactorSuccess: LiveData<Boolean?>
        get() = _twoFactorSuccess
    private val _twoFactorSuccess = MutableLiveData<Boolean?>()

    //提款功能是否啟用
    fun checkWithdrawSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkWithdrawSystem()
            }
        }
    }

    //充值功能是否啟用
    fun checkRechargeSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkRechargeSystem()
            }
        }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
        viewModelScope.launch {
            withdrawRepository.checkProfileInfoComplete()
        }
    }

    fun checkBankCardPermissions() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkBankCardPermissions()
            }
        }
    }

    //發送簡訊驗證碼
    fun sendTwoFactor() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.sendTwoFactor()
            }
            _twoFactorResult.postValue(result)
        }
    }

    //双重验证校验
    fun validateTwoFactor(validateTwoFactorRequest: ValidateTwoFactorRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.validateTwoFactor(validateTwoFactorRequest)
            }?.let { result ->
                if(result.success){
                    _twoFactorSuccess.value = true
                    withdrawRepository.sendTwoFactor()
                }
                else
                    _errorMessageDialog.value = result.msg
            }
        }
    }

}