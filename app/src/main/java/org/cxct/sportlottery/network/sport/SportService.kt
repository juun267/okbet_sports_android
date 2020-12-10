package org.cxct.sportlottery.network.sport


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface SportService {
    @Headers("x-session-platform-code:plat1")
    @GET("/api/front/sport/menu")
    suspend fun getMenu(
        @Header("x-session-token") token: String
    ): SportMenuResponse
}