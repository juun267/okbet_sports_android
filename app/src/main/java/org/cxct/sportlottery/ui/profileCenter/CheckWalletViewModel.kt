package org.cxct.sportlottery.ui.profileCenter

import android.app.Application
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.jpush.android.api.JPushInterface
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.*
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfo
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoRequest
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.CateDetailData
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.setupOddsDiscount
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.CoroutineContext


abstract class CheckWalletViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val withdrawRepository = WithdrawRepository

    val withdrawSystemOperation = withdrawRepository.withdrawSystemOperation

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
    val twoFactorResult: LiveData<NetResult?>
        get() = _twoFactorResult
    private val _twoFactorResult = MutableLiveData<NetResult?>()

    //錯誤提示
    val errorMessageDialog: LiveData<String?>
        get() = _errorMessageDialog
    private val _errorMessageDialog = MutableLiveData<String?>()

    //認證成功
    val twoFactorSuccess: LiveData<Boolean?>
        get() = _twoFactorSuccess
    private val _twoFactorSuccess = MutableLiveData<Boolean?>()



    //是否正在请求提现开关
    private var checkWithdraw = false

    //判斷提現是否需要KYC認證
    private var _isWithdrawShowVerifyDialog = SingleLiveEvent<Event<Boolean>>()
    val isWithdrawShowVerifyDialog: LiveData<Event<Boolean>>
        get() = _isWithdrawShowVerifyDialog

    //判斷充值是否需要KYC認證
    private var _isRechargeShowVerifyDialog = SingleLiveEvent<Event<Boolean>>()
    val isRechargeShowVerifyDialog: LiveData<Event<Boolean>>
        get() = _isRechargeShowVerifyDialog

    private var _rechargeSystemOperation = MutableLiveData<Event<Boolean>>()
    val rechargeSystemOperation: LiveData<Event<Boolean>>
        get() = _rechargeSystemOperation

    //提款功能是否啟用
    fun checkWithdrawSystem() {
        if (checkWithdraw)
            return
        viewModelScope.launch {
            checkWithdraw = true
            doNetwork(androidContext) {
                withdrawRepository.checkWithdrawSystem()
            }
            checkWithdraw = false
        }
    }



    //充值功能是否啟用
    fun checkRechargeSystem() {

    }

    fun checkWithdrawKYCVerify(){
        _isWithdrawShowVerifyDialog.postValue(Event(UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value && isKYCVerifyWithdrawOpen()))
    }

    fun checkRechargeKYCVerify(){
        _isRechargeShowVerifyDialog.postValue(Event(UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value && isKYCVerifyRechargeOpen()))
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


