package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.SPORT_QUERY
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.network.sport.query.SportQueryResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface SportService {

    @POST(SPORT_MENU)
    suspend fun getMenu(
        @Body sportMenuRequest: SportMenuRequest
    ): Response<SportMenuResult>

    @POST(SPORT_QUERY)
    suspend fun getQuery(
        @Body sportQueryRequest: SportQueryRequest
    ): Response<SportQueryResult>
}