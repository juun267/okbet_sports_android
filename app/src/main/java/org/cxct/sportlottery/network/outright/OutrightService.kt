package org.cxct.sportlottery.network.outright

import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OutrightService {

    @POST(OUTRIGHT_RESULT_LIST)
    suspend fun getOutrightResultList(
        @Body outrightResultListRequest: OutrightResultListRequest,
    ): Response<OutrightResultListResult>
}