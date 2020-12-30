package org.cxct.sportlottery.network.user

import org.cxct.sportlottery.network.Constants.USER_MONEY
import retrofit2.Response
import retrofit2.http.GET

interface UserService {

    @GET(USER_MONEY)
    suspend fun getMoney(): Response<UserMoneyResult>
}