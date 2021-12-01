package org.cxct.sportlottery.network.playcate

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlayCateListService {
    @GET(Constants.PLAYCATE_TYPE_LIST)
    suspend fun getPlayCateList(
        @Query("gameType") gameType: String? = null
    ): Response<PlayCateListResult>

}