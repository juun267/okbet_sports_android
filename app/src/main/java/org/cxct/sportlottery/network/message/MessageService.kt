package org.cxct.sportlottery.network.message

import org.cxct.sportlottery.network.Constants.INDEX_PROMOTENOTICE
import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface MessageService {

    @GET(MESSAGE_LIST)
    suspend fun getMessageList(
        @Query("messageType") messageType: String,
        @Query("userId") userId: Int? = null,
        @Query("platformId") platformId: Int? = null
    ): Response<MessageListResult>

    @GET(INDEX_PROMOTENOTICE)
    suspend fun getPromoteNotice(
        @Query("typeList") typeList: Array<Int>,
    ): Response<MessageListResult>
}