package org.cxct.sportlottery.net.upload

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.upload.api.UploadImgService
import org.cxct.sportlottery.network.uploadImg.ImgData
import org.cxct.sportlottery.network.uploadImg.UploadVerifyDocRequest
import org.cxct.sportlottery.repository.LoginRepository
import java.io.File

object UploadImgResposity {

    private val uploadApi by lazy { RetrofitHolder.createApiService(UploadImgService::class.java) }

    suspend fun uploadImge(imgFile: File): ApiResult<ImgData> {
        return uploadApi.uploadImg(UploadVerifyDocRequest(
            LoginRepository.userId.toString(),
            imgFile
        ).toPars())
    }
}