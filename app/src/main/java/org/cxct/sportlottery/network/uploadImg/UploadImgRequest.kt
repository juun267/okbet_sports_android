package org.cxct.sportlottery.network.uploadImg

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.cxct.sportlottery.repository.PLATFORM_CODE
import org.cxct.sportlottery.repository.PROJECT_CODE
import org.cxct.sportlottery.repository.sLoginData
import java.io.File

class UploadImgRequest(val userId: String, val path: String, ) {

    fun toParts(): List<MultipartBody.Part> {
        val file = File(path)
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)

        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("projectCode", PROJECT_CODE) //项目代码
            .addFormDataPart("platformCode", PLATFORM_CODE) //平台代码
            .addFormDataPart("userId", sLoginData?.userId.toString())
            .addFormDataPart("expireAfterDays", "0") //多久过期（天）0：不过期，N：N天后过期（1<=N<=365）
            .addFormDataPart("file", file.name, requestFile)
            .build().parts
    }

}