package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.sport.SportMenuResult
import retrofit2.Response

class SportMenuRepository {

    suspend fun getSportMenu(): Response<SportMenuResult> {
        return OneBoSportApi.sportService.getMenu()
    }
}