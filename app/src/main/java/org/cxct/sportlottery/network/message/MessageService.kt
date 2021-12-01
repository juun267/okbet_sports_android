package org.cxct.sportlottery.network.message

import org.cxct.sportlottery.network.Constants.INDEX_PROMOTENOTICE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface MessageService {

    @GET(INDEX_PROMOTENOTICE)
    suspend fun getPromoteNotice(
        @Query("typeList") typeList: Array<Int>,
    ): Response<MessageListResult>

}