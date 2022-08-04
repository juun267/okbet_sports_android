package org.cxct.sportlottery.network.bettingStation

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.chechBetting.CheckBettingResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BettingStationService {
    @GET(Constants.BETTING_STATION_QUERY)
    suspend fun bettingStationsQuery(): Response<BettingStationResult>

    @GET(Constants.BETTING_STATION_QUERY_INVITE)
    suspend fun queryPlatform(@Query("inviteCode") inviteCode: String): Response<CheckBettingResult>
}