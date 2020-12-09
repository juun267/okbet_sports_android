package org.cxct.sportlottery.network.index

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface IndexService {

    @Headers("x-session-platform-code:plat1")
    @POST("/api/front/index/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}