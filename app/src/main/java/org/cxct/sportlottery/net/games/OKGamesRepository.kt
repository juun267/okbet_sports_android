package org.cxct.sportlottery.net.games

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiListResult
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.games.api.OKGamesApi
import org.cxct.sportlottery.net.games.data.OKGamesGroup
import org.cxct.sportlottery.net.games.data.OKGamesHall

object OKGamesRepository {

    val okGamesApi by lazy { RetrofitHolder.createApiService(OKGamesApi::class.java) }

    private fun paramDevice(): JsonObject {
        val params = JsonObject()
        params.addProperty("device", 2)
        return params
    }

    suspend fun collectOkGames(gameId: Int, collect: Boolean = true): ApiResult<Any?> {
        val params = JsonObject()
        params.addProperty("id", gameId)
//        params.addProperty("collect", collect)
        return okGamesApi.okGamescollect(params)
    }

    suspend fun okGamesHall(): ApiResult<OKGamesHall?> {
        return okGamesApi.getOKGamesHall(paramDevice())
    }

    suspend fun getOKGamesList(
        page: Int,
        pageSize: Int,
        gameName: String?,
        categoryId: String?,
        firmId: String?,
    ): ApiListResult<List<OKGamesGroup>?> {

        val params = paramDevice()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("gameName", gameName)
        params.addProperty("categoryId", categoryId)
        params.addProperty("firmId", firmId)
        return okGamesApi.getOKGamesList(params)
    }
}