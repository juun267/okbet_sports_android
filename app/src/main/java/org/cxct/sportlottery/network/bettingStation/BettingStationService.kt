package org.cxct.sportlottery.network.bettingStation

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.GET

interface BettingStationService {
    @GET(Constants.BETTING_STATION_QUERY)
    suspend fun bettingStationsQuery(): Response<BettingStationResult>
}