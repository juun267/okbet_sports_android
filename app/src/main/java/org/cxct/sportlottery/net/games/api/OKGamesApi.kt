package org.cxct.sportlottery.net.games.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OKGamesApi {

    @POST(Constants.OKGAMES_COLLECT)
    suspend fun okGamescollect(@Body params: JsonObject): ApiResult<Any>

    @POST(Constants.OKGAMES_GAME_LIST)
    suspend fun getOKGamesList(@Body params: JsonObject): ApiResult<List<OKGameBean>>

    @POST(Constants.OKGAMES_HALL)
    suspend fun getOKGamesHall(@Body params: JsonObject): ApiResult<OKGamesHall>

    @GET(Constants.OKGAMES_RECORD_NEW)
    suspend fun getOKGamesRecordNew(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.OKGAMES_RECORD_RESULT)
    suspend fun getOKGamesRecordResult(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.RECORD_NEW)
    suspend fun getRecordNew(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.RECORD_RESULT)
    suspend fun getRecordResult(): ApiResult<List<RecordNewEvent>>

}