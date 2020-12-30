package org.cxct.sportlottery.network.index

import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface IndexService {

    @POST(INDEX_LOGIN)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>

    @POST(INDEX_LOGOUT)
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<LogoutResult>
}