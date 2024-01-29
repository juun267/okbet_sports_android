package org.cxct.sportlottery.net.news.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.news.News
import retrofit2.http.GET
import retrofit2.http.Query

interface NoticeApi {

    @GET(Constants.MESSAGE_LIST)
    suspend fun getMessageList(
        @Query("messageType") messageType: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): ApiResult<ArrayList<News>>

}