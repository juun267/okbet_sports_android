package org.cxct.sportlottery.network.uploadImg

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.repository.PLATFORM_CODE
import org.cxct.sportlottery.repository.PROJECT_CODE
import java.io.File

class UploadImgRequest(val userId: String, private val file: File,val platformCodeType:PlatformCodeType) {

    fun toParts(): List<MultipartBody.Part> {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)

        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("projectCode", PROJECT_CODE) //项目代码
            .addFormDataPart("platformCode", BuildConfig.CHANNEL_NAME) //平台代码
            .addFormDataPart("userId", userId)
            .addFormDataPart("expireAfterDays", "0") //多久过期（天）0：不过期，N：N天后过期（1<=N<=365）
            .addFormDataPart("file", file.name, requestFile)
            .build().parts
    }


    enum class PlatformCodeType(val code: String) {
        AVATAR("img"),
        VOUCHER("voucher")
    }

}