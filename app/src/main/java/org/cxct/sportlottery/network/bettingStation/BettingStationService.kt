package org.cxct.sportlottery.network.bettingStation

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.chechBetting.CheckBettingResult
import org.cxct.sportlottery.repository.sConfigData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BettingStationService {
    @GET(Constants.BETTING_STATION_QUERY)
    suspend fun bettingStationsQuery(): Response<BettingStationResult>

    @GET(Constants.BETTING_STATION_QUERY_INVITE)
    suspend fun queryPlatform(@Query("inviteCode") inviteCode: String): Response<CheckBettingResult>

    @GET(Constants.BETTING_STATION_QUERY_BY_BETTING_STATION_ID)
    suspend fun queryByBettingStationId(
        @Query("platformId") platformId: Int? = sConfigData?.platformId?.toInt(),
        @Query("bettingStationId") bettingStationId: Int?
    ): Response<QueryByBettingStationIdResult>
}