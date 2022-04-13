package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.Constants.MATCH_LIVE_URL
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MATCH_TRACKER_URL
import org.cxct.sportlottery.network.matchLiveInfo.*
import org.cxct.sportlottery.network.matchTracker.MatchTrackerUrlResult
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
        @Url url: String?
    ): Response<P2UrlResponse>

}