package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.sport.SportMenuResult

class SportMenuRepository {

    suspend fun getSportMenu(): SportMenuResult? {
        val sportMenuResponse = OneBoSportApi.sportService.getMenu()

        return if (sportMenuResponse.isSuccessful) {
            sportMenuResponse.body()
        } else {
            ErrorUtils.parseError(sportMenuResponse)
        }
    }

}