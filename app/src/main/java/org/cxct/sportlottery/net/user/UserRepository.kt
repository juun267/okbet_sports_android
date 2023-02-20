package org.cxct.sportlottery.net.user

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.user.api.UserApiService
import org.cxct.sportlottery.net.user.data.SendCodeRespnose

object UserRepository {

    val userApi by lazy { RetrofitHolder.createApiService(UserApiService::class.java) }

    suspend fun sendEmailForget(email: String, validCodeIdentity :String, validCode: String): ApiResult<SendCodeRespnose> {
        val params = mapOf("email" to email, "validCodeIdentity" to validCodeIdentity, "validCode" to validCode)
        return userApi.sendEmailForget(params)
    }
}