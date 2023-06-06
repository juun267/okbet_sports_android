package org.cxct.sportlottery.net.sport.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.sport.SportMenuData
import retrofit2.http.Body
import retrofit2.http.POST

interface SportService {

    @POST(Constants.SPORT_MENU)
    suspend fun getMenu(@Body sportMenuRequest: Map<String, String>): ApiResult<SportMenuData>

}