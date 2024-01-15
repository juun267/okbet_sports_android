package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.util.Event
import retrofit2.Response

object SecurityEnter{
    const val UPDATE_PW = 0//更新提款卡密碼
    const val SETTING_PW = 1//設定提現密碼
    const val COMPLETE_PROFILE_INFO = 2//完善個人資料
    const val SETTING_PROFILE_INFO = 3//設定個人資料
    const val BIND_BANK_CARD = 4//綁定銀行卡
    const val INTO_WITHDRAW = 5
}

object WithdrawRepository {


    private val userInfoRepository = UserInfoRepository

    private val userInfoFlow: LiveData<UserInfo?>
        get() = MultiLanguagesApplication.mInstance.userInfo

    private var mWithdrawSystemOperation = MutableLiveData<Event<Boolean>>()
    val withdrawSystemOperation: LiveData<Event<Boolean>>
        get() = mWithdrawSystemOperation

    private var mNeedToUpdateWithdrawPassword = MutableLiveData<Event<Boolean>>()
    val needToUpdateWithdrawPassword: LiveData<Event<Boolean>> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = mNeedToUpdateWithdrawPassword

    private var mSettingNeedToUpdateWithdrawPassword = MutableLiveData<Event<Boolean>>()
    val settingNeedToUpdateWithdrawPassword: LiveData<Event<Boolean>> //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = mSettingNeedToUpdateWithdrawPassword

    private var mNeedToCompleteProfileInfo = MutableLiveData<Event<Boolean>>()
    val needToCompleteProfileInfo: LiveData<Event<Boolean>> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = mNeedToCompleteProfileInfo

    private var mSettingNeedToCompleteProfileInfo = MutableLiveData<Event<Boolean>>()
    val settingNeedToCompleteProfileInfo: LiveData<Event<Boolean>> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = mSettingNeedToCompleteProfileInfo

    private var mNeedToBindBankCard = MutableLiveData<Event<Int?>>()
    val needToBindBankCard: LiveData<Event<Int?>> //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
        get() = mNeedToBindBankCard

    private var mIntoWithdraw = MutableLiveData<Event<Boolean>>()
    val intoWithdraw: LiveData<Event<Boolean>> //進入提款頁前判斷
        get() = mIntoWithdraw

    private var mShowSecurityDialog = MutableLiveData<Event<Boolean>>()
    val showSecurityDialog: LiveData<Event<Boolean>> //判斷是否需要簡訊驗證 true：顯示簡訊驗證彈窗 false：不顯示
        get() = mShowSecurityDialog

    private var mHasPhoneNumber = MutableLiveData<Event<Boolean>>()
    val hasPhoneNumber: LiveData<Event<Boolean>> //是否有手機號碼
        get() = mHasPhoneNumber

    private var mWithdrawOperation: SystemOperation? = null

    private var _moneyRechCfgResult = MutableLiveData<MoneyRechCfgResult>()
    val moneyRechCfgResult: LiveData<MoneyRechCfgResult>
        get() = _moneyRechCfgResult

    data class SystemOperation(
        val bankSystem: Boolean,
        val cryptoSystem: Boolean,
        val eWalletSystem: Boolean,
        val stationSystem: Boolean
    )

    //顯示驗證過了之後要繼續驗證前的邏輯
    suspend fun sendTwoFactor() {
        when (sConfigData?.enterCertified) {
            SecurityEnter.SETTING_PW -> settingCheckPermissions()
            SecurityEnter.UPDATE_PW -> withdrawCheckPermissions()
            SecurityEnter.COMPLETE_PROFILE_INFO -> checkProfileInfoComplete()
            SecurityEnter.SETTING_PROFILE_INFO -> checkSettingProfileInfoComplete()
            SecurityEnter.BIND_BANK_CARD -> checkBankCardPermissions()
            SecurityEnter.INTO_WITHDRAW -> checkIntoWithdraw()
        }
    }

//    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
//        val response = OneBoSportApi.moneyService.getRechCfg()
//        if (response.isSuccessful) {
//            _moneyRechCfgResult.postValue(response.body())
//        }
//        return response
//    }

    suspend fun checkWithdrawSystem(): Response<MoneyRechCfgResult> {
        val response = OneBoSportApi.moneyService.getRechCfg()
        if (response.isSuccessful) {
            val withdrawConfig = response.body()?.rechCfg?.uwTypes

            val bankWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.OPEN.code
            val cryptoWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.OPEN.code
            val eWalletWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.E_WALLET.type || it.type == TransferType.PAYMAYA.type }?.open == MoneyRechCfg.Switch.OPEN.code
            val statiomWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.STATION.type }?.open == MoneyRechCfg.Switch.OPEN.code

            mWithdrawOperation = SystemOperation(
                bankWithdrawSystemOperation,
                cryptoWithdrawSystemOperation,
                eWalletWithdrawSystemOperation,
                statiomWithdrawSystemOperation
            )

            val operation =
                bankWithdrawSystemOperation || cryptoWithdrawSystemOperation || eWalletWithdrawSystemOperation || statiomWithdrawSystemOperation
            mWithdrawSystemOperation.value = Event(operation)

            if (operation) {
                withdrawCheckPermissions()
            }

            _moneyRechCfgResult.postValue(response.body())
        }
        return response
    }

    suspend fun checkRechargeSystem(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg().apply {
            if (isSuccessful) {
                _moneyRechCfgResult.value = body()
            }
        }
    }

    private suspend fun checkNeedUpdatePassWord(): Boolean {
        if (userInfoFlow == null)
            runWithCatch { userInfoRepository.getUserInfo() }
        return userInfoFlow?.value?.updatePayPw == 1
    }

    //提款判斷權限
    private suspend fun withdrawCheckPermissions() {
        val needUpdatePassWord = checkNeedUpdatePassWord()
        when {
            //是否需要更新提款密碼
            needUpdatePassWord -> {
                val twoFactorStatus = checkTwoFactorStatus()

                //是否需要顯示簡訊驗證 false: 需顯示簡訊驗證
                if (twoFactorStatus == false) {
                    sConfigData?.enterCertified = SecurityEnter.UPDATE_PW
                    mShowSecurityDialog.value = Event(true)
                } else if (twoFactorStatus == true) {
                    //不需要簡訊驗證，提示需更新提款密碼
                    mNeedToUpdateWithdrawPassword.postValue(Event(needUpdatePassWord))
                }
            }
            verifyProfileInfoComplete() -> {
                checkProfileInfoComplete()
            }
            else -> {
                checkBankCardPermissions()
            }
        }
    }

    /**
     * 判斷要不要顯示簡訊驗證
     * success: true 验证成功, false 需重新验证手机
     */
    private suspend fun getTwoFactorStatus(): Boolean {
        //(success: true 验证成功, false 需重新验证手机), 在进行新增银行卡、更新银行卡密码、更新用户密码、设定真实姓名之前先判断此状态, 如果为false, 就显示验证手机简讯的画面
        val response = kotlin.runCatching { OneBoSportApi.withdrawService.getTwoFactorStatus() }.getOrNull() ?: return true
        return response.body()?.success ?: true
    }

    /**
     * 判斷要不要顯示簡訊驗證
     * @return true 验证成功, false 需重新验证手机, null 需簡訊驗證卻沒有手機號碼
     */
    private suspend fun checkTwoFactorStatus(): Boolean? {
        //(success: true 验证成功, false 需重新验证手机), 在进行新增银行卡、更新银行卡密码、更新用户密码、设定真实姓名之前先判断此状态, 如果为false, 就显示验证手机简讯的画面
        val response = kotlin.runCatching { OneBoSportApi.withdrawService.getTwoFactorStatus() }.getOrNull() ?: return true
        if (response.body()?.success == false) {
            checkUserHavePhoneNumber().let { hasPhone ->
                if (!hasPhone) {
                    //因需要顯示簡訊驗證卻沒有手機號碼時
                    mHasPhoneNumber.postValue(Event(hasPhone))
                    return null
                }
            }
        }
        return response.body()?.success ?: true
    }

    suspend fun checkNeedToShowSecurityDialog() {
        mShowSecurityDialog.value = Event(!getTwoFactorStatus())
    }

    /**
     * 使用者使否擁有手機號碼
     * @return true: 有, false: 沒有
     */
    private suspend fun checkUserHavePhoneNumber(): Boolean {
        return userInfoRepository.userInfo?.value?.phone?.isNotEmpty() ?: false
    }

    //提款設置判斷權限, 判斷需不需要更新提現密碼 -> 個人資料是否完善
    suspend fun settingCheckPermissions() {

        val twoFactorStatus = checkTwoFactorStatus()
        val needUpdatePassWord = checkNeedUpdatePassWord()

        when {
            needUpdatePassWord -> {
                //是否需要顯示簡訊驗證 false: 需顯示簡訊驗證
                if (twoFactorStatus == false) {
                    sConfigData?.enterCertified = SecurityEnter.SETTING_PW
                    mShowSecurityDialog.value = Event(true)
                } else if (twoFactorStatus == true) {
                    //不需要簡訊驗證，提示需更新提款密碼
                    mNeedToUpdateWithdrawPassword.postValue(Event(needUpdatePassWord))
                }
            }
            else -> {
                checkSettingProfileInfoComplete()
            }
        }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    suspend fun checkProfileInfoComplete() {
        mNeedToCompleteProfileInfo.postValue(Event(verifyProfileInfoComplete()))
    }

    //提款設置用
    private suspend fun checkSettingProfileInfoComplete() {
        sConfigData?.enterCertified = SecurityEnter.SETTING_PROFILE_INFO
        verifyProfileInfoComplete().let { verify ->
            val twoFactorStatus = checkTwoFactorStatus()
            //資料完整準備進入提款設置頁面
            if (!verify) {
                if (twoFactorStatus == false) {
                    mShowSecurityDialog.value = Event(true)
                } else if (twoFactorStatus == true) {
                    //不需要簡訊驗證，提示需更新提款密碼
                    mSettingNeedToCompleteProfileInfo.postValue(Event(verify))
                }
            } else {
                //資料不完善通知跳轉個人資訊頁面
                mSettingNeedToCompleteProfileInfo.value = Event(verify)
            }
        }
    }

    //true:資料不完整 false：完整
    private suspend fun verifyProfileInfoComplete(): Boolean {
        val userInfo = userInfoFlow?.value
        var complete = false
        sConfigData?.apply {
            if (enableWithdrawFullName == FLAG_OPEN && userInfo?.fullName.isNullOrBlank() ||
                enableWithdrawQQ == FLAG_OPEN && userInfo?.qq.isNullOrBlank() ||
                enableWithdrawEmail == FLAG_OPEN && userInfo?.email.isNullOrBlank() ||
                enableWithdrawPhone == FLAG_OPEN && userInfo?.phone.isNullOrBlank() ||
                enableWithdrawWechat == FLAG_OPEN && userInfo?.wechat.isNullOrBlank()
            ) {
                complete = true
            }
        }
        return complete
    }

    suspend fun checkBankCardPermissions(): Response<BankMyResult> {
        val response = OneBoSportApi.bankService.getBankMy()
        if (response.isSuccessful) {
            response.body()?.let { result ->
                if (result.success) {
                    var promptMessageId: Int = -1
                    if (result.bankCardList.isNullOrEmpty()) {
                        promptMessageId = when {
                            mWithdrawOperation?.bankSystem ?: false && mWithdrawOperation?.cryptoSystem ?: false && mWithdrawOperation?.eWalletSystem ?: false -> {
                                R.string.please_setting_money_card
                            }
                            mWithdrawOperation?.bankSystem ?: false && mWithdrawOperation?.cryptoSystem ?: false && !(mWithdrawOperation?.eWalletSystem
                                ?: false) -> {
                                R.string.please_setting_bank_card_crypto
                            }
                            !(mWithdrawOperation?.bankSystem
                                ?: false) && mWithdrawOperation?.cryptoSystem ?: false && mWithdrawOperation?.eWalletSystem ?: false -> {
                                R.string.please_setting_crypto_ewallet
                            }
                            mWithdrawOperation?.bankSystem ?: false && !(mWithdrawOperation?.cryptoSystem
                                ?: false) && mWithdrawOperation?.eWalletSystem ?: false -> {
                                R.string.please_setting_bank_card_ewallet
                            }
                            mWithdrawOperation?.bankSystem ?: false && !(mWithdrawOperation?.cryptoSystem
                                ?: false) && !(mWithdrawOperation?.eWalletSystem ?: false) -> {
                                R.string.please_setting_bank_card
                            }
                            !(mWithdrawOperation?.bankSystem
                                ?: false) && mWithdrawOperation?.cryptoSystem ?: false && !(mWithdrawOperation?.eWalletSystem
                                ?: false) -> {
                                R.string.please_setting_crypto
                            }
                            else -> {
                                R.string.please_setting_ewallet
                            }
                        }
                    } else {
                        val bankBind = checkBankSystem(result.bankCardList)
                        val cryptoBind = checkCryptoSystem(result.bankCardList)
                        val eWalletBind = checkEWalletSystem(result.bankCardList)

                        val booleanMap: MutableMap<String, Boolean> = linkedMapOf()
                        if (mWithdrawOperation?.bankSystem == true) {
                            booleanMap[TransferType.BANK.type] = bankBind
                        }
                        if (mWithdrawOperation?.cryptoSystem == true) {
                            booleanMap[TransferType.CRYPTO.type] = cryptoBind
                        }
                        if (mWithdrawOperation?.eWalletSystem == true) {
                            booleanMap[TransferType.E_WALLET.type] = eWalletBind
                        }

                        promptMessageId = when {
                            booleanMap.containsValue(true) -> -1

                            booleanMap.containsKey(TransferType.BANK.type) && booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_money_card
                            }

                            booleanMap.containsKey(TransferType.BANK.type) && booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && !booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_bank_card_crypto
                            }

                            !booleanMap.containsKey(TransferType.BANK.type) && booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_crypto_ewallet
                            }

                            booleanMap.containsKey(TransferType.BANK.type) && !booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_bank_card_ewallet
                            }

                            booleanMap.containsKey(TransferType.BANK.type) && !booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && !booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_bank_card
                            }

                            !booleanMap.containsKey(TransferType.BANK.type) && booleanMap.containsKey(
                                TransferType.CRYPTO.type
                            ) && !booleanMap.containsKey(TransferType.E_WALLET.type) -> {
                                R.string.please_setting_crypto
                            }

                            else -> {
                                R.string.please_setting_ewallet
                            }
                        }
                    }

                    checkNeedToBindBankCard(promptMessageId)

                }
            }
        }
        return response
    }

    /**
     * 檢查是否需要跳轉置綁定銀行卡，若需要則先進行二次簡訊驗證
     */
    private suspend fun checkNeedToBindBankCard(promptMessageId: Int) {
        //promptMessageId不為-1時表示需要新增銀行卡
        val needBindBankCard = promptMessageId != -1
        if (needBindBankCard) {
            val twoFactorStatus = checkTwoFactorStatus()

            //是否需要顯示簡訊驗證 false: 需顯示簡訊驗證
            if (twoFactorStatus == false) {
                sConfigData?.enterCertified = SecurityEnter.BIND_BANK_CARD
                mShowSecurityDialog.value = Event(true)
            } else if (twoFactorStatus == true) {
                //不需要簡訊驗證，提示需先綁定銀行卡
                mNeedToBindBankCard.postValue(Event(promptMessageId))
            }
        } else {
            checkIntoWithdraw()
        }
    }

    private suspend fun checkIntoWithdraw() {
        val twoFactorStatus = checkTwoFactorStatus()
        if (twoFactorStatus == false) {
            sConfigData?.enterCertified = SecurityEnter.INTO_WITHDRAW
            mShowSecurityDialog.value = Event(true)
        } else {
            mIntoWithdraw.value = Event(true)
        }
    }

    private fun checkBankSystem(bankCardList: List<BankCardList>): Boolean {
        var hasBinding = false
        if (mWithdrawOperation?.bankSystem == true) {
            kotlin.run {
                bankCardList.forEach {
                    if (it.uwType == TransferType.BANK.type) {
                        hasBinding = true
                        return@run
                    }
                }
            }
        } else {
            return true
        }
        return hasBinding
    }

    private fun checkCryptoSystem(bankCardList: List<BankCardList>): Boolean {
        var hasBinding = false
        if (mWithdrawOperation?.cryptoSystem == true) {
            kotlin.run {
                bankCardList.forEach {
                    if (it.uwType == TransferType.CRYPTO.type) {
                        hasBinding = true
                        return@run
                    }
                }
            }
        } else {
            return true
        }
        return hasBinding
    }

    private fun checkEWalletSystem(bankCardList: List<BankCardList>): Boolean {
        var hasBinding = false
        if (mWithdrawOperation?.eWalletSystem == true) {
            kotlin.run {
                bankCardList.forEach {
                    if (it.uwType == TransferType.E_WALLET.type || it.uwType == TransferType.PAYMAYA.type) {
                        hasBinding = true
                        return@run
                    }
                }
            }
        } else {
            return true
        }
        return hasBinding
    }

}