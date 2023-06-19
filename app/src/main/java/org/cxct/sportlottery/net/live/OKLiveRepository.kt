package org.cxct.sportlottery.net.live

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.net.live.api.OKLiveApi
import org.cxct.sportlottery.network.service.record.RecordNewEvent

object OKLiveRepository {

    val okLiveApi by lazy { RetrofitHolder.createApiService(OKLiveApi::class.java) }

    private fun paramDevice(): JsonObject {
        val params = JsonObject()
        params.addProperty("device", 2)
        params.addProperty("gameEntryType", "OK_LIVE")
        return params
    }

    suspend fun collectOkGames(gameId: Int, markCollect: Boolean = true): ApiResult<Any> {
        val params = JsonObject()
        params.addProperty("id", gameId)
        params.addProperty("markCollect", markCollect)
        return okLiveApi.okLivecollect(params)
    }

    suspend fun okGamesHall(): ApiResult<OKGamesHall> {
        return okLiveApi.getOKLiveHall(paramDevice())
    }

    suspend fun getOKLiveRecordNew(): ApiResult<List<RecordNewEvent>> {
        return okLiveApi.getOKLiveRecordNew()
    }

    suspend fun getOKLiveRecordResult(): ApiResult<List<RecordNewEvent>> {
        return okLiveApi.getOKLiveRecordResult()
    }

    suspend fun getOKLivesList(
        page: Int,
        pageSize: Int,
        gameName: String?,
        categoryId: String?,
        firmId: String?,
        markCollect: Boolean? = null, // 获取收藏列表时为：true
    ): ApiResult<List<OKGameBean>> {

        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        if (markCollect == null) {
            params.addProperty("gameName", gameName)
            params.addProperty("categoryId", categoryId)
            params.addProperty("firmId", firmId)
        } else {
            params.addProperty("markCollect", markCollect)
        }

        return okLiveApi.getOKLiveList(params)
    }


    /**
     * 首页okLive列表数据
     */
    suspend fun getHomeOKLivesList(
        page: Int,
        pageSize: Int
    ): ApiResult<List<OKGameBean>> {

        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        //首页推荐 1启用,2禁用
        params.addProperty("enableHome", 1)

        return okLiveApi.getOKLiveList(params)
    }
}