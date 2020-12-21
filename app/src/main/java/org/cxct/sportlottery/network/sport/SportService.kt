package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.SPORT_MENU
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers


interface SportService {

    @Headers("x-session-platform-code:plat1")
    @GET(SPORT_MENU)
    suspend fun getMenu(
    ): Response<SportMenuResult>
}