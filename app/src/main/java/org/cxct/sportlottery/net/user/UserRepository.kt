package org.cxct.sportlottery.net.user

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageData
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.user.api.OCRApiService
import org.cxct.sportlottery.net.user.api.UserApiService
import org.cxct.sportlottery.net.user.data.*
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.util.JsonUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.appendCaptchaParams
import org.cxct.sportlottery.util.toJson
import retrofit2.http.Body
import java.io.File

object UserRepository {

    val userApi by lazy { RetrofitHolder.createApiService(UserApiService::class.java) }
    val ocrApi by lazy { RetrofitHolder.createOCRApiService(OCRApiService::class.java) }
    var _userVipEvent = MutableLiveData<UserVip?>()


    fun clear() {
        _userVipEvent.postValue(null)
    }

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
        val result = userApi.sendEmailForget(params)
        SensorsEventUtil.getCodeEvent(result.succeeded(), true, if (result.succeeded()) null else result?.msg)
        return result
    }

    // 通过邮箱验或者手机证码方式重置手机号或者邮箱前的'验证码发送'
    suspend fun sendEmailOrPhoneCode(emailOrPhone: String,
                                     validCodeIdentity :String,
                                     validCode: String): ApiResult<SendCodeRespnose> {
        val params = JsonObject()
        params.addProperty("verificationMethodValue", emailOrPhone)
        params.addProperty("resetPhase", false)
        if(sConfigData?.captchaType==1){
            params.addProperty("ticket", validCodeIdentity)
            params.addProperty("randstr", validCode)
        }else{
            params.addProperty("code", validCode)
            params.addProperty("codeIdentity", validCodeIdentity)
        }
        val isEmail = emailOrPhone.contains("@")
        params.addProperty("verificationMethod", if (isEmail) "EMAIL" else "PHONE")
        val result = userApi.sendCode(params)
        SensorsEventUtil.getCodeEvent(result.succeeded(), isEmail, if (result.succeeded()) null else result.msg)
        return result
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

    suspend fun activityCategoryList() = userApi.activityCategoryList()

    suspend fun activityImageListH5(activityCategoryId: Int?): ApiResult<List<ActivityImageList>> {
        val categoryId = mutableMapOf<String, String>()
        activityCategoryId?.let { categoryId.put("activityCategoryId", it.toString()) }
        return userApi.activityImageListH5(categoryId)
    }
    suspend fun activityDetailH5(activityId: String): ApiResult<ActivityImageList> {
        return userApi.activityDetailH5(activityId)
    }
    suspend fun activityApply(activityId: String): ApiResult<String> {
        return userApi.activityApply(activityId)
    }

    suspend fun getVerifyConfig():ApiResult<VerifyConfig> = userApi.getVerifyConfig()

    suspend fun uploadReviewPhoto(selfiePicture: String?, wealthProof: String?, backOfID: String?): ApiResult<String>{
        val params = mutableMapOf<String, String>()
        selfiePicture?.let { params["selfiePicture"] = it}
        wealthProof?.let { params["wealthProof"] = it}
        backOfID?.let { params["backOfID"] = it }
        //新需求为了兼容，固定传version
        params["version"] = "v2"
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

    suspend fun getOCRInfo(ocrTypeId: Int, imgUrl: String): ApiResult<OCRInfo> {
        val params = JsonObject()
        params.addProperty("ocrTypeId", ocrTypeId)
        params.addProperty("imageUrl", sConfigData?.resServerHost + imgUrl)
        return ocrApi.getOCRInfo(params)
    }

    suspend fun getLicense(pageName: String): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("pageName", pageName)
        return ocrApi.getLicense(params)
    }

    suspend fun uploadKYCInfo(idType: Int, idNumber: String?, idImageUrl: String,
                              firstName: String, middleName: String, lastName: String, birthday: String, uide: Uide
    ): ApiResult<String> {
        SensorsEventUtil.submitKYCEvent()
        val params = JsonObject()
        params.addProperty("identityType", idType)
        params.addProperty("identityNumber", "$idNumber")
        params.addProperty("identityPhoto", idImageUrl)
        params.addProperty("firstName", firstName)
        params.addProperty("middleName", middleName)
        params.addProperty("lastName", lastName)
        params.addProperty("birthday", birthday)
        params.addProperty("nationality", uide.nationality)
        params.addProperty("gender", uide.gender)
        params.addProperty("birthplace", uide.placeOfBirth)
        uide.salarySource?.let {
            params.add("income", JsonObject().apply {
                addProperty("id",it.id)
                addProperty("name",it.name)
            })
        }
        params.addProperty("work", uide.natureOfWork)
        params.addProperty("currProvince", uide.province)
        params.addProperty("currCity", uide.city)
        params.addProperty("currAddress", uide.address)
        params.addProperty("currZipCode", uide.zipCode)
        params.addProperty("permanentProvince", uide.permanentProvince)
        params.addProperty("permanentCity", uide.permanentCity)
        params.addProperty("permanentAddress", uide.permanentAddress)
        params.addProperty("permanentZipCode", uide.permanentZipCode)
        //新需求为了兼容，固定传version，请求后用户状态就还是未认证
        params.addProperty("version", "v2")
        return userApi.uploadKYCInfo(params)
    }

    suspend fun getOCRInfoByHuawei(ocrTypeId: Int, file: File, imageUrl: String): ApiResult<OCRInfo> {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)
        val part = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("ocrTypeId", ocrTypeId.toString())
            .addFormDataPart("imageUrl", imageUrl)
            .addFormDataPart("file", file.name, requestFile)
            .build().parts
        return ocrApi.getOCRInfoByHuawei(part)
    }
    suspend fun activityRecord(activityId: String,page: Int,pageSize: Int): ApiResult<PageData<RewardRecord>> {
        val params = JsonObject()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("activityId", activityId)
        return userApi.activityRecord(params)
    }
    suspend fun getGenderL(activityId: String,page: Int,pageSize: Int): ApiResult<PageData<RewardRecord>> {
        val params = JsonObject()
        params.addProperty("page", page)
        params.addProperty("pageSize", pageSize)
        params.addProperty("activityId", activityId)
        return userApi.activityRecord(params)
    }
    suspend fun reVerify(): ApiResult<String?> {
        return userApi.reVerify()
    }
    suspend fun getUserVip(): ApiResult<UserVip> {
        return userApi.getUserVip().apply {
            getData()?.let { _userVipEvent.postValue(it) }
        }
    }
    suspend fun getVipDetail(): ApiResult<VipDetail> {
        return userApi.getVipDetail()
    }
    suspend fun vipReward(activityId: Int, rewardType: Int, levelV2Id: Int): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("activityId", activityId)
        params.addProperty("rewardType", rewardType)
        params.addProperty("levelV2Id", levelV2Id)
        return userApi.vipReward(params)
    }
    suspend fun vipRedenpApply(levelV2Id: Int): ApiResult<VipRedenpApplyResult> {
        val params = JsonObject()
        params.addProperty("levelV2Id", levelV2Id)
        return userApi.vipRedenpApply(params)
    }

    suspend fun setBirthday(birthday: String): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("birthday", birthday)
        return userApi.setBirthday(params)
    }
    suspend fun inviteUserDetail(): ApiResult<InviteUserDetail> {
        return userApi.inviteUserDetail()
    }
    suspend fun getUserSafeQuestion(userName: String, identity: String, validCode: String): ApiResult<CheckSafeQuestionResp> {
        val params = JsonObject()
        params.addProperty("userName", userName)
        params.appendCaptchaParams(identity, validCode)
        return userApi.getUserSafeQuestion(params)
    }

    suspend fun querySafeQuestionType(): ApiResult<List<SafeQuestion>> {
        return userApi.querySafeQuestionType()
    }

    suspend fun setSafeQuestion(safeQuestionType: Int,safeQuestion: String, password: String): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("safeQuestionType", safeQuestionType)
        params.addProperty("safeQuestion", safeQuestion)
        params.addProperty("password", password)
        return userApi.setSafeQuestion(params)
    }

    suspend fun checkSafeQuest(userName: String, safeQuestion: String, identity: String, validCode: String): ApiResult<CheckSafeQuestionResp> {
        val params = JsonObject()
        params.addProperty("userName", userName)
        params.addProperty("safeQuestion", safeQuestion)
        params.appendCaptchaParams(identity, validCode)
        return userApi.checkSafeQuest(params)
    }
    suspend fun updatePasswordBySafeQuestion(userName: String, securityCode: String, newPassword: String): ApiResult<ForgetPasswordResp> {
        val params = JsonObject()
        params.addProperty("userName", userName)
        params.addProperty("securityCode", securityCode)
        params.addProperty("newPassword", newPassword)
        return userApi.updatePasswordBySafeQuestion(params)
    }

    /**
     * account 用户名 loginSrc登录环境 safeQuestion密保问题答案
     */
    suspend fun loginBySafeQuestion(loginRequest: LoginRequest): ApiResult<List<LoginData>> {
        return userApi.loginBySafeQuestion(loginRequest)
    }

    suspend fun getKycNeedInformation(): ApiResult<KYCVerifyConfig> {
        return userApi.getKycNeedInformation()
    }
    suspend fun liveKycVerify(verifyId: String): ApiResult<LiveKycVerifyResult> {
        val params = JsonObject()
        params.addProperty("verifyId", verifyId)
        //活体校验设备类型：1-H5，2-APP端
        params.addProperty("clientType", 2)
        return userApi.liveKycVerify(params)
    }
}