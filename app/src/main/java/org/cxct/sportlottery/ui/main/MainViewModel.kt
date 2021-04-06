package org.cxct.sportlottery.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.util.Event


class MainViewModel(
    private val androidContext: Context,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val thirdGameRepository: ThirdGameRepository,
    private val withdrawRepository: WithdrawRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin
    }

    val token
        get() = loginRepository.token

    val userId = loginRepository.userId
    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    private val _messageListResult = MutableLiveData<MessageListResult>()
    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    private val _messageDialogResult = MutableLiveData<Event<MessageListResult>>()
    val messageDialogResult: LiveData<Event<MessageListResult>>
        get() = _messageDialogResult

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

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

    val needToUpdateWithdrawPassword =
        withdrawRepository.needToUpdateWithdrawPassword //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword =
        withdrawRepository.settingNeedToUpdateWithdrawPassword //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val needToCompleteProfileInfo =
        withdrawRepository.needToCompleteProfileInfo //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToBindBankCard =
        withdrawRepository.needToBindBankCard //提款頁面是否需要新增銀行卡 true: 需要, false:不需要

    //獲取系統公告
    fun getMarquee() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(1)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result ->
                _messageListResult.postValue(result)
            }
        }
    }

    fun getMsgDialog() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result ->
                _messageDialogResult.postValue(Event(result))
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

    //獲取彈窗圖
    fun getPopImage() {
        //H5彈窗圖: imageType = 7
        sConfigData?.imageList?.filter { it.imageType == 7 }.apply {
            _popImageList.postValue(this)
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

    fun getThirdGame() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                thirdGameRepository.getThirdGame()
            }
        }
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
            (userInfo.value?.testFlag == TestFlag.GUEST.index) -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.GUEST,
                        url = null,
                        errorMsg = androidContext.getString(R.string.message_guest_no_permission)
                    )
                )
            }
            else -> {
                viewModelScope.launch {
                    val thirdLoginResult = thirdGameLogin(gameData)

                    //若自動轉換功能開啟，要先把錢都轉過去在進入遊戲
                    if (sConfigData?.thirdTransferOpen == FLAG_OPEN)
                        autoTransfer(gameData)

                    when (thirdLoginResult?.success) {
                        true -> {
                            _enterThirdGameResult.postValue(
                                EnterThirdGameResult(
                                    resultType = EnterThirdGameResult.ResultType.SUCCESS,
                                    url = thirdLoginResult.msg
                                )
                            )
                        }
                        else -> {
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


    //提款判斷權限
    fun withdrawCheckPermissions() {
        viewModelScope.launch {
            withdrawRepository.withdrawCheckPermissions()
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
}