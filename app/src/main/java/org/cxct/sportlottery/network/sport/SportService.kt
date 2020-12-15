package org.cxct.sportlottery.network.sport

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

const val SPORT_MENU = "/api/front/sport/menu"

interface SportService {

    @Headers("x-session-platform-code:plat1")
    @GET(SPORT_MENU)
    suspend fun getMenu(
        @Header("x-session-token") token: String
    ): Response<SportMenuResult>
}