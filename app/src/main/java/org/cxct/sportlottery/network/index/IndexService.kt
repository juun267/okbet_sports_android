package org.cxct.sportlottery.network.index

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


const val INDEX_LOGIN = "/api/front/index/login"

interface IndexService {

    @Headers("x-session-platform-code:plat1")
    @POST(INDEX_LOGIN)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>
}