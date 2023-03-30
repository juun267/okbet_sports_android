package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.third_game.third_games.*
import org.cxct.sportlottery.ui.maintab.entity.GameCateData
import org.cxct.sportlottery.ui.maintab.entity.GameItemData
import org.cxct.sportlottery.ui.maintab.entity.GameTabData
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.util.Event
import retrofit2.Response
import timber.log.Timber

object ThirdGameRepository {

    private val _goToThirdGamePage = MutableLiveData<Event<ThirdGameCategory?>>()
    val goToThirdGamePage: LiveData<Event<ThirdGameCategory?>>
        get() = _goToThirdGamePage

    private val _homeCatePageDataList = MutableLiveData<List<GameCateData>>()
    val gameCateDataList: LiveData<List<GameCateData>>
        get() = _homeCatePageDataList

    private val mThirdGameData = MutableLiveData<ThirdGameData?>()
    val thirdGameData: LiveData<ThirdGameData?>
        get() = mThirdGameData

    suspend fun getThirdGame(): Response<ThirdGamesResult> {
        val response = OneBoSportApi.thirdGameService.getThirdGames()
        if (response.isSuccessful) {
            response.body()?.let { result ->
                if (result.success) {
                    val homeCatePageList = createHomeGameList(result.t)
                    _homeCatePageDataList.postValue(homeCatePageList)
                    mThirdGameData.value = result.t
                } else {
                    Timber.e("獲取第三方遊戲配置失敗")
                }
            }
        }
        return response
    }

    /**
     * 獲取第三方遊戲清單
     */
    suspend fun getThirdGameResponse(): Response<ThirdGamesResult> {
        return OneBoSportApi.thirdGameService.getThirdGames()
    }

    private fun localCateSort(code: String?): Int {
        return when (code) {
            ThirdGameCategory.LOCAL_SP.name -> 0
            ThirdGameCategory.CGCP.name -> 1
            ThirdGameCategory.LIVE.name -> 2
            ThirdGameCategory.QP.name -> 3
            ThirdGameCategory.DZ.name -> 4
            ThirdGameCategory.BY.name -> 5
            else -> 100
        }
    }

    fun createHomeGameList(thirdGameData: ThirdGameData?): MutableList<GameCateData> {
        //1. 第一層 category 按鈕
        val gameCatList = mutableListOf<GameCategory>()

        //第三方遊戲開啟判斷
        if (sConfigData?.thirdOpen == "1") {
            //判斷第一層級按鈕，各別要不要顯示 //category 內至少要有一項 open Game，才要顯示此 category
            thirdGameData?.gameCategories?.forEach gameCatList@{ categories ->
                categories.gameFirmIds?.split(",")?.forEach { gameFirmId ->
                    thirdGameData.gameFirmMap?.forEach {
                        val gameFirm = it.value
                        if (gameFirm.id.toString() == gameFirmId) {
                            gameCatList.add(categories)
                            return@gameCatList
                        }
                    }
                }
            }

            //20200226 紀錄： cate 暫時不使用 sort 排序
//            //cate list 排序，sort 從小到大排序
//            gameCatList.sortBy { it.sort }
            gameCatList.sortBy { localCateSort(it.code) }
        }

        val homeGameList = mutableListOf<GameCateData>()
        gameCatList.forEach { category ->
            val homeGame = GameCateData(ThirdGameCategory.getCategory(category.code))
            homeGame.isShowTabLayout = true

            //2. 第二層 tab 按鈕
            val gameFirmList = mutableListOf<GameFirmValues>()
            category.gameFirmIds?.split(",")?.forEach { gameFirmId ->
                thirdGameData?.gameFirmMap?.forEach gameFirmMap@{ data ->
                    val gameFirm = data.value
                    if (gameFirm.id.toString() == gameFirmId) {
                        gameFirmList.add(gameFirm)
                        return@gameFirmMap
                    }
                }
            }

            //tab list 排序，sort 從小到大排序
            gameFirmList.sortBy { it.sort }

            var isTabHasNoGameCount = 0 //在第二層中的tab，裡面的第三層game是否為空
            val singlePageList =
                mutableListOf<GameItemData>() //某些第三方遊戲只有兩層資料結構，所以需要獨立創建 singlePageList
            gameFirmList.forEach { gameFirm ->
                //3. 第三層 game 按鈕
                val pageList = createThirdGamePage(thirdGameData, gameFirm)

                //若第三層產生清單為空，用 gameFirm 產生一個 第三層按鈕
                if (pageList.isEmpty()) {
                    isTabHasNoGameCount += 1 //第二層中的tab裡面無遊戲
                    singlePageList.add(createSingleThirdGame(category, gameFirm))
                } else {
                    homeGame.tabDataList.add(GameTabData(category, gameFirm, pageList))
                }
            }
            if (singlePageList.isNotEmpty() && (isTabHasNoGameCount == gameFirmList.size)) { //如果有第三層遊戲 且 所有tab底下皆無遊戲
                homeGame.isShowTabLayout = false
                homeGame.tabDataList.add(GameTabData(category, null, singlePageList))
            }

            homeGameList.add(homeGame)
        }

        return homeGameList
    }

    private fun createThirdGamePage(
        thirdGameData: ThirdGameData?,
        gameFirm: GameFirmValues
    ): MutableList<GameItemData> {
        val pageList = mutableListOf<GameItemData>()
        thirdGameData?.thirdDictMap?.get(gameFirm.firmCode)?.forEach { thirdDict ->
            if (thirdDict?.gameCode.isNullOrEmpty())
                thirdDict?.gameCode = gameFirm.playCode

            //20200120 記錄問題: 修正電子類遊戲無法進入的問題 by Bee
            thirdDict?.open = gameFirm.open
            thirdDict?.firmName = gameFirm.firmName

            val entity = GameItemData(thirdDict)
            pageList.add(entity)
        }

        //page list 排序，sort 從小到大排序
        pageList.sortBy { it.thirdGameData?.sort }

        return pageList
    }

    fun createSingleThirdGame(gameCategory: GameCategory, gameFirm: GameFirmValues): GameItemData {
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
        thirdDict.firmName = gameFirm.firmName

        return GameItemData(thirdDict)
    }

}