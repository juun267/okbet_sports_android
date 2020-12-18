package org.cxct.sportlottery.network.index

import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface IndexService {

    @Headers("x-session-platform-code:plat1")
    @POST(INDEX_LOGIN)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>
}