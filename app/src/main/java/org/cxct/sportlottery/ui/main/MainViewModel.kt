package org.cxct.sportlottery.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*


class MainViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
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

    private val _promoteNoticeResult = MutableLiveData<Event<MessageListResult>>()
    val promoteNoticeResult: LiveData<Event<MessageListResult>>
        get() = _promoteNoticeResult

    private val _bannerList = MutableLiveData<List<ImageData>?>()
    val bannerList: LiveData<List<ImageData>?>
        get() = _bannerList

    private val _popImageList = MutableLiveData<List<ImageData>?>()
    val popImageList: LiveData<List<ImageData>?>
        get() = _popImageList

    val gameCateDataList by lazy { ThirdGameRepository.gameCateDataList }
    val goToThirdGamePage by lazy { ThirdGameRepository.goToThirdGamePage }

    private val _enterThirdGameResult = MutableLiveData<EnterThirdGameResult>()
    val enterThirdGameResult: LiveData<EnterThirdGameResult>
        get() = _enterThirdGameResult

    private val _countByInPlay = MutableLiveData<Int>()
    val countByInPlay: LiveData<Int>
        get() = _countByInPlay

    private val _countByToday = MutableLiveData<Int>()
    val countByToday: LiveData<Int>
        get() = _countByToday

    private val _inplayList = MutableLiveData<List<Item>>()
    val inplayList: LiveData<List<Item>>
        get() = _inplayList
    private val _liveRoundCount = MutableLiveData<String>()
    val liveRoundCount: LiveData<String>
        get() = _liveRoundCount

    //未讀總數目
    val totalUnreadMsgCount = infoCenterRepository.totalUnreadMsgCount

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
                ThirdGameRepository.getThirdGame()
            }
        }
    }

    fun updateOddsChangeOption(option: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.oddsChangeOption(
                    OddsChangeOptionRequest(option)
                )
            }?.let { result ->
                userInfoRepository.updateOddsChangeOption(option)
            }
        }
    }


    fun goToLottery() {
        val lotteryData =
            gameCateDataList.value?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.first()?.gameList?.first()?.thirdGameData
        requestEnterThirdGame(lotteryData)
    }

    fun createSingleThirdGame(gameCategory: GameCategory, gameFirm: GameFirmValues): GameItemData {
        return ThirdGameRepository.createSingleThirdGame(gameCategory, gameFirm)
    }

    fun requestEnterThirdGame(gameData: ThirdDictValues?) {
        when {
            gameData == null -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.hint_game_maintenance)
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
        return when (MultiLanguagesApplication.mInstance.sOddsType) {
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

    fun allTransferOut() {
        if (isThirdTransferOpen()) {
            //若自動轉換功能開啟，離開遊戲要全額轉出
            LoginRepository.allTransferOut()
        }
    }

    fun getMessageCount() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                val infoCenterRequest =
                    InfoCenterRequest(1, 1, 0)
                infoCenterRepository.getUserNoticeList(infoCenterRequest)
            }
        }
    }

    fun getSportList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }?.sportMenuData?.let { sportMenuList ->
                _countByToday.postValue(sportMenuList.menu.today.num)
                val sportCodeList = mutableListOf<StatusSheetData>()
                sportMenuList.menu.early.items.forEach {
                    sportCodeList.add(
                        StatusSheetData(
                            it.code,
                            GameType.getGameTypeString(
                                LocalUtils.getLocalizedContext(),
                                it.code
                            )
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    _sportCodeSpinnerList.value = sportCodeList
                }
            }
        }
    }


    fun getInPlayList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }?.sportMenuData?.let { sportMenuList ->
                _inplayList.postValue(sportMenuList.menu.inPlay.items)
                _countByInPlay.postValue(sportMenuList.menu.inPlay.num)
            }
        }
    }

    fun getLiveRoundCount() {
        viewModelScope.launch {
            var result = doNetwork(androidContext) {
                OneBoSportApi.matchService.getLiveRoundCount()
            }?.let {
                if (it.success) {
                    _liveRoundCount.postValue(it.t.toString())
                }
            }
        }
    }


}