package org.cxct.sportlottery.net.chat.api

import com.google.gson.JsonElement
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import retrofit2.http.GET

interface SignService {

    @GET(Constants.CHAT_GET_SIGN)
    suspend fun getSign(): ApiResult<JsonElement>
}