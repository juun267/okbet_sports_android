package org.cxct.sportlottery.ui.money.recharge

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.appsflyer.AppsFlyerLib
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.AFInAppEventUtil
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ONLINE_PAY
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.OnlineType
import org.cxct.sportlottery.network.money.config.MoneyRechCfgData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.money.config.RechType
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.JumpUtil.toExternalWeb
import org.cxct.sportlottery.util.QueryUtil.toUrlParamsFormat

class MoneyRechViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    val token = LoginRepository.token

    val rechargeConfigs: LiveData<MoneyRechCfgData?>
        get() = _rechargeConfigs
    private var _rechargeConfigs = MutableLiveData<MoneyRechCfgData?>()

    //在線支付
    val onlinePayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _onlinePayList
    private var _onlinePayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    //轉帳支付
    val transferPayList: LiveData<MutableList<MoneyPayWayData>>
        get() = _transferPayList
    private var _transferPayList = MutableLiveData<MutableList<MoneyPayWayData>>()

    //轉帳充值後API回傳
    val transferPayResult: LiveData<MoneyAddResult>
        get() = _transferPayResult
    private var _transferPayResult = MutableLiveData<MoneyAddResult>()

    //在線充值提交申請API回傳
    val onlinePayResult: LiveData<Long>
        get() = _onlinePayResult
    private var _onlinePayResult = MutableLiveData<Long>()

    //虛擬幣在線充值提交申請API回傳
    val onlinePayCryptoResult: LiveData<Double>
        get() = _onlinePayCryptoResult
    private var _onlinePayCryptoResult = MutableLiveData<Double>()

    //虛擬幣轉帳充值後API回傳
    val cryptoPayResult: LiveData<MoneyAddResult>
        get() = _cryptoPayResult
    private var _cryptoPayResult = MutableLiveData<MoneyAddResult>()

    //充值金額錯誤訊息
    val rechargeAmountMsg: LiveData<String>
        get() = _rechargeAmountMsg
    private var _rechargeAmountMsg = MutableLiveData<String>()

    //充值個數錯誤訊息
    val rechargeAccountMsg: LiveData<String>
        get() = _rechargeAccountMsg
    private var _rechargeAccountMsg = MutableLiveData<String>()

    //微信錯誤訊息
    val wxErrorMsg: LiveData<String>
        get() = _wxErrorMsg
    private var _wxErrorMsg = MutableLiveData<String>()

    //姓名錯誤訊息
    val nameErrorMsg: LiveData<String>
        get() = _nameErrorMsg
    private var _nameErrorMsg = MutableLiveData<String>()

    //区块链交易ID錯誤訊息
    val hashCodeErrorMsg: LiveData<String>
        get() = _hashCodeErrorMsg
    private var _hashCodeErrorMsg = MutableLiveData<String>()

    //支付截圖錯誤訊息
    val voucherPathErrorMsg: LiveData<Event<String>>
        get() = _voucherPathErrorMsg
    private var _voucherPathErrorMsg = MutableLiveData<Event<String>>()

    //暱稱錯誤訊息
    val nickNameErrorMsg: LiveData<String>
        get() = _nickNameErrorMsg
    private var _nickNameErrorMsg = MutableLiveData<String>()

    //線上支付充值金額
    val rechargeOnlineAmountMsg: LiveData<String>
        get() = _rechargeOnlineAmountMsg
    private var _rechargeOnlineAmountMsg = MutableLiveData<String>()

    //線上支付充值賬號
    val rechargeOnlineAccountMsg: LiveData<String>
        get() = _rechargeOnlineAccountMsg
    private var _rechargeOnlineAccountMsg = MutableLiveData<String>()

    //銀行卡號錯誤訊息
    val bankIDErrorMsg: LiveData<String>
        get() = _bankIDErrorMsg
    private var _bankIDErrorMsg = MutableLiveData<String>()

    //上傳支付截圖
    val voucherUrlResult: LiveData<Event<String>> = AvatarRepository.voucherUrlResult
    //上传支付截图
    val uploadPayResult:LiveData<Event<UploadImgResult?>> = AvatarRepository.uploadResult
    //线上首次充值提示文字
    val onlinePayFirstRechargeTips: LiveData<Event<String?>>
        get() = _onlinePayFirstRechargeTips
    private val _onlinePayFirstRechargeTips = MutableLiveData<Event<String?>>()
    //在線充值提交申請API回傳
    val rechCheckMsg: LiveData<Event<String>>
        get() = _rechCheckMsg
    private var _rechCheckMsg = MutableLiveData<Event<String>>()

    var dailyConfigEvent = SingleLiveEvent<List<DailyConfig>>()

    //更新使用者資料
    fun getUserInfo() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                UserInfoRepository.getUserInfo()
            }
        }
    }

    //獲取充值的基礎配置
    fun getRechCfg() {
        Log.e(">>>", "getRechCfg")
        WithdrawRepository.moneyRechCfgResult.value?.rechCfg?.let {
            _rechargeConfigs.value = it
            filterBankList(it.rechTypes,it.rechCfgs)
        }
    }

    //篩選List要顯示的資料
    private fun filterBankList(rechTypesList: List<RechType>, rechConfigList: List<RechCfg>) {
        try {
            val onlineData: MutableList<MoneyPayWayData> = mutableListOf()
            val transferData: MutableList<MoneyPayWayData> = mutableListOf()
            //篩選，後台有開"且"使用者有權限的充值方式
            val filterRechargeDataList = mutableListOf<RechCfg>()

            rechTypesList.forEach { rechTypes ->
                rechConfigList.forEach { rechConfig ->
                    if (rechTypes.value == rechConfig.rechType) {
                        filterRechargeDataList.add(rechConfig)
                    }
                }
            }
            val dataList: MutableList<MoneyPayWayData> = mutableListOf()
            MoneyManager.getMoneyPayWayList()?.forEach { moneyPayWay ->
                if (filterRechargeDataList.firstOrNull {
                        it.rechType == org.cxct.sportlottery.network.common.RechType.ONLINEPAYMENT.code && it.onlineType == moneyPayWay.onlineType
                                || it.rechType != org.cxct.sportlottery.network.common.RechType.ONLINEPAYMENT.code && it.rechType == moneyPayWay.rechType
                    } != null) {
                    dataList.add(moneyPayWay)
                }
            }
            dataList.forEach {
                when (it.rechType) {
                    org.cxct.sportlottery.network.common.RechType.ONLINEPAYMENT.code -> {
                        if (it.onlineType != 202) //後端說要filter掉202
                            onlineData.add(
                                it
                            )
                    }
                    else -> transferData.add(it)
                }
            }
            _onlinePayList.value = onlineData
            _transferPayList.value = transferData
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //充值頁面[轉帳充值]-[按鈕]提交申請
    fun rechargeSubmit(
        moneyAddRequest: MoneyAddRequest,
        rechType: String?,
        rechConfig: RechCfg?
    ) {
        checkAll(moneyAddRequest, rechType, rechConfig)
        if (checkTransferPayInput()) {
            rechargeAdd(moneyAddRequest)
        }
    }

    //充值页面[虚拟币充值]-[按钮]提交申请
    fun rechargeCryptoSubmit(
        moneyAddRequest: MoneyAddRequest,
        rechType: String?,
        rechConfig: RechCfg?
    ) {
        checkAll(moneyAddRequest, rechType, rechConfig)
        if (checkTransferPayCryptoInput()) {
            rechargeAdd(
                moneyAddRequest,
                ArithUtil.mul(
                    (moneyAddRequest.depositMoney ?: "0.0").toString().toDouble(),
                    (rechConfig?.exchangeRate ?: 0.0)
                ).toString()
            )
        }
    }

    //轉帳支付 - 一般充值
    private fun rechargeAdd(moneyAddRequest: MoneyAddRequest) {
        if (checkTransferPayInput()) {
            if (!checkOnlinePayFirstRechargeLimit(moneyAddRequest.depositMoney!!))
                return
            viewModelScope.launch {
                doNetwork(androidContext) {
                    MoneyRepository.rechargeAdd(moneyAddRequest)
                }.let {
                    it?.result = moneyAddRequest.depositMoney.toString()//金額帶入result
                    _transferPayResult.value = it
                    AFInAppEventUtil.deposit(moneyAddRequest?.depositMoney ?: "",
                        sConfigData?.systemCurrency ?: "")
                }
            }
        }
    }

    //轉帳支付 - 虛擬幣充值 最後顯示的RMB要自己換算
    private fun rechargeAdd(moneyAddRequest: MoneyAddRequest, rechargeMoney:String) {
        if (checkTransferPayInput()) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    MoneyRepository.rechargeAdd(moneyAddRequest)
                }.let {
                    it?.result = rechargeMoney//金額帶入result
                    _cryptoPayResult.value = it
                    AFInAppEventUtil.deposit(moneyAddRequest?.depositMoney ?: "",
                        sConfigData?.systemCurrency ?: "")
                }
            }
        }
    }

    //在線支付 - 一般充值
    fun rechargeNormalOnlinePay(
        context: Context,
        mSelectRechCfgs: RechCfg?,
        depositMoney: String,
        bankCode: String?,
        payer: String?,
        activityType:Int?
    ) {
        checkRcgOnlineAmount(depositMoney, mSelectRechCfgs)
        if (onlinePayInput()) {
            if (!checkOnlinePayFirstRechargeLimit(depositMoney))
                return

            var url = Constants.getBaseUrl() + USER_RECHARGE_ONLINE_PAY
            val rechCfgId = (mSelectRechCfgs?.id ?: "").toString()
            val queryMap = hashMapOf(
                "x-session-token" to (LoginRepository.token ?: ""),
                "rechCfgId" to rechCfgId,
                "bankCode" to (bankCode ?: ""),
                "depositMoney" to depositMoney,
                "clientType" to "2",
            ).apply {
                activityType?.let { put("activityType",it.toString()) }
                if (!payer.isNullOrEmpty())
                    put("payer", payer)
                AppsFlyerLib.getInstance().getAppsFlyerUID(context)?.let {
                    put("appsFlyerId", it)
                    put("appsFlyerKey", BuildConfig.AF_APPKEY)
                    put("appsFlyerPkgName",BuildConfig.APPLICATION_ID)
                }
            }
            val params = JsonObject()
            queryMap.forEach { params.addProperty(it.key,it.value) }
            callApi({org.cxct.sportlottery.net.money.MoneyRepository.rechCheckStauts(params)}){
                if (it.succeeded()){
                    url += toUrlParamsFormat(queryMap)
                    toExternalWeb(context, url)
                    AFInAppEventUtil.deposit(depositMoney ?: "",
                        sConfigData?.systemCurrency ?: "")
                    _onlinePayResult.value = depositMoney.toLong() //金額帶入result
                }else{
                    _rechCheckMsg.postValue(Event(it.msg))
                }
            }
        }
    }
    //在線支付 - 虛擬幣充值
    fun rechargeOnlinePay(
        context: Context,
        mSelectRechCfgs: RechCfg?,
        depositMoney: String,
        payee: String?,
        payeeName: String?,
        activityType:Int?
    ) {
        checkRcgOnlineAccount(depositMoney, mSelectRechCfgs)
        if (onlineCryptoPayInput()) {
            var url = Constants.getBaseUrl() + USER_RECHARGE_ONLINE_PAY
            val rechCfgId = (mSelectRechCfgs?.id ?: "").toString()
            val queryMap = hashMapOf(
                "x-session-token" to (LoginRepository.token ?: ""),
                "rechCfgId" to (mSelectRechCfgs?.id ?: "").toString(),
                "payee" to (payee ?: ""),
                "payeeName" to (payeeName ?: ""),
                "depositMoney" to depositMoney,
                "clientType" to "2"
            ).apply {
                activityType?.let { put("activityType",it.toString()) }
                AppsFlyerLib.getInstance().getAppsFlyerUID(context)?.let {
                    put("appsFlyerId", it)
                    put("appsFlyerKey", BuildConfig.AF_APPKEY)
                    put("appsFlyerPkgName",BuildConfig.APPLICATION_ID)
                }
            }
            val params = JsonObject()
            queryMap.forEach { params.addProperty(it.key,it.value) }
            callApi({org.cxct.sportlottery.net.money.MoneyRepository.rechCheckStauts(params)}){
                if (it.succeeded()){
                    url += toUrlParamsFormat(queryMap)
                    toExternalWeb(context, url)
                    AFInAppEventUtil.deposit(depositMoney ?: "",
                        sConfigData?.systemCurrency ?: "")
                    _onlinePayCryptoResult.value = ArithUtil.mul(depositMoney.toDouble(),
                        (mSelectRechCfgs?.exchangeRate ?: 0.0)) //金額帶入result
                }else{
                    _rechCheckMsg.postValue(Event(it.msg))
                }
            }
        }
    }

    //轉帳支付 - 送出前判斷全部
    private fun checkAll(
        moneyAddRequest: MoneyAddRequest,
        rechType: String?,
        rechConfig: RechCfg?
    ) {
        when (rechType) {
            MoneyType.BANK_TYPE.code, MoneyType.CTF_TYPE.code -> {
                checkUserName(MoneyType.BANK_TYPE.code,moneyAddRequest.payerName)
                checkBankID(moneyAddRequest.payer ?: "")
            }
            MoneyType.WX_TYPE.code -> {
                checkWX(moneyAddRequest.payerName)
            }
            MoneyType.ALI_TYPE.code -> {
                checkNickName(moneyAddRequest.payerName)
                checkUserName(MoneyType.ALI_TYPE.code,moneyAddRequest.payerInfo ?: "")
            }
            MoneyType.CRYPTO_TYPE.code ->{
                checkHashCode(moneyAddRequest.txHashCode ?: "")
                checkRechargeAccount(moneyAddRequest.depositMoney.toString() , rechConfig)
                checkScreenShot(moneyAddRequest.voucherPath)
            }
        }
        checkRechargeAmount(moneyAddRequest.depositMoney.toString(), rechConfig)
    }

    //充值金額驗證
    fun checkRechargeAmount(rechargeAmount: String, rechConfig: RechCfg?) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeAmountMsg.value = when {
            rechargeAmount.isNullOrEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            rechargeAmount.toLongOrNull() == null || rechargeAmount.toLong().equals(0) -> {
                LocalUtils.getString(R.string.error_recharge_amount_format)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) == -1 -> {
                LocalUtils.getString(R.string.error_amount_limit_less)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) == 1 -> {
                LocalUtils.getString(R.string.error_amount_limit_exceeded)
            }
            else -> ""
        }
    }

    //充值個數驗證
    fun checkRechargeAccount(rechargeAmount: String, rechConfig: RechCfg?) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeAccountMsg.value = when {
            rechargeAmount.isNullOrEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            rechargeAmount.toLongOrNull() == null || rechargeAmount.toLong().equals(0) -> {
                LocalUtils.getString(R.string.error_recharge_amount_format)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) != 0 -> {
                LocalUtils.getString(R.string.error_amount_limit_exceeded)
            }
            else -> ""
        }
    }

    //在線充值金額
    fun checkRcgOnlineAmount(rechargeAmount: String, rechConfig: RechCfg?) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeOnlineAmountMsg.value = when {
            rechargeAmount.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            rechargeAmount.toLongOrNull() == null || rechargeAmount.toLong().equals(0) -> {
                LocalUtils.getString(R.string.error_recharge_amount_format)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            )  == -1 -> {
                LocalUtils.getString(R.string.error_amount_limit_less)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) == 1 -> {
                LocalUtils.getString(R.string.error_amount_limit_exceeded)
            }
            else -> ""
        }
    }

    /**
     * 檢查是否符合首次充值限額
     * @return true:符合, false:不符合
     */
    private fun checkOnlinePayFirstRechargeLimit(rechargeAmount: String): Boolean {
        return when {
            checkFirstRecharge() && !VerifyConstUtil.verifyFirstRechargeAmount(rechargeAmount) -> {
                _onlinePayFirstRechargeTips.postValue(
                    Event(
                        androidContext.getString(
                            R.string.error_first_recharge_amount,
                            TextUtil.formatMoney(sConfigData?.firstRechLessAmountLimit ?: 0)
                        )
                    )
                )
                false
            }
            else -> {
                _onlinePayFirstRechargeTips.postValue(Event(null))
                true
            }
        }
    }

    /**
     * 判斷是否為首次充值
     * @return true: 首次, false: 非首次
     */
    private fun checkFirstRecharge(): Boolean {
        return UserInfoRepository.userInfo.value?.firstRechTime.isNullOrEmpty()
    }

    fun checkRcgNormalOnlineAccount(rechargeAccount: String) {
        _rechargeOnlineAccountMsg.value = when {
            rechargeAccount.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            else -> {
                ""
            }
        }
    }

    //在線充值 虛擬幣充值個數認證
    private fun checkRcgOnlineAccount(
        rechargeAmount: String,
        rechConfig: RechCfg?
    ) {
        val channelMinMoney = rechConfig?.minMoney?.toLong() ?: 0
        val channelMaxMoney = rechConfig?.maxMoney?.toLong()
        _rechargeAccountMsg.value = when {
            rechargeAmount.isNullOrEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            rechargeAmount.toLongOrNull() == null || rechargeAmount.toLong().equals(0) -> {
                LocalUtils.getString(R.string.error_recharge_amount_format)
            }
            VerifyConstUtil.verifyRechargeAmount(
                rechargeAmount,
                channelMinMoney,
                channelMaxMoney
            ) != 0 -> {
                LocalUtils.getString(R.string.error_amount_limit_exceeded)
            }
            else -> ""
        }
    }

    //微信認證
    fun checkWX(wxID: String) {
        _wxErrorMsg.value = when {
            wxID.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            else -> {
                ""
            }
        }
    }

    //姓名認證
    fun checkUserName(moneyType: String, userName: String) {
        _nameErrorMsg.value = when {
            userName.isEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFullName(userName) -> LocalUtils.getString(R.string.error_input_has_blank)
            else -> {
                ""
            }
        }
    }

    //暱稱認證
    fun checkNickName(userName: String) {
        _nickNameErrorMsg.value = when {
            userName.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            else -> {
                ""
            }
        }
    }

    //銀行卡號認證
    fun checkBankID(bankId: String) {
        _bankIDErrorMsg.value = when {
            bankId.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            else -> {
                ""
            }
        }
    }

    //HashCode区块链交易ID認證
    fun checkHashCode(HashCode:String){
        _hashCodeErrorMsg.value = when {
            HashCode.isEmpty() -> {
                LocalUtils.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyHashCode(
                HashCode
            ) -> {
                LocalUtils.getString(R.string.error_hashcode)
            }
            else -> {
                ""
            }
        }
    }

    //驗證支付截圖
    private fun checkScreenShot(voucherPath:String?){
        viewModelScope.launch {
            _voucherPathErrorMsg.value = when {
                voucherPath.isNullOrEmpty() -> {
                    Event(LocalUtils.getString(R.string.title_upload_pic_plz))
                }
                else -> {
                    Event("")
                }
            }
        }
    }

    private fun checkTransferPayInput(): Boolean {
        if (!rechargeAmountMsg.value.isNullOrEmpty())
            return false
        if (!wxErrorMsg.value.isNullOrEmpty())
            return false
        if (!nameErrorMsg.value.isNullOrEmpty())
            return false
        if (!bankIDErrorMsg.value.isNullOrEmpty())
            return false
        if (!nickNameErrorMsg.value.isNullOrEmpty())
            return false
        return true
    }

    private fun checkTransferPayCryptoInput(): Boolean {
        if (!rechargeAccountMsg.value.isNullOrEmpty())
            return false
        if (!hashCodeErrorMsg.value.isNullOrEmpty())
            return false
        if (!voucherPathErrorMsg.value?.peekContent().isNullOrEmpty())
            return false

        return true
    }

    private fun onlinePayInput(): Boolean {
        if (!_rechargeOnlineAmountMsg.value.isNullOrEmpty())
            return false
        return true
    }

    private fun onlineCryptoPayInput(): Boolean {
        if (!_rechargeAccountMsg.value.isNullOrEmpty())
            return false
        return true
    }


    fun clearnRechargeStatus() {
        _rechargeAmountMsg.value = ""
        _wxErrorMsg.value = ""
        _nameErrorMsg.value = ""
        _bankIDErrorMsg.value = ""
        _nickNameErrorMsg.value = ""
        _rechargeOnlineAmountMsg.value = ""
        _hashCodeErrorMsg.value = ""
        _rechargeAccountMsg.value = ""
        _voucherPathErrorMsg.value = Event("")
    }

    //上傳支付截圖
    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                AvatarRepository.uploadVoucher(uploadImgRequest)
            }
        }
    }

    //在線充值帳戶選單名稱
    fun getOnlinePayTypeName(onlineType: Int?): String {
        return when (onlineType) {//在线充值类型：1-网银在线充值、2-支付宝在线充值、3-微信在线充值、4-qq在线充值、5-出款、6、信用卡在线充值、7-百度钱包、8-京东钱包
            OnlineType.WY.type -> androidContext.resources.getString(R.string.online_bank)
            OnlineType.ZFB.type -> androidContext.resources.getString(R.string.online_alipay)
            OnlineType.WX.type -> androidContext.resources.getString(R.string.online_weixin)
            OnlineType.QQ.type -> androidContext.resources.getString(R.string.online_qq)
            OnlineType.XYK.type -> androidContext.resources.getString(R.string.online_credit_card)
            OnlineType.JUAN.type -> androidContext.resources.getString(R.string.online_juan)
            OnlineType.DISPENSHIN.type -> androidContext.resources.getString(R.string.online_dispenshing)
            OnlineType.ONLINEBANK.type -> androidContext.resources.getString(R.string.online_online_bank)
            OnlineType.GCASH.type -> androidContext.resources.getString(R.string.online_gcash)
            OnlineType.GRABPAY.type -> androidContext.resources.getString(R.string.online_grab)
            OnlineType.PAYMAYA.type -> androidContext.resources.getString(R.string.online_maya)
            OnlineType.PAYPAL.type -> androidContext.resources.getString(R.string.online_paypal)
            OnlineType.DRAGON_PAY.type -> androidContext.resources.getString(R.string.online_gragon_pay)
            OnlineType.FORTUNE_PAY.type -> androidContext.resources.getString(R.string.online_fortune_pay)
            OnlineType.ONLINEBANK.type -> androidContext.resources.getString(R.string.online_online_bank)
            OnlineType.AUB.type -> androidContext.resources.getString(R.string.aub)
            OnlineType.EPON.type -> androidContext.resources.getString(R.string.epon)
            else -> ""
        }
    }

    //轉帳充值帳戶選單名稱
    fun getPayTypeName(rechType: String?): String {
        return when (rechType) {
            org.cxct.sportlottery.network.common.RechType.ALIPAY.code -> androidContext.resources.getString(
                R.string.title_alipay
            )
            org.cxct.sportlottery.network.common.RechType.WX.code -> androidContext.resources.getString(
                R.string.title_weixin
            )
            org.cxct.sportlottery.network.common.RechType.CFT.code -> androidContext.resources.getString(
                R.string.title_cft
            )
            org.cxct.sportlottery.network.common.RechType.GCASH.code -> androidContext.resources.getString(
                R.string.online_gcash
            )
            org.cxct.sportlottery.network.common.RechType.GRABPAY.code -> androidContext.resources.getString(
                R.string.online_grab
            )
            org.cxct.sportlottery.network.common.RechType.PAYMAYA.code -> androidContext.resources.getString(
                R.string.online_maya
            )
            org.cxct.sportlottery.network.common.RechType.PAYPAL.code -> androidContext.resources.getString(//TODO Bill 等後端補上Paypal
                R.string.online_paypal
            )
            else -> ""
        }
    }
    fun getDailyConfig(){
        callApi({org.cxct.sportlottery.net.money.MoneyRepository.rechDailyConfig()}){
            if (it.succeeded()){
                it.getData()?.let {
                    dailyConfigEvent.postValue(it)
                }
            }
        }
    }
}
