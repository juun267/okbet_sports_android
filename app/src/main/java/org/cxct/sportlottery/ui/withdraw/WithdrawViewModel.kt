package org.cxct.sportlottery.ui.withdraw

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteRequest
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.MoneyRechCfgData
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.network.money.config.Detail
import org.cxct.sportlottery.network.money.config.UwType
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil
import java.math.RoundingMode
import kotlin.math.min

class WithdrawViewModel(
    private val androidContext: Context,
    private val moneyRepository: MoneyRepository,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository, infoCenterRepository) {


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> //使用者餘額
        get() = _loading

    val userInfo = userInfoRepository.userInfo.asLiveData()

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

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

    //提款密碼錯誤訊息
    val withdrawPasswordMsg: LiveData<String>
        get() = _withdrawPasswordMsg
    private var _withdrawPasswordMsg = MutableLiveData<String>()

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

    //提款金額提示
    val withdrawAmountHint: LiveData<String>
        get() = _withdrawAmountHint
    private var _withdrawAmountHint = MutableLiveData<String>()

    //所有的提款卡
    private var myWithdrawCardList: List<BankCardList>? = null

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
    private var _withdrawTabIsShow = MutableLiveData<Event<Boolean>>()
    val withdrawSystemOperation: LiveData<Event<Boolean>>
        get() = _withdrawTabIsShow

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

    fun addWithdraw(withdrawCard: BankCardList?, applyMoney: String, withdrawPwd: String) {
        checkWithdrawAmount(withdrawCard, applyMoney)
        checkWithdrawPassword(withdrawPwd)
        if (checkWithdrawData()) {
            loading()
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.withdrawService.addWithdraw(getWithdrawAddRequest(withdrawCard?.id?.toLong() ?: 0, applyMoney, withdrawPwd))
                }?.let { result ->
                    _withdrawAddResult.value = result
                    hideLoading()
                }
            }
        }

    }

    private fun getWithdrawAddRequest(bankCardId: Long, applyMoney: String, withdrawPwd: String): WithdrawAddRequest {
        return WithdrawAddRequest(
            id = bankCardId,
            applyMoney = applyMoney.toDouble(),
            withdrawPwd = MD5Util.MD5Encode(withdrawPwd)
        )
    }

    private fun checkWithdrawData(): Boolean {
        if (withdrawAmountMsg.value != "")
            return false
        if (withdrawPasswordMsg.value != "")
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
                    cardList.add(bankCard.apply { transferType = TransferType.values().find { it.type == bankCard.uwType } ?: TransferType.BANK })
                }
                _bankCardList.value = cardList
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

    fun addBankCard(bankName: String, subAddress: String? = null, cardNo: String, fundPwd: String, id: String?, uwType: String, bankCode: String? = null) {
        if (checkInputBankCardData(userInfo.value?.fullName, cardNo, subAddress, fundPwd, uwType)) {
            viewModelScope.launch {
                loading()
                doNetwork(androidContext) {
                    val userId = userInfoRepository.userInfo.firstOrNull()?.userId.toString()
                    OneBoSportApi.bankService.bankAdd(createBankAddRequest(bankName, subAddress, cardNo, fundPwd, userInfo.value?.fullName, id, userId, uwType, bankCode))
                }?.let { result ->
                    _bankAddResult.value = result
                    hideLoading()
                }
            }
        }
    }

    private fun checkInputBankCardData(fullName: String?, cardNo: String, subAddress: String?, withdrawPassword: String, uwType: String): Boolean {
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
                    OneBoSportApi.bankService.bankDelete(createBankDeleteRequest(id, MD5Util.MD5Encode(fundPwd)))
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
            doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }.let { userMoneyResult ->
                _userMoney.postValue(userMoneyResult?.money)
                doNetwork(androidContext) {
                    moneyRepository.getRechCfg()
                }?.let { result ->
                    result.rechCfg?.let { moneyRechCfgData ->
                        uwBankType = moneyRechCfgData.uwTypes.firstOrNull { config -> config.type == TransferType.BANK.type }
                        _rechargeConfigs.value = moneyRechCfgData
                        getWithdrawCardList()
                        //判斷Tab要不要顯示
                        val withdrawConfig = moneyRechCfgData.uwTypes
                        val bankWithdrawSystemOperation = withdrawConfig.find { it.type == TransferType.BANK.type }?.open.toString() == FLAG_OPEN
                        val cryptoWithdrawSystemOperation = withdrawConfig.find { it.type == TransferType.CRYPTO.type }?.open.toString() == FLAG_OPEN
                        val operation = bankWithdrawSystemOperation && cryptoWithdrawSystemOperation
                        _withdrawTabIsShow.value = Event(operation)

                    }
                }
            }

        }
    }

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
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

    private fun checkBankCardDeleteData(): Boolean {
        if (withdrawPasswordMsg.value != "")
            return false
        return true
    }

    fun checkCreateName(createName: String) {
        _createNameErrorMsg.value = when {
            createName.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyCreateName(createName) -> {
                androidContext.getString(R.string.error_create_name)
            }
            else -> ""
        }
    }

    fun checkBankCardNumber(bankCardNumber: String) {
        _bankCardNumberMsg.value = when {
            bankCardNumber.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyBankCardNumber(bankCardNumber) -> {
                androidContext.getString(R.string.error_bank_card_number)
            }
            else -> ""
        }
    }

    fun checkNetWorkPoint(networkPoint: String) {
        _networkPointMsg.value = when {
            networkPoint.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyNetworkPoint(networkPoint) -> {
                androidContext.getString(R.string.error_network_point)
            }
            else -> ""
        }
    }

    fun checkWalletAddress(walletAddress: String) {
        _walletAddressMsg.value = when {
            walletAddress.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyCryptoWalletAddress(walletAddress) -> androidContext.getString(R.string.error_wallet_address)
            else -> ""
        }
    }

    fun checkWithdrawPassword(withdrawPassword: String) {
        _withdrawPasswordMsg.value = when {
            withdrawPassword.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWithdrawPassword(withdrawPassword) -> {
                androidContext.getString(R.string.error_withdraw_password)
            }
            else -> ""
        }
    }

    fun checkWithdrawAmount(withdrawCard: BankCardList?, inputAmount: String) {
        var withdrawAmount = inputAmount
        val amountLimit = getWithdrawAmountLimit()
        _withdrawAmountMsg.value = when {
            withdrawAmount.isEmpty() -> {
                withdrawAmount = "0"
                androidContext.getString(R.string.error_input_empty)
            }
            amountLimit.isBalanceMax && withdrawAmount.toDouble() > getWithdrawAmountLimit().max -> {
                androidContext.getString(R.string.error_withdraw_amount_bigger_than_balance)
            }
            !VerifyConstUtil.verifyWithdrawAmount(
                withdrawAmount,
                amountLimit.min,
                amountLimit.max
            ) -> {
                when (dealType) {
                    TransferType.BANK -> androidContext.getString(R.string.error_withdraw_amount_bank)
                    TransferType.CRYPTO -> androidContext.getString(R.string.error_withdraw_amount_crypto)
                }
            }
            else -> {
                ""
            }
        }
        getWithdrawRate(withdrawCard, withdrawAmount.toDouble())
    }

    fun getWithdrawHint() {
        val limit = getWithdrawAmountLimit()
        _withdrawAmountHint.value = String.format(
            androidContext.getString(R.string.hint_please_enter_withdraw_amount), limit.min.toLong(), limit.max.toLong()
        )
    }

    fun getWithdrawAmountLimit(): WithdrawAmountLimit {
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
        val maxLimit = if (configMaxLimit == null) balanceMaxLimit else min(balanceMaxLimit, configMaxLimit)
        return WithdrawAmountLimit(minLimit, maxLimit, isBalanceMax)
    }

    private fun getBalanceMaxLimit(): Double {
        return when (dealType) {
            TransferType.BANK -> ArithUtil.div((userMoney.value ?: 0.0), ((cardConfig?.feeRate?.plus(1) ?: 1.0)), 0, RoundingMode.FLOOR)
            TransferType.CRYPTO -> ArithUtil.div(
                ArithUtil.minus((userMoney.value), (cardConfig?.feeVal?.times(cardConfig?.exchangeRate ?: 0.0))),
                cardConfig?.exchangeRate ?: 1.0, 3, RoundingMode.FLOOR
            )
        }
    }

    fun getWithdrawRate(withdrawCard: BankCardList?, withdrawAmount: Double? = 0.0) {
        when (dealType) {
            TransferType.BANK -> {
                _withdrawCryptoAmountHint.value = ""
                _withdrawCryptoFeeHint.value = ""
                _withdrawRateHint.value = String.format(
                    androidContext.getString(R.string.withdraw_handling_fee_hint),
                    ArithUtil.toMoneyFormat(cardConfig?.feeRate?.times(100)),
                    ArithUtil.toMoneyFormat((cardConfig?.feeRate)?.times(withdrawAmount ?: 0.0))
                )
            }
            TransferType.CRYPTO -> {
                withdrawCard?.let {
                    val fee = cardConfig?.let { it.exchangeRate?.times(it.feeVal ?: 0.0) } ?: 0.0
                    val withdrawNeedAmount = if (withdrawAmount != 0.0 && withdrawAmount != null) cardConfig?.let { withdrawAmount.times(it.exchangeRate ?: 0.0) } ?: 0.0 else 0.0
                    _withdrawCryptoAmountHint.value = String.format(
                        androidContext.getString(R.string.withdraw_crypto_amount_hint),
                        ArithUtil.toMoneyFormat(withdrawNeedAmount)
                    )
                    _withdrawCryptoFeeHint.value = String.format(
                        androidContext.getString(R.string.withdraw_crypto_fee_hint),
                        ArithUtil.toMoneyFormat(fee)
                    )
                    _withdrawRateHint.value = String.format(
                        androidContext.getString(R.string.withdraw_crypto_exchange_rate_hint),
                        ArithUtil.toMoneyFormat(cardConfig?.exchangeRate)
                    )
                }
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
        }
    }

    private fun checkTransferTypeExistence(result: BankMyResult) {
        val bankCardExistence = result.bankCardList?.any { card -> card.uwType == TransferType.BANK.type } == true
        val bankWithdrawSwitch = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code

        val cryptoCardExistence = result.bankCardList?.any { card -> card.uwType == TransferType.CRYPTO.type } == true
        val cryptoWithdrawSwitch = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code

        val bankCardExist = bankCardExistence && bankWithdrawSwitch
        val cryptoCardExist = cryptoCardExistence && cryptoWithdrawSwitch

        val moneyCardExistSet = mutableSetOf<MoneyCardExist>().apply {
            add(MoneyCardExist(TransferType.BANK, bankCardExist))
            add(MoneyCardExist(TransferType.CRYPTO, cryptoCardExist))
        }

        _moneyCardExist.value = moneyCardExistSet
    }

    /**
     * 判斷當前銀行卡數量是否超出銀行卡綁定上限
     */
    fun checkBankCardCount() {
        var showAddCryptoCard = false //是否顯示虛擬幣
        val showAddBankCard: Boolean // 是否顯示銀行卡

        //虛擬幣是否可以被提款或新增卡片
        val cryptoOpen = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.open == MoneyRechCfg.Switch.ON.code

        val bankCardCountLimit = uwBankType?.detailList?.first()?.countLimit
        val bankCardCount = bankCardList.value?.count { it.transferType == TransferType.BANK }
        //銀行卡是否可以被提款或新增卡片
        val bankOpen = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.BANK.type }?.open == MoneyRechCfg.Switch.ON.code

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

        showAddBankCard = when {
            !bankOpen -> false
            bankCardCountLimit == null -> true
            bankCardCount == null -> true
            else -> bankCardCount < bankCardCountLimit
        }
        _addMoneyCardSwitch.value = TransferTypeAddSwitch(showAddBankCard, showAddCryptoCard)
    }

    data class CryptoCardCountLimit(val channel: String, var count: Int, var canBind: Boolean? = null)

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
            val configLimit = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.find { it.contract == cryptoCardCountLimit.channel }?.countLimit
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

        val cryptoCanBindList = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.toMutableList() ?: mutableListOf()

        cryptoCanBind.forEach { canBindData ->
            if (canBindData.canBind == false) {
                if (modifyMoneyChannel != canBindData.channel) {
                    val removeData = rechargeConfigs.value?.uwTypes?.find { it.type == TransferType.CRYPTO.type }?.detailList?.find { it.contract == canBindData.channel }
                    cryptoCanBindList.remove(removeData)
                }
            }
        }
        _addCryptoCardList.value = cryptoCanBindList
    }

    fun resetWithdrawPage() {
        _withdrawAmountMsg.value = ""
        _withdrawPasswordMsg.value = ""
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }
}