package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.network.Constants.INDEX_SENDCODE
import org.cxct.sportlottery.network.Constants.INDEX_VERIFYORRESET
import org.cxct.sportlottery.network.Constants.SEND_EMAIL_FORGET
import retrofit2.http.Body
import retrofit2.http.POST

//用户账户相关Api
interface UserApiService {

    @POST(SEND_EMAIL_FORGET)
    suspend fun sendEmailForget(@Body params: Map<String, String>): ApiResult<SendCodeRespnose>

    @POST(INDEX_SENDCODE)
    suspend fun sendCode(@Body params: JsonObject): ApiResult<SendCodeRespnose>

    @POST(INDEX_VERIFYORRESET)
    suspend fun verifyOrResetInfo(@Body params: JsonObject): ApiResult<SendCodeRespnose>
}