package org.cxct.sportlottery.net.news.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.network.news.SportNewsRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface NoticeApi {

    @POST(Constants.MESSAGE_LIST2)
    suspend fun getMessageList(@Body params: SportNewsRequest): ApiResult<ArrayList<News>>

}