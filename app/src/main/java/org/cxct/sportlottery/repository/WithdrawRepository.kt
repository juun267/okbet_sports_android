package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.util.Event
import retrofit2.Response

class WithdrawRepository(
    private val userInfoRepository: UserInfoRepository
) {

    private val userInfoFlow: Flow<UserInfo?>?
        get() = MultiLanguagesApplication.getInstance()?.userInfo

    private var _withdrawSystemOperation = MutableLiveData<Event<Boolean>>()
    val withdrawSystemOperation: LiveData<Event<Boolean>>
        get() = _withdrawSystemOperation

    private var _rechargeSystemOperation = MutableLiveData<Event<Boolean>>()
    val rechargeSystemOperation: LiveData<Event<Boolean>>
        get() = _rechargeSystemOperation

    private var _needToUpdateWithdrawPassword = MutableLiveData<Event<Boolean>>()
    val needToUpdateWithdrawPassword: LiveData<Event<Boolean>> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _needToUpdateWithdrawPassword

    private var _settingNeedToUpdateWithdrawPassword = MutableLiveData<Event<Boolean>>()
    val settingNeedToUpdateWithdrawPassword: LiveData<Event<Boolean>> //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _settingNeedToUpdateWithdrawPassword

    private var _needToCompleteProfileInfo = MutableLiveData<Event<Boolean>>()
    val needToCompleteProfileInfo: LiveData<Event<Boolean>> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = _needToCompleteProfileInfo

    private var _settingNeedToCompleteProfileInfo = MutableLiveData<Event<Boolean>>()
    val settingNeedToCompleteProfileInfo: LiveData<Event<Boolean>> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = _settingNeedToCompleteProfileInfo

    private var _needToBindBankCard = MutableLiveData<Event<Int?>>()
    val needToBindBankCard: LiveData<Event<Int?>>
        get() = _needToBindBankCard //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示

    private var mWithdrawOperation: SystemOperation? = null

    data class SystemOperation(
        val bankSystem: Boolean,
        val cryptoSystem: Boolean,
        val eWalletSystem: Boolean
    )

    //顯示驗證過了之後要繼續驗證前的邏輯
    suspend fun sendTwoFactor() {
        when (sConfigData?.enterCertified) {
            ProfileCenterViewModel.SecurityEnter.SETTING_PW.ordinal -> settingCheckPermissions()
            ProfileCenterViewModel.SecurityEnter.UPDATE_PW.ordinal -> withdrawCheckPermissions()
            ProfileCenterViewModel.SecurityEnter.COMPLETET_PROFILE_INFO.ordinal -> checkProfileInfoComplete()
            ProfileCenterViewModel.SecurityEnter.SETTING_PROFILE_INFO.ordinal -> checkSettingProfileInfoComplete()
            ProfileCenterViewModel.SecurityEnter.BIND_BANK_CARD.ordinal -> checkBankCardPermissions()
        }
    }

    suspend fun checkWithdrawSystem(): Response<MoneyRechCfgResult> {
        val response = OneBoSportApi.moneyService.getRechCfg()
        if (response.isSuccessful) {
            val withdrawConfig = response.body()?.rechCfg?.uwTypes

            val bankWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.BANK.type }?.open.toString() == FLAG_OPEN
            val cryptoWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.CRYPTO.type }?.open.toString() == FLAG_OPEN
            val eWalletWithdrawSystemOperation =
                withdrawConfig?.find { it.type == TransferType.E_WALLET.type }?.open.toString() == FLAG_OPEN

            mWithdrawOperation = SystemOperation(
                bankWithdrawSystemOperation,
                cryptoWithdrawSystemOperation,
                eWalletWithdrawSystemOperation
            )

            val operation =
                bankWithdrawSystemOperation || cryptoWithdrawSystemOperation || eWalletWithdrawSystemOperation
            _withdrawSystemOperation.value = Event(operation)

            if (operation) {
                withdrawCheckPermissions()
            }
        }
        return response
    }

    suspend fun checkRechargeSystem(): Response<MoneyRechCfgResult> {
        val response = OneBoSportApi.moneyService.getRechCfg()
        if (response.isSuccessful) {
            val rechTypesList = response.body()?.rechCfg?.rechTypes //玩家層級擁有的充值方式
            val rechCfgsList = response.body()?.rechCfg?.rechCfgs  //後台有開的充值方式
            val operation = (rechTypesList?.size ?: 0 > 0) && (rechCfgsList?.size ?: 0 > 0)

            _rechargeSystemOperation.value = Event(operation)
        }
        return response
    }

    private suspend fun checkNeedUpdatePassWord(): Boolean {
        if (userInfoFlow == null)
            userInfoRepository.getUserInfo()
        return userInfoFlow?.firstOrNull()?.updatePayPw == 1
    }

    //提款判斷權限
    private suspend fun withdrawCheckPermissions() {
        this.checkNeedUpdatePassWord().let {
            if((sConfigData?.twoFactorVerified == "1" && sConfigData?.hasCertified == false) && (it || !verifyProfileInfoComplete())){
                sConfigData?.enterCertified = ProfileCenterViewModel.SecurityEnter.UPDATE_PW.ordinal
                _needToSendTwoFactor.value = Event(true)
            }else
            _needToUpdateWithdrawPassword.value = Event(it)
        }
    }

    //提款設置判斷權限, 判斷需不需要更新提現密碼 -> 個人資料是否完善
    suspend fun settingCheckPermissions() {
        this.checkNeedUpdatePassWord().let {
            if (it) {
                _settingNeedToUpdateWithdrawPassword.value = Event(it)
            } else {
                checkSettingProfileInfoComplete()
            }
        }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    suspend fun checkProfileInfoComplete() {
        _needToCompleteProfileInfo.value = Event(verifyProfileInfoComplete())
    }

    //提款設置用
    private suspend fun checkSettingProfileInfoComplete() {
        verifyProfileInfoComplete().let { verify ->
            /*if (verify) {
                _settingNeedToCompleteProfileInfo.value = Event(verify)
            } else {

            }*/
            _settingNeedToCompleteProfileInfo.value = Event(verify)
        }
    }

    private suspend fun verifyProfileInfoComplete(): Boolean {
        val userInfo = userInfoFlow?.firstOrNull()
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
                    _needToBindBankCard.value = Event(promptMessageId)
                }
            }
        }
        return response
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
                    if (it.uwType == TransferType.E_WALLET.type) {
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