package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.Constants.MATCH_LIVE_INFO
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.matchLiveInfo.IUrlResponse
import org.cxct.sportlottery.network.matchLiveInfo.MatchLiveInfoRequest
import org.cxct.sportlottery.network.matchLiveInfo.MatchLiveInfoResponse
import org.cxct.sportlottery.network.matchLiveInfo.P2UrlResponse
import retrofit2.Response
import retrofit2.http.*


interface MatchService {

    @POST(MATCH_PRELOAD)
    suspend fun getMatchPreload(
        @Body matchPreloadRequest: MatchPreloadRequest
    ): Response<MatchPreloadResult>

    @POST(MATCH_LIVE_INFO)
    suspend fun getMatchLiveInfo(
        @Body matchLiveInfoRequest: MatchLiveInfoRequest
    ): Response<MatchLiveInfoResponse>

    @GET
    suspend fun getLiveIUrl(@Url url: String): Response<IUrlResponse>

    @GET
    suspend fun getLiveP2Url(@Header("Authorization") accessToken: String, @Url url: String): Response<P2UrlResponse>

}