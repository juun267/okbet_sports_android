package org.cxct.sportlottery.net.chat.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SignService {

    @POST(Constants.CHAT_GET_SIGN)
    suspend fun getSign(@Body params: JsonObject): ApiResult<JsonElement>
}