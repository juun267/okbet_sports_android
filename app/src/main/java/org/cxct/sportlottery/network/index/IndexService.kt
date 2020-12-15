package org.cxct.sportlottery.network.index

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IndexService {

    companion object {
        const val index_login = "/api/front/index/login"
    }

    @Headers("x-session-platform-code:plat1")
    @POST(index_login)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>
}