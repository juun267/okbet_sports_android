package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.SPORT_MENU
import retrofit2.Response
import retrofit2.http.GET


interface SportService {

    @GET(SPORT_MENU)
    suspend fun getMenu(
    ): Response<SportMenuResult>
}