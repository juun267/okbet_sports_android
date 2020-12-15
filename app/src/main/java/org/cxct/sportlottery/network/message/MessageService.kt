package org.cxct.sportlottery.network.message

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface MessageService {
    @Headers("x-session-platform-code:plat1")
    @GET("/api/front/message/list")
    suspend fun getMessageList(
        @Header("x-session-token") token: String?,
        @Header("x-lang") language: String,
        @Query("messageType") messageType: String,
        @Query("userId") userId: Int? = null,
        @Query("platformId") platformId: Int? = null
    ): Response<MessageListResult>
}