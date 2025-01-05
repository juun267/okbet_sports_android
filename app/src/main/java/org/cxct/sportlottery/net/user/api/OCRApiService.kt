package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.network.Constants
import retrofit2.http.*

interface OCRApiService {

    @POST(Constants.OCR_INFO)
    suspend fun getOCRInfo(@Body params : JsonObject) : ApiResult<OCRInfo>

    @Multipart
    @POST(Constants.OCR_INFO_BY_HUAWEI)
    suspend fun getOCRInfoByHuawei(@Part parts: List<MultipartBody.Part>): ApiResult<OCRInfo>

    @POST(Constants.GET_LICENSE)
    suspend fun getLicense(@Body params : JsonObject) : ApiResult<String>
}