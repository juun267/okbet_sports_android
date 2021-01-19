package org.cxct.sportlottery.network.user

import org.cxct.sportlottery.network.Constants.USER_EDIT_NICKNAME
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.sport.SportMenuRequest
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {

    @GET(USER_MONEY)
    suspend fun getMoney(): Response<UserMoneyResult>

    @POST(USER_EDIT_NICKNAME)
    suspend fun editNickname(@Body nicknameRequest: NicknameRequest): Response<NicknameResult>
}