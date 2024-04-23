package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageData
import org.cxct.sportlottery.net.user.data.*
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.net.user.data.VerifyConfig
import org.cxct.sportlottery.net.user.data.WheelActivityInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.ACTIVITY_APPLY
import org.cxct.sportlottery.network.Constants.ACTIVITY_CATEGORY_LIST
import org.cxct.sportlottery.network.Constants.ACTIVITY_DETAIL_H5
import org.cxct.sportlottery.network.Constants.ACTIVITY_IMAGELIST_H5
import org.cxct.sportlottery.network.Constants.ACTIVITY_RECORD
import org.cxct.sportlottery.network.Constants.INDEX_SENDCODE
import org.cxct.sportlottery.network.Constants.INDEX_VERIFYORRESET
import org.cxct.sportlottery.network.Constants.LOGIN
import org.cxct.sportlottery.network.Constants.LOGIN_CHECK_NEED_CODE
import org.cxct.sportlottery.network.Constants.RRESET_WITHDRAW
import org.cxct.sportlottery.network.Constants.SEND_EMAIL_FORGET
import org.cxct.sportlottery.network.Constants.UPLOAD_REVIEW_PHOTO
import org.cxct.sportlottery.network.Constants.USER_VERIFY_CONFIG
import org.cxct.sportlottery.network.Constants.SET_USERNAME
import org.cxct.sportlottery.network.Constants.UPLOAD_VERIFY_PHOTO
import org.cxct.sportlottery.network.Constants.WHEEL_ACTIVITY_INFO
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.interceptor.HEADER_UPLOAD_IMG
import org.cxct.sportlottery.network.interceptor.KEY_BASE_URL
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import retrofit2.Response
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

    @GET(WHEEL_ACTIVITY_INFO)
    suspend fun getWheelActivityInfo(): ApiResult<WheelActivityInfo>

}