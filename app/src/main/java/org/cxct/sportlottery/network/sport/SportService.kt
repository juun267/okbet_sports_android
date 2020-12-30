package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.SPORT_MENU
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface SportService {

    @POST(SPORT_MENU)
    suspend fun getMenu(
        @Body sportMenuRequest: SportMenuRequest
    ): Response<SportMenuResult>
}