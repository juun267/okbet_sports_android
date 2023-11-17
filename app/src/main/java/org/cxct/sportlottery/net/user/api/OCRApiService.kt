package org.cxct.sportlottery.net.user.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.POST

interface OCRApiService {

    @POST(Constants.OCR_INFO)
    suspend fun getOCRInfo(@Body params : JsonObject) : ApiResult<OCRInfo>

}