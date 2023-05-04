package org.cxct.sportlottery.net.news.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.news.data.NewsCategory
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.POST

interface NewsApi {

    @POST(Constants.NEW_LIST_HOME)
    suspend fun getListHome(@Body params: JsonObject): ApiResult<List<NewsCategory>>

}