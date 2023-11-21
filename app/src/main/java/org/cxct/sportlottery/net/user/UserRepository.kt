package org.cxct.sportlottery.net.user

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.user.api.OCRApiService
import org.cxct.sportlottery.net.user.api.UserApiService
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.net.user.data.VerifyConfig
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.MD5Util
import retrofit2.http.Body

object UserRepository {

    val userApi by lazy { RetrofitHolder.createApiService(UserApiService::class.java) }
    val ocrApi by lazy { RetrofitHolder.createOCRApiService(OCRApiService::class.java) }

    suspend fun sendEmailForget(email: String, validCodeIdentity :String, validCode: String): ApiResult<SendCodeRespnose> {
        val params = mutableMapOf("email" to email).apply {
            if (sConfigData?.captchaType==1){
                put("ticket",validCodeIdentity)
                put("randstr",validCode)
            }else{
                put("validCodeIdentity",validCodeIdentity)
                put("validCode",validCode)
            }
        }
        return userApi.sendEmailForget(params)
    }

    // 通过邮箱验或者手机证码方式重置手机号或者邮箱前的邮箱验证码发送
    suspend fun sendEmailOrPhoneCode(emailOrPhone: String,
                                     validCodeIdentity :String,
                                     validCode: String): ApiResult<SendCodeRespnose> {
        val params = JsonObject()
        params.addProperty("verificationMethodValue", emailOrPhone)
        params.addProperty("resetPhase", false)
        if(sConfigData?.captchaType==1){
            params.addProperty("ticket", validCode)
            params.addProperty("randstr", validCodeIdentity)
        }else{
            params.addProperty("code", validCode)
            params.addProperty("codeIdentity", validCodeIdentity)
        }
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
    suspend fun activityDetailH5(activityId: String): ApiResult<ActivityImageList> {
        return userApi.activityDetailH5(activityId)
    }
    suspend fun activityApply(activityId: String): ApiResult<String> {
        return userApi.activityApply(activityId)
    }

    suspend fun getVerifyConfig():ApiResult<VerifyConfig> = userApi.getVerifyConfig()

    suspend fun uploadReviewPhoto(selfiePicture: String?, wealthProof: String?): ApiResult<String>{
        val params = mutableMapOf<String, String>()
        selfiePicture?.let { params["selfiePicture"] = it}
        wealthProof?.let { params["wealthProof"] = it}
        return userApi.uploadReviewPhoto(params.toMap())
    }

    suspend fun changeUserName(firstName: String, middelName: String, lastName: String): ApiResult<String>  {
        val params = mutableMapOf<String, String>()
        params["firstName"] = firstName
        params["middleName"] = middelName
        params["lastName"] = lastName
        params["fullName"] = "$firstName $middelName $lastName"
        return userApi.changeUserName(params)
    }

    suspend fun checkUserLoginNeedCode(account: String, password: String): ApiResult<Boolean> {
        val params = mutableMapOf<String, String>()
        params["account"] = account
        params["password"] = password
        params["loginSrc"] = "$LOGIN_SRC"
        return userApi.checkUserLoginNeedCode(params)
    }

    suspend fun login(@Body params: LoginRequest): ApiResult<LoginData> {
        return userApi.login(params)
    }

    suspend fun verifySMSCode(phone: String, smsCode: String): ApiResult<String> {
        return userApi.verifySMSCode(mapOf("phone" to phone, "securityCode" to smsCode))
    }

    suspend fun resetWithdraw(newPassword: String): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("userId", UserInfoRepository.userId())
        params.addProperty("platformId", sConfigData?.platformId ?: 1)
        params.addProperty("newPassword", MD5Util.MD5Encode(newPassword))
        return userApi.resetWithdraw(params)
    }

    suspend fun getOCRInfo(idType: Int, imgUrl: String): ApiResult<OCRInfo> {
        val params = JsonObject()
        params.addProperty("ocrTypeId", idType)
        params.addProperty("imageUrl", sConfigData?.resServerHost + imgUrl)
        return ocrApi.getOCRInfo(params)
    }

    suspend fun uploadKYCInfo(idType: Int, idNumber: String?, idImageUrl: String,
                              firstName: String, middleName: String, lastName: String, birthday: String): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("identityType", idType)
        params.addProperty("identityNumber", "$idNumber")
        params.addProperty("identityPhoto", idImageUrl)
        params.addProperty("firstName", firstName)
        params.addProperty("middleName", middleName)
        params.addProperty("lastName", lastName)
        params.addProperty("birthday", birthday)
        return userApi.uploadKYCInfo(params)
    }

}