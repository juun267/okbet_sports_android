package org.cxct.sportlottery.net.games.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.LOGIN_SRC
import retrofit2.http.*

interface OKGamesApi {

    @POST(Constants.OKGAMES_COLLECT)
    suspend fun okGamescollect(@Body params: JsonObject): ApiResult<Any>

    @POST(Constants.OKGAMES_GAME_LIST)
    suspend fun getOKGamesList(@Body params: JsonObject): ApiResult<List<OKGameBean>>

    @POST(Constants.OKGAMES_HALL)
    suspend fun getOKGamesHall(@Body params: JsonObject): ApiResult<OKGamesHall>

    @GET(Constants.OKGAMES_JACKPOT)
    suspend fun getOKGamesJackpot(): ApiResult<String>

    @GET(Constants.OKGAMES_RECORD_NEW)
    suspend fun getOKGamesRecordNew(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.OKGAMES_RECORD_RESULT)
    suspend fun getOKGamesRecordResult(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.RECORD_NEW)
    suspend fun getRecordNew(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.RECORD_RESULT)
    suspend fun getRecordResult(): ApiResult<List<RecordNewEvent>>

    @GET(Constants.GET_GAMEFIRMS)
    suspend fun getGameFirms(): ApiResult<List<OKGamesFirm>>

    @GET(Constants.GET_HALL_OKSPORT)
    suspend fun getHallOKSport(): ApiResult<OKGameBean>

    @GET(Constants.GET_GAME_COLLECT_NUM)
    suspend fun getGameCollectNum(@Query("platformId") platformId: Int): ApiResult<MutableMap<String,String>>

    @GET(Constants.GUEST_LOGIN)
    suspend fun guestLogin(@Path("firmType") firmType: String,
                           @Query("firmType") firmType1: String,
                           @Query("gameCode") gameCode: String,
                           @Query("loginSrc") loginSrc: Long = LOGIN_SRC): ApiResult<String>
}