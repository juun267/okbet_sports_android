package org.cxct.sportlottery.network.match

import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface MatchService {

    @POST(MATCH_PRELOAD)
    suspend fun getMatchPreload(
        @Body matchPreloadRequest: MatchPreloadRequest
    ): Response<MatchPreloadResult>
}