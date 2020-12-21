package org.cxct.sportlottery.network.message

import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


interface MessageService {

    @Headers("x-session-platform-code:plat1")
    @GET(MESSAGE_LIST)
    suspend fun getMessageList(
        @Query("messageType") messageType: String,
        @Query("userId") userId: Int? = null,
        @Query("platformId") platformId: Int? = null
    ): Response<MessageListResult>
}