package org.cxct.sportlottery.net.user.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import retrofit2.http.Body
import retrofit2.http.POST

//用户账户相关Api
interface UserApiService {

    @POST("/api/front/index/sendEmailCode")
    suspend fun sendEmailForget(@Body params: Map<String, String>): ApiResult<SendCodeRespnose>

}