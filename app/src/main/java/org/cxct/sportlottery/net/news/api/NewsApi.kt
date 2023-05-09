package org.cxct.sportlottery.net.news.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.data.NewsCategory
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.Constants
import retrofit2.http.Body
import retrofit2.http.POST

interface NewsApi {

    @POST(Constants.NEWS_LIST_HOME)
    suspend fun getListHome(@Body params: JsonObject): ApiResult<List<NewsCategory>>

    @POST(Constants.NEWS_LIST_RECOMMEND)
    suspend fun getListRecommend(@Body params: JsonObject): ApiResult<List<NewsCategory>>

    @POST(Constants.NEWS_LIST_PAGE)
    suspend fun getListPage(@Body params: JsonObject): ApiResult<PageInfo<NewsItem>>

    @POST(Constants.NEWS_DETIAL)
    suspend fun getNewsDetail(@Body params: JsonObject): ApiResult<NewsDetail>

}