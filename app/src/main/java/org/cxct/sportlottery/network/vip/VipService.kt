package org.cxct.sportlottery.network.vip

import org.cxct.sportlottery.network.Constants.USER_LEVEL_GROWTH
import org.cxct.sportlottery.network.vip.growth.LevelGrowthResult
import retrofit2.Response
import retrofit2.http.GET

interface VipService {
    @GET(USER_LEVEL_GROWTH)
    suspend fun getUserLevelGrowth(): Response<LevelGrowthResult>
}