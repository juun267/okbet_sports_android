package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.Constants.LIVE_LOGIN
import org.cxct.sportlottery.network.Constants.HOT_LIVE_LIST
import org.cxct.sportlottery.network.Constants.MATCH_LIVE_ROUND
import org.cxct.sportlottery.network.Constants.MATCH_LIVE_ROUND_COUNT
import org.cxct.sportlottery.network.Constants.MATCH_LIVE_ROUND_HALL
import org.cxct.sportlottery.network.Constants.MATCH_LIVE_URL
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MATCH_TRACKER_URL
import org.cxct.sportlottery.network.matchLiveInfo.*
import org.cxct.sportlottery.network.matchTracker.MatchTrackerUrlResult
import org.cxct.sportlottery.network.odds.list.MatchLiveResult
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchLiveResult
import retrofit2.Response
import retrofit2.http.*


interface MatchService {

    @POST(MATCH_PRELOAD)
    suspend fun getMatchPreload(
        @Body matchPreloadRequest: MatchPreloadRequest
    ): Response<MatchPreloadResult>

    @POST(MATCH_LIVE_URL)
    suspend fun getMatchLiveUrl(
        @Body matchLiveUrlRequest: MatchLiveUrlRequest
    ): Response<MatchLiveUrlResponse>

    @GET(MATCH_TRACKER_URL)
    suspend fun getMatchTrackerUrl(@Path("mappingId") mappingId: String): Response<MatchTrackerUrlResult>

    @GET
    suspend fun getMatchLiveInfo(@Url url: String): Response<MatchLiveInfoResponse>

    @GET
    suspend fun getLiveIUrl(@Url url: String): Response<IUrlResponse>

    @GET
    suspend fun getLiveP2Url(
        @Header("Authorization") accessToken: String?,
        @Header("Referer") referer: String?,
        @Url url: String?,
    ): Response<P2UrlResponse>

    @GET(MATCH_LIVE_ROUND + "/{roundNo}")
    suspend fun getMatchLiveRound(
        @Path("roundNo") roundNo: String,
    ): Response<MatchRoundResult>

    @GET(MATCH_LIVE_ROUND_COUNT)
    suspend fun getLiveRoundCount(): Response<MatchLiveUrlResponse>

    @GET(MATCH_LIVE_ROUND_HALL)
    suspend fun getLiveRoundHall(): Response<MatchLiveResult>
    @GET(HOT_LIVE_LIST)
    suspend fun getLiveList():Response<HotMatchLiveResult>


    @POST(LIVE_LOGIN)
    suspend fun liveLogin(
        @Body chatLiveLoginRequest: ChatLiveLoginRequest,
    ): Response<ChatLiveLoginResult>
}