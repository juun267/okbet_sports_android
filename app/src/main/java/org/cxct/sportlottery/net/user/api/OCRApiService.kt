package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.interceptor.HEADER_UPLOAD_IMG
import org.cxct.sportlottery.network.interceptor.KEY_BASE_URL
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import retrofit2.Response
import retrofit2.http.*

interface OCRApiService {

    @POST(Constants.OCR_INFO)
    suspend fun getOCRInfo(@Body params : JsonObject) : ApiResult<OCRInfo>

    @Multipart
    @POST(Constants.OCR_INFO_BY_HUAWEI)
    suspend fun getOCRInfoByHuawei(@Part parts: List<MultipartBody.Part>): ApiResult<OCRInfo>

}