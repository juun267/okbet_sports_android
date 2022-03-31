package org.cxct.sportlottery.network.news

import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET(MESSAGE_LIST)
    suspend fun getMessageList(
        @Query("messageType") messageType: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<NewsResult>
}