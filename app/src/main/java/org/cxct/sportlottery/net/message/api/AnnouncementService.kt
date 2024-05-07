package org.cxct.sportlottery.net.message.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.message.Row
import retrofit2.http.GET
import retrofit2.http.Query

interface AnnouncementService {

    @GET(Constants.INDEX_PROMOTENOTICE)
    suspend fun getPromoteNotice(@Query("typeList") typeList: Array<Int>): ApiResult<Array<Row>>

}