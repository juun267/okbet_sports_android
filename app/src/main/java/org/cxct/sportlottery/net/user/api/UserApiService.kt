package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageData
import org.cxct.sportlottery.net.user.data.*
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.net.user.data.VerifyConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.ACTIVITY_APPLY
import org.cxct.sportlottery.network.Constants.ACTIVITY_CATEGORY_LIST
import org.cxct.sportlottery.network.Constants.ACTIVITY_DETAIL_H5
import org.cxct.sportlottery.network.Constants.ACTIVITY_IMAGELIST_H5
import org.cxct.sportlottery.network.Constants.ACTIVITY_RECORD
import org.cxct.sportlottery.network.Constants.CHECKSAFEQUEST
import org.cxct.sportlottery.network.Constants.GET_KYC_NEED_INFORMATION
import org.cxct.sportlottery.network.Constants.GET_USER_SAFEQUESTION
import org.cxct.sportlottery.network.Constants.INDEX_SENDCODE
import org.cxct.sportlottery.network.Constants.INDEX_VERIFYORRESET
import org.cxct.sportlottery.network.Constants.INVITE_USER_DETAIL
import org.cxct.sportlottery.network.Constants.LIVE_KYC_VERIFY
import org.cxct.sportlottery.network.Constants.LOGIN
import org.cxct.sportlottery.network.Constants.LOGIN_BY_SAFEQUESTION
import org.cxct.sportlottery.network.Constants.LOGIN_CHECK_NEED_CODE
import org.cxct.sportlottery.network.Constants.QUERY_SAFEQUESTION_TYPE
import org.cxct.sportlottery.network.Constants.REVERIFY
import org.cxct.sportlottery.network.Constants.RRESET_WITHDRAW
import org.cxct.sportlottery.network.Constants.SEND_EMAIL_FORGET
import org.cxct.sportlottery.network.Constants.SETBIRTHDAY
import org.cxct.sportlottery.network.Constants.SET_SAFEQUESTION
import org.cxct.sportlottery.network.Constants.UPLOAD_REVIEW_PHOTO
import org.cxct.sportlottery.network.Constants.USER_VERIFY_CONFIG
import org.cxct.sportlottery.network.Constants.SET_USERNAME
import org.cxct.sportlottery.network.Constants.UPDATE_PASSWORD_BY_SAFEQUESTION
import org.cxct.sportlottery.network.Constants.UPLOAD_VERIFY_PHOTO
import org.cxct.sportlottery.network.Constants.VIP_DETAIL
import org.cxct.sportlottery.network.Constants.VIP_REWARD
import org.cxct.sportlottery.network.Constants.VIP_UNIREDENP_APPLY
import org.cxct.sportlottery.network.Constants.VIP_USER
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.network.index.login.LoginRequest
import retrofit2.http.*

//用户账户相关Api
interface UserApiService {

    @POST(SEND_EMAIL_FORGET)
    suspend fun sendEmailForget(@Body params: Map<String, String>): ApiResult<SendCodeRespnose>

    @POST(INDEX_SENDCODE)
    suspend fun sendCode(@Body params: JsonObject): ApiResult<SendCodeRespnose>

    @POST(INDEX_VERIFYORRESET)
    suspend fun verifyOrResetInfo(@Body params: JsonObject): ApiResult<SendCodeRespnose>

    @GET(ACTIVITY_IMAGELIST_H5)
    suspend fun activityImageListH5(@QueryMap categoryId: Map<String, String>): ApiResult<List<ActivityImageList>>

    @GET(ACTIVITY_CATEGORY_LIST)
    suspend fun activityCategoryList(): ApiResult<List<ActivityCategory>>

    @GET(ACTIVITY_DETAIL_H5)
    suspend fun activityDetailH5(@Path("activityId") activityId: String): ApiResult<ActivityImageList>

    @GET(ACTIVITY_APPLY)
    suspend fun activityApply(@Path("activityId") activityId: String): ApiResult<String>

    @POST(ACTIVITY_RECORD)
    suspend fun activityRecord(@Body params : JsonObject): ApiResult<PageData<RewardRecord>>

    @GET(USER_VERIFY_CONFIG)
    suspend fun getVerifyConfig(): ApiResult<VerifyConfig>

    @POST(UPLOAD_REVIEW_PHOTO)
    suspend fun uploadReviewPhoto(@Body params: Map<String, String>): ApiResult<String>

    @POST(SET_USERNAME)
    suspend fun changeUserName(@Body params: Map<String, String>): ApiResult<String>

    @POST(LOGIN_CHECK_NEED_CODE)
    suspend fun checkUserLoginNeedCode(@Body params: Map<String, String>): ApiResult<Boolean>

    @POST(LOGIN)
    suspend fun login(@Body params: LoginRequest): ApiResult<LoginData>

    @POST(Constants.VERIFY_SMS_CODE)
    suspend fun verifySMSCode(@Body params: Map<String, String>): ApiResult<String>

    @POST(RRESET_WITHDRAW)
    suspend fun resetWithdraw(@Body params : JsonObject) : ApiResult<String>

    @POST(UPLOAD_VERIFY_PHOTO)
    suspend fun uploadKYCInfo(@Body params : JsonObject): ApiResult<String>

    @GET(REVERIFY)
    suspend fun reVerify(): ApiResult<String?>

    @GET(VIP_USER)
    suspend fun getUserVip(): ApiResult<UserVip>

    @GET(VIP_DETAIL)
    suspend fun getVipDetail(): ApiResult<VipDetail>

    @POST(VIP_REWARD)
    suspend fun vipReward(@Body params : JsonObject): ApiResult<String>

    @POST(VIP_UNIREDENP_APPLY)
    suspend fun vipRedenpApply(@Body params : JsonObject): ApiResult<VipRedenpApplyResult>

    @POST(SETBIRTHDAY)
    suspend fun setBirthday(@Body params : JsonObject): ApiResult<String>

    @GET(INVITE_USER_DETAIL)
    suspend fun inviteUserDetail(): ApiResult<InviteUserDetail>

    @POST(GET_USER_SAFEQUESTION)
    suspend fun getUserSafeQuestion(@Body params : JsonObject): ApiResult<CheckSafeQuestionResp>

    @GET(QUERY_SAFEQUESTION_TYPE)
    suspend fun querySafeQuestionType(): ApiResult<List<SafeQuestion>>

    @POST(SET_SAFEQUESTION)
    suspend fun setSafeQuestion(@Body params : JsonObject): ApiResult<String>

    @POST(CHECKSAFEQUEST)
    suspend fun checkSafeQuest(@Body params : JsonObject): ApiResult<CheckSafeQuestionResp>

    @POST(UPDATE_PASSWORD_BY_SAFEQUESTION)
    suspend fun updatePasswordBySafeQuestion(@Body params : JsonObject): ApiResult<ForgetPasswordResp>

    @POST(LOGIN_BY_SAFEQUESTION)
    suspend fun loginBySafeQuestion(@Body params : LoginRequest): ApiResult<List<LoginData>>

    @POST(GET_KYC_NEED_INFORMATION)
    suspend fun getKycNeedInformation(): ApiResult<KYCVerifyConfig>

    @POST(LIVE_KYC_VERIFY)
    suspend fun liveKycVerify(@Body params : JsonObject): ApiResult<LiveKycVerifyResult>


}