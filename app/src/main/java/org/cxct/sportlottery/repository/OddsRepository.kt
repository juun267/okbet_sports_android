package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult

class OddsRepository {

    suspend fun getOddsDetail(matchId: String, oddsType: String): OddsDetailResult? {

        val oddsDetailResponse = OneBoSportApi.oddsService.getOddsDetail(
            OddsDetailRequest(matchId, oddsType)
        )

        return if (oddsDetailResponse.isSuccessful) {
            oddsDetailResponse.body()
        } else {
            ErrorUtils.parseError(oddsDetailResponse)
        }
    }

}