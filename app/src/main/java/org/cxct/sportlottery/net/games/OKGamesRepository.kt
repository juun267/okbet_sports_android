package org.cxct.sportlottery.net.games

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.games.api.OKGamesApi
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.GameCollectManager

object OKGamesRepository {

    private val okGamesApi by lazy { RetrofitHolder.createApiService(OKGamesApi::class.java) }
    val okPlayEvent = SingleLiveEvent<OKGameBean?>()
    val gameFiremEvent = SingleLiveEvent<List<GameFirmValues>>()
    var enterGameAfterLogin: OKGameBean? = null
    val collectLiveList = MutableLiveData<MutableList<OKGameBean>>()
    val recentGamesEvent = MutableLiveData<List<OKGameBean>>()
    private const val DEVICE = 2

    private fun paramDevice(): JsonObject {
        val params = JsonObject()
        params.addProperty("device", DEVICE)
        return params
    }

    private fun paramGame(gameEntryType: String = GameEntryType.OKGAMES): JsonObject {
        val params = paramDevice()
        params.addProperty("gameEntryType", gameEntryType)
        return params
    }
    suspend fun collectOkGames(gameId: Int, markCollect: Boolean = true,gameEntryType: String = GameEntryType.OKGAMES): ApiResult<Any> {
        val params = JsonObject()
        params.addProperty("id", gameId)
        params.addProperty("markCollect", markCollect)
        params.addProperty("gameEntryType", gameEntryType)
        return okGamesApi.okGamescollect(params)
    }

    suspend fun okGamesHall(): ApiResult<OKGamesHall> {
        return okGamesApi.getOKGamesHall(paramGame())
    }
    suspend fun getGameFirms(): ApiResult<List<OKGamesFirm>> {
        return okGamesApi.getGameFirms()
    }

    suspend fun okGamesJackpot(): ApiResult<String> {
        return okGamesApi.getOKGamesJackpot()
    }

    suspend fun getOKGamesRecordNew(): ApiResult<List<RecordNewEvent>> {
        return okGamesApi.getOKGamesRecordNew()
    }

    suspend fun getOKGamesRecordResult(): ApiResult<List<RecordNewEvent>> {
        return okGamesApi.getOKGamesRecordResult()
    }

    suspend fun getRecordNew(): ApiResult<List<RecordNewEvent>> {
        return okGamesApi.getRecordNew()
    }

    suspend fun getRecordResult(): ApiResult<List<RecordNewEvent>> {
        return okGamesApi.getRecordResult()
    }

    suspend fun getOKGamesList(
        page: Int,
        pageSize: Int,
        gameName: String?,
        categoryId: String?,
        firmId: String?,
        markCollect: Boolean? = null, // 获取收藏列表时为：true
        gameEntryType: String = GameEntryType.OKGAMES
    ): ApiResult<List<OKGameBean>> {

        val params = paramGame()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("gameEntryType",  gameEntryType)
        if (markCollect == null) {
            params.addProperty("gameName", gameName)
            params.addProperty("categoryId", categoryId)
            params.addProperty("firmId", firmId)
        } else {
            params.addProperty("markCollect", markCollect)
        }

        return okGamesApi.getOKGamesList(params)
    }


    /**
     * 首页okgames列表数据
     */
    suspend fun getHomeOKGamesList(
        gameEntryType: String,
        page: Int,
        pageSize: Int
    ): ApiResult<List<OKGameBean>> {

        val params = paramGame()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        //首页推荐 1启用,2禁用
        params.addProperty("enableHome", 1)
        params.addProperty("gameEntryType", gameEntryType)
        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getOKLiveList(page: Int, pageSize: Int, gameEntryType: String): ApiResult<List<OKGameBean>> {
        val params = paramGame()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("enableHome", 1)
        params.addProperty("gameEntryType", gameEntryType)

        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getHotGameList(page: Int, pageSize: Int): ApiResult<List<OKGameBean>> {
        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getNewGameList(page: Int, pageSize: Int): ApiResult<List<OKGameBean>> {
        val params = paramGame()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("isNew", true)
        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getMiniGameList(page: Int, pageSize: Int): ApiResult<List<OKGameBean>> {
        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("gameEntryType", GameEntryType.OKMINIS)

        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getHallOKSport(): ApiResult<OKGameBean> {
        return okGamesApi.getHallOKSport().apply { okPlayEvent.postValue(getData())}
    }


    suspend fun getGameCollectNum(): ApiResult<MutableMap<String,String>> {
        return okGamesApi.getGameCollectNum(sConfigData?.platformId?.toInt() ?: 1).apply {
            getData()?.let {
                GameCollectManager.gameCollectNum.postValue(it)
            }
        }
    }
    suspend fun guestLogin(firmType: String, gameCode: String): ApiResult<String> {
        return okGamesApi.guestLogin(firmType,firmType,gameCode)
    }
    open fun isSingleWalletType(firmType: String?): Boolean{
        if (firmType==null){
            return false
        }
        return gameFiremEvent.value?.firstOrNull{it.firmType == firmType }?.walletType == 1
    }
    open fun isGuestOpen(firmType: String?): Boolean{
        if (firmType==null){
            return false
        }
        return gameFiremEvent.value?.firstOrNull{it.firmType == firmType }?.guestOpen == 2
    }

    suspend fun getRecentGames(): ApiResult<List<OKGameBean>>{
       return okGamesApi.getRecentGames(paramDevice()).apply {
            getData()?.let {
                it.forEach { item->
                    //三方游戏时，把gameId赋值给gameCode,id
                    if(item.gameType!=GameEntryType.SPORT&&item.gameType!=GameEntryType.ES){
                        item.gameCode = item.gameId
                        item.id = item.gameId.toIntS(item.id)
                    }
                }
                recentGamesEvent.postValue(it)
            }
        }
    }

}