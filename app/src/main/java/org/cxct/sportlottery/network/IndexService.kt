package org.cxct.sportlottery.network

import retrofit2.http.Body
import retrofit2.http.POST


interface IndexService {

    @POST("/api/front/index/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}