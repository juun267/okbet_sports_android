package org.cxct.sportlottery.net.games.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiListResult
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.data.OKGamesGroup
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.POST

interface OKGamesApi {

    @POST(Constants.OKGAMES_COLLECT)
    suspend fun okGamescollect(@Body params: JsonObject): ApiResult<Any>

    @POST(Constants.OKGAMES_GAME_LIST)
    suspend fun getOKGamesList(@Body params: JsonObject): ApiListResult<List<OKGamesGroup>>

    @POST(Constants.OKGAMES_HALL)
    suspend fun getOKGamesHall(@Body params: JsonObject): ApiResult<OKGamesHall>
}