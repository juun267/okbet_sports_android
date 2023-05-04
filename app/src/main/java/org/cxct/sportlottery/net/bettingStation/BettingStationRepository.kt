package org.cxct.sportlottery.net.bettingStation

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.bettingStation.api.BettingStationApi
import org.cxct.sportlottery.network.bettingStation.BettingStation

object BettingStationRepository {

    private val bettingStationApi: BettingStationApi by lazy {
        RetrofitHolder.createApiService(BettingStationApi::class.java)
    }

    suspend fun getBettingStationList(): ApiResult<List<BettingStation>> {
        return bettingStationApi.getBettingStationList()
    }


}