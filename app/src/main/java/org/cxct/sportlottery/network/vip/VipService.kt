package org.cxct.sportlottery.network.vip

import org.cxct.sportlottery.network.Constants.THIRD_REBATES
import org.cxct.sportlottery.network.Constants.USER_LEVEL_GROWTH
import org.cxct.sportlottery.network.vip.growth.LevelGrowthResult
import org.cxct.sportlottery.network.vip.thirdRebates.ThirdRebatesResult
import org.cxct.sportlottery.repository.sConfigData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VipService {
    @GET(USER_LEVEL_GROWTH)
    suspend fun getUserLevelGrowth(): Response<LevelGrowthResult>

    @GET(THIRD_REBATES)
    suspend fun getThirdRebates(
        @Query("firmCode") firmCode: String,
        @Query("firmType") firmType: String,
        @Query("platformId") platformId: String? = sConfigData?.platformId?.toString()
    ): Response<ThirdRebatesResult>
}