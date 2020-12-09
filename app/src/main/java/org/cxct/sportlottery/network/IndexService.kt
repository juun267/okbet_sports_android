package org.cxct.sportlottery.network

import retrofit2.http.Body
import retrofit2.http.POST


interface IndexService {

    @POST("/index/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}