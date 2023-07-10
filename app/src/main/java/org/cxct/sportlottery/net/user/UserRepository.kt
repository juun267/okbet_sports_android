package org.cxct.sportlottery.net.user

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.user.api.UserApiService
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.net.user.data.UserBasicInfoResponse
import org.cxct.sportlottery.network.index.config.ImageData

object UserRepository {

    val userApi by lazy { RetrofitHolder.createApiService(UserApiService::class.java) }

    suspend fun sendEmailForget(email: String, validCodeIdentity :String, validCode: String): ApiResult<SendCodeRespnose> {
        val params = mapOf("email" to email, "validCodeIdentity" to validCodeIdentity, "validCode" to validCode)
        return userApi.sendEmailForget(params)
    }

    // 通过邮箱验或者手机证码方式重置手机号或者邮箱前的邮箱验证码发送
    suspend fun sendEmailOrPhoneCode(emailOrPhone: String,
                                     validCodeIdentity :String,
                                     validCode: String): ApiResult<SendCodeRespnose> {
        val params = JsonObject()
        params.addProperty("verificationMethodValue", emailOrPhone)
        params.addProperty("resetPhase", false)
        params.addProperty("code", validCode)
        params.addProperty("codeIdentity", validCodeIdentity)
        params.addProperty("verificationMethod", if (emailOrPhone.contains("@")) "EMAIL" else "PHONE")
        return userApi.sendCode(params)
    }

    suspend fun verifyEmailOrPhoneCode(emailOrPhone: String,
                                  code: String): ApiResult<SendCodeRespnose> {
        return resetEmailOrPhone(emailOrPhone, code, false)
    }

    // 修改(或者第一次绑定)手机号或者邮箱
    suspend fun resetEmailOrPhone(emailOrPhone: String,
                                  validCode: String,
                                  resetPhase: Boolean = true): ApiResult<SendCodeRespnose> {
        val params = JsonObject()
        params.addProperty("verificationMethodValue", emailOrPhone)
        params.addProperty("resetPhase", resetPhase)
        params.addProperty("code", validCode)
        params.addProperty("verificationMethod", if (emailOrPhone.contains("@")) "EMAIL" else "PHONE")
        return userApi.verifyOrResetInfo(params)
    }

    suspend fun activityImageListH5(): ApiResult<List<ActivityImageList>> {
        return userApi.activityImageListH5()
    }

}