package org.cxct.sportlottery.network.user

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.LOCK_MONEY
import org.cxct.sportlottery.network.Constants.ODDS_CHANGE_OPTION
import org.cxct.sportlottery.network.Constants.PASSWORD_VERIFY
import org.cxct.sportlottery.network.Constants.USER_BET_LIMIT
import org.cxct.sportlottery.network.Constants.USER_EDIT_ICON_URL
import org.cxct.sportlottery.network.Constants.USER_EDIT_NICKNAME
import org.cxct.sportlottery.network.Constants.USER_FROZE
import org.cxct.sportlottery.network.Constants.USER_INFO
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.Constants.USER_UPDATE_FUND_PWD
import org.cxct.sportlottery.network.Constants.USER_UPDATE_PWD
import org.cxct.sportlottery.network.Constants.USER_WITHDRAW_INFO
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.bettingStation.AreaAllResult
import org.cxct.sportlottery.network.user.iconUrl.IconUrlRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.network.user.passwordVerify.PasswordVerifyRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeResult
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitRequest
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitResult
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoRequest
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoResult
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.ui.profileCenter.profile.UserInfoDetailsEntity
import org.cxct.sportlottery.ui.profileCenter.profile.WorkNameEntity
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
    suspend fun editNickname(@Body nicknameRequest: NicknameRequest): Response<NetResult>

    @POST(USER_EDIT_ICON_URL)
    suspend fun editIconUrl(@Body editIconUrlRequest: IconUrlRequest): Response<IconUrlResult>

    @POST(USER_UPDATE_PWD)
    suspend fun updatePwd(@Body updatePwdRequest: UpdatePwdRequest): Response<NetResult>

    @POST(USER_UPDATE_FUND_PWD)
    suspend fun updateFundPwd(@Body updateFundPwdRequest: UpdateFundPwdRequest): Response<NetResult>

    @POST(USER_WITHDRAW_INFO)
    suspend fun setWithdrawUserInfo(@Body withdrawInfoRequest: WithdrawInfoRequest): Response<WithdrawInfoResult>

    @POST(USER_BET_LIMIT)
    suspend fun setPerBetLimit(@Body perBetLimitRequest: PerBetLimitRequest): Response<PerBetLimitResult>

    @POST(USER_FROZE)
    suspend fun froze(@Body frozeRequest: FrozeRequest): Response<FrozeResult>

    @GET(LOCK_MONEY)
    suspend fun lockMoney(): Response<UserMoneyResult>

    @POST(PASSWORD_VERIFY)
    suspend fun passwordVerify(@Body passwordVerifyRequest: PasswordVerifyRequest): Response<NetResult>

    @POST(ODDS_CHANGE_OPTION)
    suspend fun oddsChangeOption(@Body oddsChangeOption : OddsChangeOptionRequest) : Response<NetResult>

    @POST(Constants.AREA_UNIVERSAL)
    suspend fun getAreaUniversal(): Response<AreaAllResult>

    @GET(Constants.WORKS_QUERYALL)
    suspend fun getWorksQueryAll(): Response<WorkNameEntity>

    @GET(Constants.USER_QUERYUSERINFODETAILS)
    suspend fun userQueryUserInfoDetails(): Response<UserInfoDetailsEntity>

    @POST(Constants.USER_COMPLETEUSERDETAILS)
    suspend fun userCompleteUserDetails(
        @Body uide: Uide
    ): Response<NetResult>

}