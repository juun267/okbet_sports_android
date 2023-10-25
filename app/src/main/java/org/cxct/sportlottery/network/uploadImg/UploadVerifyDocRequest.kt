package org.cxct.sportlottery.network.uploadImg

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.repository.PLATFORM_CODE
import org.cxct.sportlottery.repository.PROJECT_CODE
import java.io.File

class UploadVerifyDocRequest(val userId: String, private val docFile: File) {
    fun toPars(): List<MultipartBody.Part> {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestFile = docFile.asRequestBody(mediaType)

        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("projectCode", PROJECT_CODE) //项目代码
            .addFormDataPart("platformCode", BuildConfig.CHANNEL_NAME) //平台代码
            .addFormDataPart("userId", userId)
            .addFormDataPart("expireAfterDays", "0") //多久过期（天）0：不过期，N：N天后过期（1<=N<=365）
            .addFormDataPart("file", docFile.name, requestFile)
            .build().parts
    }
}