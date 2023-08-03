package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.network.Constants.ACTIVITY_APPLY
import org.cxct.sportlottery.network.Constants.ACTIVITY_DETAIL_H5
import org.cxct.sportlottery.network.Constants.ACTIVITY_IMAGELIST_H5
import org.cxct.sportlottery.network.Constants.INDEX_SENDCODE
import org.cxct.sportlottery.network.Constants.INDEX_VERIFYORRESET
import org.cxct.sportlottery.network.Constants.SEND_EMAIL_FORGET
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

//用户账户相关Api
interface UserApiService {

    @POST(SEND_EMAIL_FORGET)
    suspend fun sendEmailForget(@Body params: Map<String, String>): ApiResult<SendCodeRespnose>

    @POST(INDEX_SENDCODE)
    suspend fun sendCode(@Body params: JsonObject): ApiResult<SendCodeRespnose>

    @POST(INDEX_VERIFYORRESET)
    suspend fun verifyOrResetInfo(@Body params: JsonObject): ApiResult<SendCodeRespnose>

    @GET(ACTIVITY_IMAGELIST_H5)
    suspend fun activityImageListH5(): ApiResult<List<ActivityImageList>>

    @GET(ACTIVITY_DETAIL_H5)
    suspend fun activityDetailH5(@Path("activityId") activityId: String): ApiResult<ActivityImageList>

    @GET(ACTIVITY_APPLY)
    suspend fun activityApply(@Path("activityId") activityId: String): ApiResult<String>


}