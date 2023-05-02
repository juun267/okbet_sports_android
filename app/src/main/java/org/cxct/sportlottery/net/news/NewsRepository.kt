package org.cxct.sportlottery.net.news

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.news.api.NewsApi
import org.cxct.sportlottery.net.news.data.NewsCategory
import org.cxct.sportlottery.util.toJson

object NewsRepository {

    val newsApi by lazy { RetrofitHolder.createApiService(NewsApi::class.java) }

    private fun paramDevice(): JsonObject {
        val params = JsonObject()
        params.addProperty("device", 2)
        return params
    }

    suspend fun getListHome(
        pageNum: Int,
        pageSize: Int,
        categoryIds: List<Int>,
    ): ApiResult<NewsCategory> {
        val params = JsonObject()
        params.addProperty("pageNum", pageNum)
        params.addProperty("pageSize", pageSize)
        params.addProperty("categoryIds", categoryIds.toJson())
        return newsApi.getListHome(params)
    }


}