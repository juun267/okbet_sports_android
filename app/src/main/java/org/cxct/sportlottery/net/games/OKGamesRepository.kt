package org.cxct.sportlottery.net.games

import com.google.gson.JsonObject
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.games.api.OKGamesApi
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.service.record.RecordNewEvent

object OKGamesRepository {

    val okGamesApi by lazy { RetrofitHolder.createApiService(OKGamesApi::class.java) }

    private fun paramDevice(gameEntryType: String = GameEntryType.OKGAMES): JsonObject {
        val params = JsonObject()
        params.addProperty("device", 2)
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
        return okGamesApi.getOKGamesHall(paramDevice())
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

        val params = paramDevice()
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

        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        //首页推荐 1启用,2禁用
        params.addProperty("enableHome", 1)
        params.addProperty("gameEntryType", gameEntryType)
        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getOKLiveList(page: Int, pageSize: Int, gameEntryType: String): ApiResult<List<OKGameBean>> {
        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("enableHome", 1)
        params.addProperty("gameEntryType", gameEntryType)

        return okGamesApi.getOKGamesList(params)
    }

    suspend fun getMiniGameList(page: Int, pageSize: Int): ApiResult<List<OKGameBean>> {
        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("gameEntryType", GameEntryType.MINIGAMES)

        return okGamesApi.getOKGamesList(params)
    }
}