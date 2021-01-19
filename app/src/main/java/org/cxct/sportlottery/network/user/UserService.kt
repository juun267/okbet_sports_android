package org.cxct.sportlottery.network.user

import org.cxct.sportlottery.network.Constants.USER_EDIT_ICON_URL
import org.cxct.sportlottery.network.Constants.USER_EDIT_NICKNAME
import org.cxct.sportlottery.network.Constants.USER_INFO
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.Constants.USER_UPDATE_PWD
import org.cxct.sportlottery.network.user.iconUrl.IconUrlRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    @GET(USER_INFO)
    suspend fun getUserInfo(
        @Query("userId") userId: Int? = null,
        @Query("platformId") platformId: Int? = null
    ): Response<UserInfoResult>

    @GET(USER_MONEY)
    suspend fun getMoney(): Response<UserMoneyResult>

    @POST(USER_EDIT_NICKNAME)
    suspend fun editNickname(@Body nicknameRequest: NicknameRequest): Response<NicknameResult>

    @POST(USER_EDIT_ICON_URL)
    suspend fun editIconUrl(@Body editIconUrlRequest: IconUrlRequest): Response<IconUrlResult>

    @POST(USER_UPDATE_PWD)
    suspend fun updatePwd(@Body updatePwdRequest: UpdatePwdRequest): Response<UpdatePwdResult>

}