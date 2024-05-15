package org.cxct.sportlottery.network.index

import org.cxct.sportlottery.net.user.data.UserBasicInfoResponse
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.BIND_FACEBOOK
import org.cxct.sportlottery.network.Constants.BIND_GOOGLE
import org.cxct.sportlottery.network.Constants.CANCEL_ACCOUNT
import org.cxct.sportlottery.network.Constants.FACEBOOK_LOGIN
import org.cxct.sportlottery.network.Constants.FORGET_PASSWORD_SMS
import org.cxct.sportlottery.network.Constants.FORGET_PASSWORD_VALIDATE_EMAIL
import org.cxct.sportlottery.network.Constants.GOOGLE_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_TOKEN
import org.cxct.sportlottery.network.Constants.INDEX_CONFIG
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.INDEX_REGISTER
import org.cxct.sportlottery.network.Constants.INDEX_SEND_LOGIN_DEVICE_SMS
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_CODE
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_LOGIN_DEVICE_SMS
import org.cxct.sportlottery.network.Constants.LOGIN
import org.cxct.sportlottery.network.Constants.LOGIN_FOR_GUEST
import org.cxct.sportlottery.network.Constants.LOGIN_OR_REG
import org.cxct.sportlottery.network.Constants.LOGIN_OR_REG_SEND_VALIDCODE
import org.cxct.sportlottery.network.Constants.REG_PLATFORM_USER
import org.cxct.sportlottery.network.Constants.RESET_FORGET_PASSWORD
import org.cxct.sportlottery.network.Constants.RESET_FORGET_PASSWORD_BY_EMAIL
import org.cxct.sportlottery.network.Constants.SEND_EMAIL_FORGET
import org.cxct.sportlottery.network.Constants.SEND_SMS_FORGET
import org.cxct.sportlottery.network.Constants.USER_BASIC_INFO_UPDATE
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.forgetPassword.*
import org.cxct.sportlottery.network.index.login.*
import org.cxct.sportlottery.network.index.login_for_guest.LoginForGuestRequest
import org.cxct.sportlottery.network.index.logout.LogoutRequest
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.network.user.UserSwitchResult
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.network.user.info.UserBasicInfoRequest
import org.cxct.sportlottery.network.user.info.UserSalaryListResult
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountResult
import retrofit2.Response
import retrofit2.http.*


interface IndexService {

    @POST(LOGIN)
    suspend fun login(@Body params: LoginRequest): Response<LoginResult>

    @POST(INDEX_SEND_LOGIN_DEVICE_SMS)
    suspend fun sendLoginDeviceSms(@Header("x-session-token") token: String): Response<NetResult>

    @POST(INDEX_VALIDATE_LOGIN_DEVICE_SMS)
    suspend fun validateLoginDeviceSms(
        @Header("x-session-token") token: String,
        @Body validateLoginDeviceSmsRequest: ValidateLoginDeviceSmsRequest,
    ): Response<NetResult>

    @POST(INDEX_LOGOUT)
    suspend fun logout(@Header("x-session-token") token: String): Response<NetResult>

    @POST(USER_BASIC_INFO_UPDATE)
    suspend fun commitUserBasicInfo(@Body infoRequest: UserBasicInfoRequest): Response<NetResult>


    @GET(INDEX_CONFIG)
    suspend fun getConfig(): Response<ConfigResult>

    @POST(INDEX_VALIDATE_CODE)
    suspend fun getValidCode(@Body validCodeRequest: ValidCodeRequest): Response<ValidCodeResult>

    @POST(INDEX_REGISTER)
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResult>

    @GET(Constants.USER_BASIC_INFO_SWITCH)
    suspend fun getUserInfoSwitch(): Response<UserSwitchResult>

    @GET(Constants.USER_SALARY_SOURCE_LIST)
    suspend fun getUserSalaryList(): Response<UserSalaryListResult>

    @GET(Constants.USER_GET_BASIC_INFO)
    suspend fun getUserBasicInfo(): Response<UserBasicInfoResponse>

    @GET(Constants.USER_BASIC_INFO_CHECK)
    suspend fun getUserInfoCheck(): Response<UserSwitchResult>


    @POST(INDEX_CHECK_TOKEN)
    suspend fun checkToken(): Response<NetResult>

    @POST(LOGIN_FOR_GUEST)
    suspend fun loginForGuest(@Body loginForGuestRequest: LoginForGuestRequest): Response<LoginResult>

    @POST(CANCEL_ACCOUNT)
    suspend fun cancelAccount(@Path("password") password: String ):Response<CancelAccountResult>

    @POST(FORGET_PASSWORD_SMS)
    suspend fun forgetPasswordSMS(@Body smsRequest: ForgetPasswordSmsRequest): Response<NetResult>

    @POST(RESET_FORGET_PASSWORD)
    suspend fun resetPassWord(@Body resetPasswordRequest: ResetPasswordRequest): Response<ResetPasswordResult>

    @POST(RESET_FORGET_PASSWORD_BY_EMAIL)
    suspend fun resetPassWordByEmail(@Body resetPasswordRequest: ResetPasswordRequest): Response<ResetPasswordResult>

    @POST(SEND_SMS_FORGET)
    suspend fun sendSmsForget(@Body sendSmsRequest: SendSmsRequest): Response<SendSmsResult>

    @POST(SEND_EMAIL_FORGET)
    suspend fun sendEmailForget(@Body params: Map<String, String>): Response<SendSmsResult>

    @POST(FORGET_PASSWORD_VALIDATE_EMAIL)
    suspend fun validateEmailCode(@Body params: Map<String, String>): Response<NetResult>


    /**
     * "account":"3vbekk",
    "password":"cygnc5",
    "validCodeIdentity":"7bv66q",
    "validCode":"34535",
    "SecurityCode":"34535",
    "loginSrc":797,
    "deviceSn":"4o4je0",
    "appVersion":"9.1",
    "loginEnvInfo":"417koq",
    "currency":"lfblej",
    "imOpenId":"34",
    "googleCode":"34535",
    "useCodeLogin":true
     */
    @POST(LOGIN_OR_REG)
    suspend fun loginOrReg(@Body loginRequest: LoginRequest): Response<LoginResult>


    @POST(LOGIN_OR_REG_SEND_VALIDCODE)
    suspend fun loginOrRegSendValidCode(@Body loginCodeRequest: LoginCodeRequest): Response<NetResult>

    @POST(FACEBOOK_LOGIN)
    suspend fun facebookLogin(@Body loginTokenRequest: LoginTokenRequest): Response<LoginResult>


    @POST(GOOGLE_LOGIN)
    suspend fun googleLogin(@Body loginTokenRequest: LoginTokenRequest): Response<LoginResult>

    @POST(Constants.INDEX_CHECK_EXIST_NEW)
    suspend fun checkUserExist(@Body checkUserRequest: CheckUserRequest): Response<CheckAccountResult>

    @POST(BIND_GOOGLE)
    suspend fun bindGoogle(@Body loginTokenRequest: LoginTokenRequest): Response<AuthBindResult>

    @POST(BIND_FACEBOOK)
    suspend fun bindFacebook(@Body loginTokenRequest: LoginTokenRequest): Response<AuthBindResult>

    @POST(REG_PLATFORM_USER)
    suspend fun regPlatformUser(
        @Header("x-session-token") token: String,
        @Body loginRequest: LoginRequest): Response<LoginResult>

}