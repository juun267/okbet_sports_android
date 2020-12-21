package org.cxct.sportlottery.network.playcate

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface PlayCateListService {

    @Headers("x-session-platform-code:plat1")
    @GET(Constants.PLAYCATE_TYPE_LIST)
    suspend fun getPlayCateList(
        @Header("x-session-token") token: String?,
        @Query("gameType") gameType: String? = null
    ): Response<PlayCateListResult>

}