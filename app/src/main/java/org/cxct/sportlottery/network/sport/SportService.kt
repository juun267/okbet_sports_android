package org.cxct.sportlottery.network.sport

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface SportService {

    companion object {
        const val sport_menu = "/api/front/sport/menu"
    }

    @Headers("x-session-platform-code:plat1")
    @GET(sport_menu)
    suspend fun getMenu(
        @Header("x-session-token") token: String
    ): Response<SportMenuResult>
}