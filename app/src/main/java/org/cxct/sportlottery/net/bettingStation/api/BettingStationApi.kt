package org.cxct.sportlottery.net.bettingStation.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bettingStation.BettingStation
import retrofit2.http.GET

interface BettingStationApi {

    @GET(Constants.BETTING_STATION_QUERY)
    suspend fun getBettingStationList(): ApiResult<List<BettingStation>>

}