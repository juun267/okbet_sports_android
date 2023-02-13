package org.cxct.sportlottery.network.index

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.CANCEL_ACCOUNT
import org.cxct.sportlottery.network.Constants.FORGET_PASSWORD_SMS
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_TOKEN
import org.cxct.sportlottery.network.Constants.INDEX_CONFIG
import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.INDEX_REGISTER
import org.cxct.sportlottery.network.Constants.INDEX_SEND_LOGIN_DEVICE_SMS
import org.cxct.sportlottery.network.Constants.INDEX_SEND_SMS
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_CODE
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_LOGIN_DEVICE_SMS
import org.cxct.sportlottery.network.Constants.LOGIN_FOR_GUEST
import org.cxct.sportlottery.network.Constants.RESET_FORGET_PASSWORD
import org.cxct.sportlottery.network.Constants.SEND_SMS_FORGET
import org.cxct.sportlottery.network.Constants.VALIDATE_USER
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.checktoken.CheckTokenResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.forgetPassword.*
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.login.ValidateLoginDeviceSmsRequest
import org.cxct.sportlottery.network.index.login_for_guest.LoginForGuestRequest
import org.cxct.sportlottery.network.index.logout.LogoutRequest
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.register.RegisterRequest
import org.cxct.sportlottery.network.index.sendSms.SmsRequest
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountResult
import retrofit2.Response
import retrofit2.http.*


interface IndexService {

    @POST(INDEX_LOGIN)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>

    @POST(INDEX_SEND_LOGIN_DEVICE_SMS)
    suspend fun sendLoginDeviceSms(@Header("x-session-token") token: String): Response<LogoutResult>

    @POST(INDEX_VALIDATE_LOGIN_DEVICE_SMS)
    suspend fun validateLoginDeviceSms(
        @Header("x-session-token") token: String,
        @Body validateLoginDeviceSmsRequest: ValidateLoginDeviceSmsRequest,
    ): Response<LogoutResult>

    @POST(INDEX_LOGOUT)
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<LogoutResult>

    @GET(INDEX_CONFIG)
    suspend fun getConfig(): Response<ConfigResult>

    @POST(INDEX_VALIDATE_CODE)
    suspend fun getValidCode(@Body validCodeRequest: ValidCodeRequest): Response<ValidCodeResult>

    @POST(INDEX_REGISTER)
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResult>

    @POST(INDEX_SEND_SMS)
    suspend fun sendSms(@Body smsRequest: SmsRequest): Response<SmsResult>

    @GET(Constants.INDEX_CHECK_EXIST)
    suspend fun checkAccountExist(@Path("userName") userName: String): Response<CheckAccountResult>

    @POST(INDEX_CHECK_TOKEN)
    suspend fun checkToken(): Response<CheckTokenResult>

    @POST(LOGIN_FOR_GUEST)
    suspend fun loginForGuest(@Body loginForGuestRequest: LoginForGuestRequest): Response<LoginResult>

    @POST(CANCEL_ACCOUNT)
    suspend fun cancelAccount(@Path("password") password: String ):Response<CancelAccountResult>

    @POST(FORGET_PASSWORD_SMS)
    suspend fun forgetPasswordSMS(@Body smsRequest: ForgetPasswordSmsRequest): Response<ForgetSmsResult>
    @POST(RESET_FORGET_PASSWORD)
    suspend fun resetPassWord(@Body resetPasswordRequest: ResetPasswordRequest): Response<ResetPasswordResult>
    @POST(SEND_SMS_FORGET)
    suspend fun sendSmsForget(@Body sendSmsRequest: SendSmsRequest): Response<SendSmsResult>

    @POST(VALIDATE_USER)
    suspend fun checkValidateUser(@Body sendSmsRequest: ValidateUserRequest): Response<ValidateUserResult>

}