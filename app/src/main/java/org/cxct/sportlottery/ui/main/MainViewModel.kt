package org.cxct.sportlottery.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseSecurityCodeResult
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LanguageManager


class MainViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val thirdGameRepository: ThirdGameRepository,
    private val withdrawRepository: WithdrawRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val token
        get() = loginRepository.token

    val userId = loginRepository.userId

    val navActivity: LiveData<Event<Class<*>>>
        get() = _navActivity
    private val _navActivity = MutableLiveData<Event<Class<*>>>()

    val isCreditAccount: LiveData<Boolean> = loginRepository.isCreditAccount

    private val _promoteNoticeResult = MutableLiveData<Event<MessageListResult>>()
    val promoteNoticeResult: LiveData<Event<MessageListResult>>
        get() = _promoteNoticeResult

    private val _bannerList = MutableLiveData<List<ImageData>?>()
    val bannerList: LiveData<List<ImageData>?>
        get() = _bannerList

    private val _popImageList = MutableLiveData<List<ImageData>?>()
    val popImageList: LiveData<List<ImageData>?>
        get() = _popImageList

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }
    val goToThirdGamePage by lazy { thirdGameRepository.goToThirdGamePage }

    private val _enterThirdGameResult = MutableLiveData<EnterThirdGameResult>()
    val enterThirdGameResult: LiveData<EnterThirdGameResult>
        get() = _enterThirdGameResult

    val withdrawSystemOperation =
        withdrawRepository.withdrawSystemOperation
    val rechargeSystemOperation =
        withdrawRepository.rechargeSystemOperation
    val needToUpdateWithdrawPassword =
        withdrawRepository.needToUpdateWithdrawPassword //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword =
        withdrawRepository.settingNeedToUpdateWithdrawPassword //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToCompleteProfileInfo =
        withdrawRepository.settingNeedToCompleteProfileInfo //提款設置頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToCompleteProfileInfo =
        withdrawRepository.needToCompleteProfileInfo //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToBindBankCard =
        withdrawRepository.needToBindBankCard //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
    val needToSendTwoFactor =
        withdrawRepository.showSecurityDialog //判斷是不是要進行手機驗證 true: 需要, false: 不需要

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

    //需要完善個人資訊(缺電話號碼) needPhoneNumber
    val showPhoneNumberMessageDialog = withdrawRepository.hasPhoneNumber

    //獲取系統公告及跑馬燈
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result ->
                _promoteNoticeResult.postValue(Event(result))
            }
        }
    }

    //獲取輪播圖
    fun getBanner() {
        //H5轮播: imageType = 2
        sConfigData?.imageList?.filter { it.imageType == 2 }.apply {
            _bannerList.postValue(this)
        }
    }

    //獲取彈窗圖 //20210414 紀錄：體育暫不使用首頁彈窗圖功能
    fun getPopImage() {
        //H5彈窗圖: imageType = 7
        sConfigData?.imageList?.filter { it.imageType == 7 }.apply {
            _popImageList.postValue(this)
        }
    }

    fun getThirdGame() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                thirdGameRepository.getThirdGame()
            }
        }
    }

    fun goToLottery() {
        val lotteryData =
            gameCateDataList.value?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.first()?.gameList?.first()?.thirdGameData
        requestEnterThirdGame(lotteryData)
    }

    fun createSingleThirdGame(gameCategory: GameCategory, gameFirm: GameFirmValues): GameItemData {
        return thirdGameRepository.createSingleThirdGame(gameCategory, gameFirm)
    }

    fun requestEnterThirdGame(gameData: ThirdDictValues?) {
        when {
            gameData == null -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.error_url_fail)
                    )
                )
            }
            loginRepository.isLogin.value != true -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.NEED_REGISTER,
                        url = null,
                        errorMsg = null
                    )
                )
            }
            else -> {
                viewModelScope.launch {
                    val thirdLoginResult = thirdGameLogin(gameData)

                    //若自動轉換功能開啟，要先把錢都轉過去在進入遊戲
                    if (sConfigData?.thirdTransferOpen == FLAG_OPEN)
                        autoTransfer(gameData)

                    //20210526 result == null，代表 webAPI 處理跑出 exception，exception 處理統一在 BaseActivity 實作，這邊 result = null 直接略過
                    thirdLoginResult?.let {
                        if (it.success) {
                            _enterThirdGameResult.postValue(
                                EnterThirdGameResult(
                                    resultType = EnterThirdGameResult.ResultType.SUCCESS,
                                    url = thirdLoginResult.msg
                                )
                            )
                        } else {
                            _enterThirdGameResult.postValue(
                                EnterThirdGameResult(
                                    resultType = EnterThirdGameResult.ResultType.FAIL,
                                    url = null,
                                    errorMsg = thirdLoginResult?.msg
                                )
                            )
                        }
                    }

                }
            }
        }
    }

    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    fun clearThirdGame() {
        _enterThirdGameResult.postValue(
            EnterThirdGameResult(
                resultType = EnterThirdGameResult.ResultType.NONE,
                url = null,
                errorMsg = null
            )
        )
    }

    private suspend fun autoTransfer(gameData: ThirdDictValues) {
        val result = doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.autoTransfer(gameData.firmType)
        }

        if (result?.success == true)
            getMoney() //金額有變動，通知刷新
    }

    private suspend fun thirdGameLogin(gameData: ThirdDictValues): ThirdLoginResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.thirdLogin(gameData.firmType, gameData.gameCode)
        }
    }


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

    //提款設置判斷權限
    fun settingCheckPermissions() {
        viewModelScope.launch {
            withdrawRepository.settingCheckPermissions()
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

    fun getLanguageStatusSheetList(context: Context): ArrayList<StatusSheetData> {
        val languageList: ArrayList<StatusSheetData> = arrayListOf()
        LanguageManager.getLanguageStringList(context).forEachIndexed { index, string ->
            languageList.add(StatusSheetData(index.toString(), string))
        }
        return languageList
    }

    fun getOddTypeStatusSheetList(context: Context): List<StatusSheetData> {
        return listOf(
            StatusSheetData(OddsType.EU.code, context.resources?.getString(R.string.odd_type_eu)),
            StatusSheetData(OddsType.HK.code, context.resources?.getString(R.string.odd_type_hk)),
            StatusSheetData(OddsType.MYS.code, context.resources?.getString(R.string.odd_type_mys)),
            StatusSheetData(OddsType.IDN.code, context.resources?.getString(R.string.odd_type_idn))
        )
    }

    fun getDeafaultOddTypeStatusSheetData(context: Context): StatusSheetData {
        return when (loginRepository.sOddsType) {
            OddsType.EU.code -> StatusSheetData(
                OddsType.EU.code,
                context.resources?.getString(R.string.odd_type_eu)
            )
            OddsType.HK.code -> StatusSheetData(
                OddsType.HK.code,
                context.resources?.getString(R.string.odd_type_hk)
            )
            OddsType.MYS.code -> StatusSheetData(
                OddsType.MYS.code,
                context.resources?.getString(R.string.odd_type_mys)
            )
            OddsType.IDN.code -> StatusSheetData(
                OddsType.IDN.code,
                context.resources?.getString(R.string.odd_type_idn)
            )
            else -> StatusSheetData(
                OddsType.EU.code,
                context.resources?.getString(R.string.odd_type_eu)
            )
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

    fun navActivity(navClass: Class<*>) {
        when (navClass) {
            ProfileCenterActivity::class.java -> {
                if (isLogin.value == true) {
                    _navActivity.postValue(Event(navClass))
                } else {
                    _navActivity.postValue(Event(LoginActivity::class.java))
                }
            }
        }
    }
}