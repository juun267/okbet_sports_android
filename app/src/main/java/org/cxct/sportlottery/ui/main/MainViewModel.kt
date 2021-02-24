package org.cxct.sportlottery.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.third_game.third_games.ThirdGameData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.ui.main.entity.GameTabData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.util.GameConfigManager
import timber.log.Timber


class MainViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin.apply {
            if (this.value == false && !loginRepository.isCheckToken) {
                checkToken()
            }
        }
    }

    val token = loginRepository.token
    val userId = loginRepository.userId
    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    private val _messageListResult = MutableLiveData<MessageListResult>()
    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    private val _messageDialogResult = MutableLiveData<MessageListResult>()

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    private val _bannerList = MutableLiveData<List<ImageData>>()
    val bannerList: LiveData<List<ImageData>>
        get() = _bannerList

    private val _popImageList = MutableLiveData<List<ImageData>>()
    val popImageList: LiveData<List<ImageData>>
        get() = _popImageList

    private val _homeCatePageDataList = MutableLiveData<List<GameCateData>>()
    val gameCateDataList: LiveData<List<GameCateData>>
        get() = _homeCatePageDataList

    private fun checkToken() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.checkToken()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.logout()
            }.apply {
                loginRepository.clear()
                betInfoRepository.clear()
                //TODO change timber to actual logout ui to da
                Timber.d("logout result is ${this?.success} ${this?.code} ${this?.msg}")
            }
        }
    }

    //獲取系統公告
    fun getMarquee() {
        val messageType = "1"
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.messageService.getMessageList(messageType)
            }?.let { result -> _messageListResult.postValue(result) }
        }
    }

    fun getMsgDialog() {
        val messageType = "2"
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.messageService.getMessageList(messageType)
            }?.let { result -> _messageDialogResult.postValue(result) }
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
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getThirdGames()
            }

            if (result?.success == true) {
                val homeCatePageList = createHomeGameList(result.t)
                _homeCatePageDataList.postValue(homeCatePageList)
            } else {
                //TODO simon test 獲取第三方遊戲配置失敗
            }
        }
    }

    private fun createHomeGameList(thirdGameData: ThirdGameData?): MutableList<GameCateData> {
        //1. 第一層 category 按鈕
        val gameCatList = mutableListOf<GameCategory>()

        //第三方遊戲開啟判斷
        if (sConfigData?.thirdOpen == "1") {
            //判斷第一層級按鈕，各別要不要顯示 //category 內至少要有一項 open Game，才要顯示此 category
            thirdGameData?.gameCategories?.forEach gameCatList@{ categories ->
                categories.gameFirmIds?.split(",")?.forEach { gameFirmId ->
                    thirdGameData.gameFirmMap?.forEach {
                        val gameFirm = it.value
                        if (gameFirm.id.toString() == gameFirmId && gameFirm.open == 1) {
                            gameCatList.add(categories)
                            return@gameCatList
                        }
                    }
                }
            }

            //cate list 排序，sort 從小到大排序
            gameCatList.sortBy { it.sort }
        }

        val homeGameList = mutableListOf<GameCateData>()
        gameCatList.forEach { category ->
            val homeGame = GameCateData(ThirdGameCategory.getCategory(category.code))
            homeGame.categoryThird.title = category.typeName //類別名稱
            homeGame.isShowTabLayout = true

            //2. 第二層 tab 按鈕
            val gameFirmList = mutableListOf<GameFirmValues>()
            category.gameFirmIds?.split(",")?.forEach { gameFirmId ->
                thirdGameData?.gameFirmMap?.forEach gameFirmMap@{ data ->
                    val gameFirm = data.value
                    if (gameFirm.id.toString() == gameFirmId && gameFirm.open == 1) {
                        gameFirmList.add(gameFirm)
                        return@gameFirmMap
                    }
                }
            }

            //tab list 排序，sort 從小到大排序
            gameFirmList.sortBy { it.sort }

            var isTabHasNoGameCount = 0 //在第二層中的tab，裡面的第三層game是否為空
            val singlePageList = mutableListOf<GameItemData>() //某些第三方遊戲只有兩層資料結構，所以需要獨立創建 singlePageList
            gameFirmList.forEach { gameFirm ->
                //3. 第三層 game 按鈕
                val pageList = createThirdGamePage(thirdGameData, gameFirm)

                //若第三層產生清單為空，用 gameFirm 產生一個 第三層按鈕
                if (pageList.isEmpty()) {
                    isTabHasNoGameCount += 1 //第二層中的tab裡面無遊戲
                    singlePageList.add(createSingleThirdGame(category, gameFirm))
                } else {
                    val iconUrl = GameConfigManager.getThirdGameIconUrlFirm(category.code, gameFirm.firmCode)
                    homeGame.tabDataList.add(GameTabData(tabTitle = gameFirm.firmName, gameList = pageList, iconUrl = iconUrl))
                }
            }
            if (singlePageList.isNotEmpty() && (isTabHasNoGameCount == gameFirmList.size)) { //如果有第三層遊戲 且 所有tab底下皆無遊戲
                homeGame.isShowTabLayout = false
                homeGame.tabDataList.add(GameTabData(null, singlePageList))
            }

            homeGameList.add(homeGame)
        }

        return homeGameList
    }

    private fun createThirdGamePage(thirdGameData: ThirdGameData?, gameFirm: GameFirmValues): MutableList<GameItemData> {
        val pageList = mutableListOf<GameItemData>()
        thirdGameData?.thirdDictMap?.get(gameFirm.firmCode)?.forEach { thirdDict ->
            if (thirdDict?.gameCode == null)
                thirdDict?.gameCode = gameFirm.playCode

            //20200120 記錄問題: 修正電子類遊戲無法進入的問題 by Bee
            thirdDict?.open = gameFirm.open

            val entity = GameItemData(thirdDict)
            pageList.add(entity)
        }

        //page list 排序，sort 從小到大排序
        pageList.sortBy { it.thirdGameData?.sort }

        return pageList
    }

    private fun createSingleThirdGame(gameCategory: GameCategory, gameFirm: GameFirmValues): GameItemData {
        //20190716 若 thirdDict 清單資料為空，用 gameFirm 產生一筆，
        val thirdDict = ThirdDictValues(
            id = gameFirm.id,
            gameCategory = gameCategory.code,
            chineseName = gameFirm.firmName,
            englishName = gameFirm.firmName,
            firmType = gameFirm.firmType,
            firmCode = gameFirm.firmCode,
            sort = gameFirm.sort,
            gameCode = gameFirm.playCode,
            isH5 = null,
            isFlash = null,
            imageName = null,
            h5ImageName = null,
            gameType = null
        )

        thirdDict.open = gameFirm.open

        return GameItemData(thirdDict)
    }

}