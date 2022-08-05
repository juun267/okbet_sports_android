package org.cxct.sportlottery.ui.withdraw

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteRequest
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.money.config.*
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil
import java.math.RoundingMode
import kotlin.math.min


class WithdrawViewModel(
    androidContext: Application,
    private val moneyRepository: MoneyRepository,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val bankCardList: LiveData<List<BankCardList>>
        get() = _bankCardList
    private var _bankCardList = MutableLiveData<List<BankCardList>>()

    data class MyMoneyCard(val cardList: List<BankCardList>, val transferType: TransferType)

    val moneyCardList: LiveData<MyMoneyCard>
        get() = _moneyCardList
    private var _moneyCardList = MutableLiveData<MyMoneyCard>()

    val bankAddResult: LiveData<BankAddResult>
        get() = _bankAddResult
    private var _bankAddResult = MutableLiveData<BankAddResult>()

    val bankDeleteResult: LiveData<BankDeleteResult>
        get() = _bankDeleteResult
    private var _bankDeleteResult = MutableLiveData<BankDeleteResult>()

    val withdrawAddResult: LiveData<WithdrawAddResult>
        get() = _withdrawAddResult
    private var _withdrawAddResult = MutableLiveData<WithdrawAddResult>()

    //獲取資金設定
    val rechargeConfigs: LiveData<MoneyRechCfgData>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData>()

    //--銀行卡編輯頁面
    //開戶名錯誤訊息
    val createNameErrorMsg: LiveData<String>
        get() = _createNameErrorMsg
    private var _createNameErrorMsg = MutableLiveData<String>()

    //銀行卡號錯誤訊息
    val bankCardNumberMsg: LiveData<String>
        get() = _bankCardNumberMsg
    private var _bankCardNumberMsg = MutableLiveData<String>()

    //開戶網點錯誤訊息
    val networkPointMsg: LiveData<String>
        get() = _networkPointMsg
    private var _networkPointMsg = MutableLiveData<String>()

    //電話號碼錯誤訊息
    val phoneNumberMsg: LiveData<String>
        get() = _phoneNumberMsg
    private var _phoneNumberMsg = MutableLiveData<String>()

    //提款密碼錯誤訊息
    val withdrawPasswordMsg: LiveData<String>
        get() = _withdrawPasswordMsg
    private var _withdrawPasswordMsg = MutableLiveData<String>()

    //提款预约日期錯誤訊息
    val WithdrawAppointmentMsg: LiveData<String>
        get() = _withdrawAppointmentMsg
    private var _withdrawAppointmentMsg = MutableLiveData<String>()


    //提款金額錯誤訊息
    val withdrawAmountMsg: LiveData<String>
        get() = _withdrawAmountMsg
    private var _withdrawAmountMsg = MutableLiveData<String>()

    //提款手續費(銀行卡)/匯率(虛擬幣)提示
    val withdrawRateHint: LiveData<String>
        get() = _withdrawRateHint
    private var _withdrawRateHint = MutableLiveData<String>()

    //提款虛擬幣所需餘額提示
    val withdrawCryptoAmountHint: LiveData<String>
        get() = _withdrawCryptoAmountHint
    private var _withdrawCryptoAmountHint = MutableLiveData<String>()

    //提款虛擬幣花費手續費
    val withdrawCryptoFeeHint: LiveData<String>
        get() = _withdrawCryptoFeeHint
    private var _withdrawCryptoFeeHint = MutableLiveData<String>()

    //提款手續費提示
    val walletAddressMsg: LiveData<String>
        get() = _walletAddressMsg
    private var _walletAddressMsg = MutableLiveData<String>()

    //提款总计
    val withdrawAmountTotal: LiveData<String>
        get() = _withdrawAmountTotal
    private var _withdrawAmountTotal = MutableLiveData<String>()

    //提款金額提示
    val withdrawAmountHint: LiveData<String>
        get() = _withdrawAmountHint
    private var _withdrawAmountHint = MutableLiveData<String>()

    //所有的提款卡
    var myWithdrawCardList: List<BankCardList>? = null

    //資金卡片是否可以繼續增加(銀行卡、虛擬幣)
    val addMoneyCardSwitch: LiveData<TransferTypeAddSwitch>
        get() = _addMoneyCardSwitch
    private var _addMoneyCardSwitch = MutableLiveData<TransferTypeAddSwitch>()

    data class MoneyCardExist(val transferType: TransferType, val exist: Boolean)

    //資金卡片是否已添加(存在)
    val moneyCardExist: LiveData<Set<MoneyCardExist>>
        get() = _moneyCardExist
    private var _moneyCardExist = MutableLiveData<Set<MoneyCardExist>>()

    //可被增加的虛擬幣卡列表
    val addCryptoCardList: LiveData<List<Detail>>
        get() = _addCryptoCardList
    private var _addCryptoCardList = MutableLiveData<List<Detail>>()

    //判斷Tab是不是要顯示
    private var _withdrawTabIsShow = MutableLiveData<List<String>>()
    val withdrawTabIsShow: LiveData<List<String>>
        get() = _withdrawTabIsShow

    private var _needCheck = MutableLiveData<Boolean>()
    val needCheck: LiveData<Boolean>
        get() = _needCheck

    private var _commissionCheckList = MutableLiveData<List<CheckList>>()
    val commissionCheckList: LiveData<List<CheckList>>
        get() = _commissionCheckList

    private var _deductMoney = MutableLiveData<Double>()
    val deductMoney: LiveData<Double>
        get() = _deductMoney

    private val _bettingStationList = MutableLiveData<List<BettingStation>>()
    val bettingStationList: LiveData<List<BettingStation>>
        get() = _bettingStationList

    private val _areaList = MutableLiveData<AreaAll>()
    val areaList: LiveData<AreaAll>
        get() = _areaList

    private var uwBankType: UwType? = null

    //資金卡片config
    private var cardConfig: Detail? = null

    private var dealType: TransferType = TransferType.BANK


    /**
     * @param isBalanceMax: 是否為當前餘額作為提款上限, true: 提示字為超過餘額相關, false: 提示字為金額設定相關
     */
    data class WithdrawAmountLimit(val min: Double, val max: Double, val isBalanceMax: Boolean)

    fun setDealType(type: TransferType) {
        dealType = type
        transferTypeMoneyCardList()
    }

    fun addWithdraw(
        withdrawCard: BankCardList?,
        applyMoney: String,
        withdrawPwd: String,
        bettingStationId: Int?,
        appointmentDate: String?,
        appointmentHour: String?
    ) {
        checkWithdrawAmount(withdrawCard, applyMoney)
        checkWithdrawPassword(withdrawPwd)
        if (bettingStationId != null) {
            checkWithdrawAppointment(appointmentDate ?: "", appointmentHour ?: "")
        }
        if (checkWithdrawData()) {
            loading()
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.withdrawService.addWithdraw(
                        getWithdrawAddRequest(
                            withdrawCard?.id?.toLong() ?: 0,
                            applyMoney,
                            withdrawPwd,
                            bettingStationId,
                            appointmentDate,
                            appointmentHour
                        )
                    )
                }?.let { result ->
                    _withdrawAddResult.value = result
                    hideLoading()
                }
            }
        }

    }

    private fun getWithdrawAddRequest(
        bankCardId: Long,
        applyMoney: String,
        withdrawPwd: String,
        bettingStationId: Int?,
        appointmentDate: String?,
        appointmentHour: String?
    ): WithdrawAddRequest {
        return WithdrawAddRequest(
            id = bankCardId,
            applyMoney = applyMoney.toDouble(),
            withdrawPwd = MD5Util.MD5Encode(withdrawPwd),
            bettingStationId = bettingStationId,
            appointmentDate = appointmentDate,
            appointmentHour = appointmentHour
        )
    }

    private fun checkWithdrawData(): Boolean {
        if (withdrawAmountMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        if (WithdrawAppointmentMsg.value != "")
            return false
        return true
    }

    fun getBankCardList() {
        viewModelScope.launch {
            loading()
            doNetwork(androidContext) {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result ->
                val cardList = mutableListOf<BankCardList>()
                result.bankCardList?.forEach { bankCard ->
                    cardList.add(bankCard.apply {
                        transferType = TransferType.values().find { it.type == bankCard.uwType }
                            ?: TransferType.BANK
                    })
                }
                _bankCardList.value = cardList
                hideLoading()
            }
        }
    }

    fun getUwCheck() {
        viewModelScope.launch {
            loading()
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.getWithdrawUwCheck()
            }?.let { result ->
                _needCheck.postValue(result.t?.needCheck ?: false)
                _deductMoney.postValue(result.t?.total?.deductMoney ?: 0.0)
                _commissionCheckList.postValue(result.t?.checkList ?: listOf())
                hideLoading()
            }
        }
    }

    private fun getWithdrawCardList() {
        viewModelScope.launch {
            loading()
            doNetwork(androidContext) {
                OneBoSportApi.bankService.getBankMy()
            }?.let { result ->
                myWithdrawCardList = result.bankCardList
                checkTransferTypeExistence(result)
                hideLoading()
            }
        }
    }

    private fun transferTypeMoneyCardList() {
        val cardList = mutableListOf<BankCardList>()
        myWithdrawCardList?.forEach { bankCard ->
            if (dealType.type == bankCard.uwType)
                cardList.add(bankCard.apply { transferType = dealType })
        }
        _moneyCardList.value = MyMoneyCard(cardList, dealType)
        getWithdrawRate(cardList.firstOrNull())
        getWithdrawHint()
    }

    fun addBankCard(
        bankName: String,
        subAddress: String? = null,
        cardNo: String,
        fundPwd: String,
        id: String?,
        uwType: String,
        bankCode: String? = null
    ) {
        if (checkInputBankCardData(userInfo.value?.fullName, cardNo, subAddress, fundPwd, uwType)) {
            viewModelScope.launch {
                loading()
                doNetwork(androidContext) {
                    val userId = userInfoRepository.userInfo?.value?.userId.toString()
                    OneBoSportApi.bankService.bankAdd(
                        createBankAddRequest(
                            bankName,
                            subAddress,
                            cardNo,
                            fundPwd,
                            userInfo.value?.fullName,
                            id,
                            userId,
                            uwType,
                            bankCode
                        )
                    )
                }?.let { result ->
                    _bankAddResult.value = result
                    hideLoading()
                }
            }
        }
    }

    private fun checkInputBankCardData(
        fullName: String?,
        cardNo: String,
        subAddress: String?,
        withdrawPassword: String,
        uwType: String
    ): Boolean {
        return when (uwType) {
            TransferType.BANK.type -> {
                checkCreateName(fullName ?: "")
                checkBankCardNumber(cardNo)
                checkNetWorkPoint(subAddress ?: "")
                checkWithdrawPassword(withdrawPassword)
                checkBankCardData()
            }
            TransferType.CRYPTO.type -> {
                checkWalletAddress(cardNo)
                checkWithdrawPassword(withdrawPassword)
                checkCryptoCardData()
            }
            TransferType.E_WALLET.type -> {
                checkCreateName(fullName ?: "")
                checkPhoneNumber(cardNo)
                checkWithdrawPassword(withdrawPassword)
                checkEWalletCardData()
            }
            else -> false
        }
    }

    private fun createBankAddRequest(
        bankName: String,
        subAddress: String?,
        cardNo: String,
        fundPwd: String,
        fullName: String?,
        id: String?,
        userId: String?,
        uwType: String,
        bankCode: String?
    ): BankAddRequest {
        return BankAddRequest(
            bankName = bankName,
            subAddress = subAddress,
            cardNo = cardNo,
            fundPwd = MD5Util.MD5Encode(fundPwd),
            fullName = fullName,
            id = id,
            userId = userId,
            uwType = uwType,
            bankCode = bankCode
        )
    }

    fun deleteBankCard(id: Long, fundPwd: String) {
        checkInputBankCardDeleteData(fundPwd)
        if (checkBankCardDeleteData()) {
            loading()
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.bankService.bankDelete(
                        createBankDeleteRequest(
                            id,
                            MD5Util.MD5Encode(fundPwd)
                        )
                    )
                }?.let { result ->
                    _bankDeleteResult.value = result
                    hideLoading()
                }
            }
        }
    }

    private fun checkInputBankCardDeleteData(fundPwd: String) {
        checkWithdrawPassword(fundPwd)
    }

    private fun createBankDeleteRequest(id: Long, fundPwd: String): BankDeleteRequest {
        return BankDeleteRequest(id = id, fundPwd = fundPwd)
    }

    fun getMoneyConfigs() {
        viewModelScope.launch {
            loading()
            getMoney()
            doNetwork(androidContext) {
                moneyRepository.getRechCfg()
            }?.let { result ->
                result.rechCfg?.let { moneyRechCfgData ->
                    uwBankType =
                        moneyRechCfgData.uwTypes.firstOrNull { config -> config.type == TransferType.BANK.type }
                    _rechargeConfigs.value = moneyRechCfgData
                    getWithdrawCardList()
                    //判斷Tab要不要顯示
                    val withdrawConfig = moneyRechCfgData.uwTypes
                    val tabList: ArrayList<String> = arrayListOf()
                    if (withdrawConfig.find { it.type == TransferType.CRYPTO.type }?.open.toString() == FLAG_OPEN) tabList.add(
                        TransferType.CRYPTO.type
                    )
                    if (withdrawConfig.find { it.type == TransferType.BANK.type }?.open.toString() == FLAG_OPEN) tabList.add(
                        TransferType.BANK.type
                    )
                    if (withdrawConfig.find { it.type == TransferType.E_WALLET.type }?.open.toString() == FLAG_OPEN) tabList.add(
                        TransferType.E_WALLET.type
                    )
                    if (withdrawConfig.find { it.type == TransferType.STATION.type }?.open.toString() == FLAG_OPEN) tabList.add(
                        TransferType.STATION.type
                    )
                    _withdrawTabIsShow.postValue(tabList)

                    checkBankCardCount()
                }
            }
        }
    }

    fun clearBankCardFragmentStatus() {
        //若不清除下一次進入編輯銀行卡頁面時會直接觸發觀察判定編輯成功
        _bankDeleteResult = MutableLiveData()
        _bankAddResult = MutableLiveData()
        _createNameErrorMsg = MutableLiveData()
        _bankCardNumberMsg = MutableLiveData()
        _networkPointMsg = MutableLiveData()
        _withdrawPasswordMsg = MutableLiveData()
        _walletAddressMsg = MutableLiveData()
    }

    private fun checkBankCardData(): Boolean {
        if (createNameErrorMsg.value != "")
            return false
        if (bankCardNumberMsg.value != "")
            return false
        if (networkPointMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    private fun checkCryptoCardData(): Boolean {
        if (walletAddressMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    private fun checkEWalletCardData(): Boolean {
        if (createNameErrorMsg.value != "")
            return false
        if (phoneNumberMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    private fun checkBankCardDeleteData(): Boolean {
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    fun checkCreateName(createName: String) {
        _createNameErrorMsg.value = when {
            userInfo.value?.fullName.isNullOrEmpty() and !VerifyConstUtil.verifyFullName(createName) -> LocalUtils.getString(
                R.string.error_input_has_blank
            )
            else -> ""
        }
    }

    fun checkBankCardNumber(bankCardNumber: String) {
        _bankCardNumberMsg.value = when {
            bankCardNumber.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> ""
        }
    }

    fun checkNetWorkPoint(networkPoint: String) {
        _networkPointMsg.value = when {
            networkPoint.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> ""
        }
    }

    fun checkPhoneNumber(phoneNumber: String) {
        _phoneNumberMsg.value = when {
            phoneNumber.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            else -> ""
        }
    }

    fun checkWalletAddress(walletAddress: String) {
        _walletAddressMsg.value = when {
            walletAddress.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyCryptoWalletAddress(walletAddress) -> LocalUtils.getString(R.string.error_wallet_address)
            else -> ""
        }
    }

    fun checkWithdrawPassword(withdrawPassword: String) {
        _withdrawPasswordMsg.value = when {
            withdrawPassword.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWithdrawPassword(withdrawPassword) -> {
                LocalUtils.getString(R.string.error_withdraw_password)
            }
            else -> ""
        }
    }

    fun checkWithdrawAppointment(appointmentDate: String, appointmentHour: String) {
        if (appointmentDate.isNullOrBlank()) {
            _withdrawAppointmentMsg.value = LocalUtils.getString(R.string.select_date)
        } else if (appointmentHour.isNullOrBlank()) {
            _withdrawAppointmentMsg.value = LocalUtils.getString(R.string.select_time)
        } else {
            _withdrawAppointmentMsg.value = ""
        }
    }

    fun checkWithdrawAmount(withdrawCard: BankCardList?, inputAmount: String) {
        var withdrawAmount = inputAmount
        val amountLimit = getWithdrawAmountLimit()
        _withdrawAmountMsg.value = when {
            withdrawAmount.isEmpty() -> {
                withdrawAmount = "0"
                LocalUtils.getString(R.string.error_input_empty)
            }
            amountLimit.isBalanceMax && withdrawAmount.toDouble() > getWithdrawAmountLimit().max -> {
                LocalUtils.getString(R.string.error_withdraw_amount_bigger_than_balance)
            }
            withdrawAmount.toDoubleOrNull() == null || withdrawAmount.toDouble().equals(0) -> {
                LocalUtils.getString(R.string.error_withdraw_amount_bigger_than_balance)
            }
            VerifyConstUtil.verifyWithdrawAmount(
                withdrawAmount,
                amountLimit.min,
                amountLimit.max
            ) != 0 -> {
                LocalUtils.getString(R.string.error_amount_limit_exceeded)
            }
            else -> ""
        }
        if (dealType != TransferType.STATION) {
            getWithdrawRate(withdrawCard, withdrawAmount.toDouble())
        }
    }


    fun getWithdrawHint() {
        val limit = getWithdrawAmountLimit()
        _withdrawAmountHint.value = String.format(
            LocalUtils.getString(R.string.edt_hint_deposit_money),
            sConfigData?.systemCurrencySign,
            limit.min.toLong(), limit.max.toLong()
        )
    }

    fun getWithdrawAmountLimit(): WithdrawAmountLimit {
        //{"countLimit":5,"contract":"RMB","currency":"RMB","exchangeRate":1,"feeVal":0,"feeRate":0,"minWithdrawMoney":1,"maxWithdrawMoney":10000}
//        {"contract":"RMB","countLimit":5,"currency":"RMB","exchangeRate":1.0,"feeRate":0.0,"feeVal":0.0,"maxWithdrawMoney":10000.0,"minWithdrawMoney":1.0}
        //用戶可提取最小金額
        val minLimit = cardConfig?.minWithdrawMoney ?: 0.0
        //提取金額不得超過 餘額-手續費
        val balanceMaxLimit = getBalanceMaxLimit()
        //是否使用餘額作為提取上限
        val isBalanceMax: Boolean
        //用戶可提取最大金額
        val configMaxLimit: Double?
        if ((cardConfig?.maxWithdrawMoney == 0.0) || (cardConfig?.maxWithdrawMoney == null)) {
            configMaxLimit = null
            isBalanceMax = true
        } else {
            configMaxLimit = cardConfig?.maxWithdrawMoney!!
            isBalanceMax = balanceMaxLimit < configMaxLimit
        }
        val maxLimit =
            if (configMaxLimit == null) balanceMaxLimit else min(balanceMaxLimit, configMaxLimit)
        return WithdrawAmountLimit(minLimit, maxLimit, isBalanceMax)
    }

    private fun getBalanceMaxLimit(): Double {
        return when (dealType) {
            TransferType.BANK -> ArithUtil.div(
                (userMoney.value ?: 0.0),
                ((cardConfig?.feeRate?.plus(1) ?: 1.0)),
                0,
                RoundingMode.FLOOR
            )
            TransferType.CRYPTO -> ArithUtil.div(
                ArithUtil.minus(
                    (userMoney.value),
                    (cardConfig?.feeVal?.times(cardConfig?.exchangeRate ?: 0.0))
                ),
                cardConfig?.exchangeRate ?: 1.0, 3, RoundingMode.FLOOR
            )
            TransferType.E_WALLET -> ArithUtil.div(
                (userMoney.value ?: 0.0),
                ((cardConfig?.feeRate?.plus(1) ?: 1.0)),
                0,
                RoundingMode.FLOOR
            )
            TransferType.STATION -> ArithUtil.div(
                (userMoney.value ?: 0.0),
                ((cardConfig?.feeRate?.plus(1) ?: 1.0)),
                0,
                RoundingMode.FLOOR
            )
        }
    }

    fun getWithdrawRate(withdrawCard: BankCardList?, withdrawAmount: Double? = 0.0) {
        when (dealType) {
            TransferType.BANK, TransferType.E_WALLET -> {
                _withdrawCryptoAmountHint.value = ""
                _withdrawCryptoFeeHint.value = ""
                _withdrawRateHint.value = String.format(
                    LocalUtils.getString(R.string.withdraw_handling_fee_hint),
                    ArithUtil.toMoneyFormat(cardConfig?.feeRate?.times(100)), sConfigData?.systemCurrencySign,
                    ArithUtil.toMoneyFormat((cardConfig?.feeRate)?.times(withdrawAmount ?: 0.0))
                )

                _withdrawAmountTotal.value = "0.000"





                if (withdrawAmount != null && withdrawAmount.toDouble() > 0) {
                    _withdrawAmountTotal.value =  ArithUtil.toMoneyFormat(
                        ArithUtil.add(
                            withdrawAmount.toDouble(),
                            ArithUtil.toMoneyFormat(
                                (cardConfig?.feeRate)?.times(
                                    withdrawAmount ?: 0.0
                                )
                            ).toDouble()
                        )
                    )


                }
            }
            TransferType.CRYPTO -> {
                withdrawCard?.let {
                    val fee = cardConfig?.let { it.exchangeRate?.times(it.feeVal ?: 0.0) } ?: 0.0
                    val withdrawNeedAmount =
                        if (withdrawAmount != 0.0 && withdrawAmount != null) cardConfig?.let {
                            withdrawAmount.times(it.exchangeRate ?: 0.0)
                        } ?: 0.0 else 0.0
                    _withdrawCryptoAmountHint.value = String.format(
                        LocalUtils.getString(R.string.withdraw_crypto_amount_hint),
                        sConfigData?.systemCurrencySign,
                        ArithUtil.toMoneyFormat(withdrawNeedAmount)

                    )
                    _withdrawAmountTotal.value =  "0.000"


                    _withdrawCryptoFeeHint.value = String.format(
                        LocalUtils.getString(R.string.withdraw_crypto_fee_hint),
                        sConfigData?.systemCurrencySign,
                        ArithUtil.toMoneyFormat(fee)

                    )
                    _withdrawRateHint.value = String.format(
                        LocalUtils.getString(R.string.hint_rate),
                        ArithUtil.toMoneyFormat(cardConfig?.exchangeRate)
                    )
                    if (withdrawAmount != null && withdrawAmount.toDouble() > 0) {
                        _withdrawAmountTotal.value =ArithUtil.toMoneyFormat(
                            (ArithUtil.add(
                                withdrawAmount.toDouble(),
                                ArithUtil.div(fee, cardConfig?.exchangeRate!!.toDouble())
                            )
                                    )

                        )


                    }

                }
            }
            TransferType.STATION -> {
            }
        }
    }

    fun setupWithdrawCard(withdrawCard: BankCardList) {
        getWithdrawCardConfig(withdrawCard)
        getWithdrawRate(withdrawCard)
        getWithdrawHint()
    }

    private fun getWithdrawCardConfig(withdrawCard: BankCardList) {
        cardConfig = when (dealType) {
            TransferType.BANK -> {
                rechargeConfigs.value?.uwTypes?.find { config -> config.type == TransferType.BANK.type }?.detailList?.first()
            }
            TransferType.CRYPTO -> {
                rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.find { it.contract == withdrawCard.bankName }
            }
            TransferType.E_WALLET -> {
                rechargeConfigs.value?.uwTypes?.find { config -> config.type == TransferType.E_WALLET.type }?.detailList?.first()
            }
            TransferType.STATION -> {
                rechargeConfigs.value?.uwTypes?.find { config -> config.type == TransferType.STATION.type }?.detailList?.first()
            }
        }
    }

    private fun checkTransferTypeExistence(result: BankMyResult) {
        val bankCardExistence =
            result.bankCardList?.any { card -> card.uwType == TransferType.BANK.type } == true
        val bankWithdrawSwitch =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code

        val cryptoCardExistence =
            result.bankCardList?.any { card -> card.uwType == TransferType.CRYPTO.type } == true
        val cryptoWithdrawSwitch =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code

        val eWalletCardExistence =
            result.bankCardList?.any { card -> card.uwType == TransferType.E_WALLET.type } == true
        val eWalletWithdrawSwitch =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.E_WALLET.type }?.open == MoneyRechCfg.Switch.ON.code

        val bankCardExist = bankCardExistence && bankWithdrawSwitch
        val cryptoCardExist = cryptoCardExistence && cryptoWithdrawSwitch
        val eWalletCardExist = eWalletCardExistence && eWalletWithdrawSwitch

        val moneyCardExistSet = mutableSetOf<MoneyCardExist>().apply {
            add(MoneyCardExist(TransferType.BANK, bankCardExist))
            add(MoneyCardExist(TransferType.CRYPTO, cryptoCardExist))
            add(MoneyCardExist(TransferType.E_WALLET, eWalletCardExist))
        }

        _moneyCardExist.value = moneyCardExistSet
    }

    /**
     * 判斷當前銀行卡數量是否超出銀行卡綁定上限
     */
    fun checkBankCardCount() {
        var showAddCryptoCard = false //是否顯示虛擬幣
        val showAddBankCard: Boolean // 是否顯示銀行卡
        val showAddEWalletCard: Boolean // 是否顯示eWallet

        //虛擬幣是否可以被提款或新增卡片
        val cryptoOpen =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code
        val cryptoCardLimitList = checkCryptoCanBind()
        run breaking@{
            if (!cryptoOpen) {
                showAddCryptoCard = false
                return@breaking
            }
            cryptoCardLimitList.forEach {
                if (it.canBind == true) {
                    showAddCryptoCard = true
                    return@breaking
                } else {
                    showAddCryptoCard = false
                }
            }
        }

        //銀行卡是否可以被提款或新增卡片
        val bankCardCountLimit = uwBankType?.detailList?.first()?.countLimit
        val bankCardCount = bankCardList.value?.count { it.transferType == TransferType.BANK }
        val bankOpen =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code
        showAddBankCard = when {
            !bankOpen -> false
            bankCardCountLimit == null -> true
            bankCardCount == null -> true
            else -> bankCardCount < bankCardCountLimit
        }

        //E-wallet是否可以被提款或新增卡片
        val eWalletOpen =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.E_WALLET.type }?.open == MoneyRechCfg.Switch.ON.code
        val eWalletCardCountLimit =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.E_WALLET.type }?.detailList?.first()?.countLimit
        val eWalletCardCount =
            bankCardList.value?.count { it.transferType == TransferType.E_WALLET }
        showAddEWalletCard = when {
            !eWalletOpen -> false
            eWalletCardCountLimit == null -> true
            eWalletCardCount == null -> true
            else -> eWalletCardCount < eWalletCardCountLimit
        }

        _addMoneyCardSwitch.value =
            TransferTypeAddSwitch(showAddBankCard, showAddCryptoCard, showAddEWalletCard)
    }

    data class CryptoCardCountLimit(
        val channel: String,
        var count: Int,
        var canBind: Boolean? = null
    )

    /**
     * @return 各虛擬幣提款渠道的名稱、數量、是否可再被添加(channel, count, canBind)
     */
    private fun checkCryptoCanBind(): List<CryptoCardCountLimit> {
        val cryptoCardCountLimitList = mutableListOf<CryptoCardCountLimit>()
        val cryptoCardList = bankCardList.value?.filter { it.transferType == TransferType.CRYPTO }

        rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.forEach { configDetail ->
            if (cryptoCardCountLimitList.none { it.channel == configDetail.contract }) {
                configDetail.contract?.let { channelName ->
                    cryptoCardCountLimitList.add(CryptoCardCountLimit(channelName, 0))
                }
            }
        }

        cryptoCardList?.forEach { card ->
            if (cryptoCardCountLimitList.any { it.channel == card.bankName }) {
                cryptoCardCountLimitList.first { it.channel == card.bankName }.count += 1
            }
        }

        //判斷擁有的該渠道卡片小於限制數量
        cryptoCardCountLimitList.forEach { cryptoCardCountLimit ->
            val configLimit =
                rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.find { it.contract == cryptoCardCountLimit.channel }?.countLimit
            cryptoCardCountLimit.canBind = if (configLimit == null)
                true
            else {
                cryptoCardCountLimit.count < configLimit
            }
        }
        return cryptoCardCountLimitList
    }

    /**
     * 獲取虛擬幣新增或編輯銀行卡可選列表
     */
    fun getCryptoBindList(modifyMoneyCard: BankCardList? = null) {
        val cryptoCanBind = checkCryptoCanBind()
        val modifyMoneyChannel = modifyMoneyCard?.bankName

        val cryptoCanBindList =
            rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.toMutableList()
                ?: mutableListOf()

        cryptoCanBind.forEach { canBindData ->
            if (canBindData.canBind == false) {
                if (modifyMoneyChannel != canBindData.channel) {
                    val removeData =
                        rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.find { it.contract == canBindData.channel }
                    cryptoCanBindList.remove(removeData)
                }
            }
        }
        _addCryptoCardList.value = cryptoCanBindList
    }

    fun resetWithdrawPage() {
        _withdrawAmountMsg.value = ""
        _withdrawPasswordMsg.value = ""
        _withdrawAppointmentMsg.value = ""
        _withdrawAddResult = MutableLiveData<WithdrawAddResult>()
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }

    fun bettingStationQuery(platformId: Long, countryId: Int, provinceId: Int, cityId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.bettingStationsQueryUwStation(
                    platformId,
                    countryId,
                    provinceId,
                    cityId
                )
            }?.let { result ->
                val bettingStationSheetList = mutableListOf<StatusSheetData>()
                withContext(Dispatchers.Main) {
                    _bettingStationList.value = result.bettingStationList
                }
            }
        }
    }

    fun queryArea() {
        viewModelScope.launch(Dispatchers.IO) {
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.areaAll()
            }?.let { result ->
                withContext(Dispatchers.Main) {
                    _areaList.value = result.areaAll
                }
            }
        }
    }
}