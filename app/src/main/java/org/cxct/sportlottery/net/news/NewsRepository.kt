package org.cxct.sportlottery.net.news

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.news.api.NewsApi
import org.cxct.sportlottery.net.news.data.NewsCategory
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData

object NewsRepository {

    private val newsApi: NewsApi by lazy {
        RetrofitHolder.createNewRetrofit(sConfigData?.cmsUrl ?: Constants.getBaseUrl())
            .create(NewsApi::class.java)
    }
    const val SORT_CREATE_TIME ="CREATE_TIME"
    const val SORT_DEFAULT ="SORT"

    const val NEWS_OKBET_ID = 12
    const val NEWS_SPORT_ID = 13

    private fun paramDevice(): JsonObject {
        val params = JsonObject()
        params.addProperty("device", 2)
        return params
    }

    suspend fun getHomeNews(
        pageNum: Int,
        pageSize: Int,
        sort: String? = null,
        categoryIds: List<Int>,
    ): ApiResult<List<NewsCategory>> {
        val params = JsonObject()
        params.addProperty("pageNum", pageNum)
        params.addProperty("pageSize", pageSize)
        params.addProperty("sort", sort)
        params.add("categoryIds", Gson().toJsonTree(categoryIds))
        return newsApi.getListHome(params)
    }

    suspend fun getRecommendNews(
        pageNum: Int,
        pageSize: Int,
        categoryIds: List<Int>,
    ): ApiResult<List<NewsCategory>> {
        val params = JsonObject()
        params.addProperty("pageNum", pageNum)
        params.addProperty("pageSize", pageSize)
        params.add("categoryIds", Gson().toJsonTree(categoryIds))
        return newsApi.getListRecommend(params)
    }

    suspend fun getPageNews(
        pageNum: Int,
        pageSize: Int,
        sort: String? = null,
        categoryId: Int,
    ): ApiResult<PageInfo<NewsItem>> {
        val params = JsonObject()
        params.addProperty("pageNum", pageNum)
        params.addProperty("pageSize", pageSize)
        params.addProperty("sort", sort)
        params.addProperty("categoryId", categoryId)
        return newsApi.getListPage(params)
    }

    suspend fun getNewsDetail(
        id: Int,
        sort: String? = null,
    ): ApiResult<NewsDetail> {
        val params = JsonObject()
        params.addProperty("id", id)
        params.addProperty("sort", sort)
        return newsApi.getNewsDetail(params)
    }


}