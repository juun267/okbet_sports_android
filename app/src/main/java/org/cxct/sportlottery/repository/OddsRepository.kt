package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.odds.OddsDetailRequest
import org.cxct.sportlottery.network.odds.OddsDetailResult

class OddsRepository {

    suspend fun getOddsDetail(matchId: String, oddsType: String): OddsDetailResult? {

        val oddsDetailResponse = OneBoSportApi.oddsService.getOddsDetail(
            OddsDetailRequest(matchId, oddsType)
        )

        if (oddsDetailResponse.isSuccessful) {
            return oddsDetailResponse.body()
        } else {
            val apiError = ErrorUtils.parseError(oddsDetailResponse)
            apiError?.let {
                if (it.success != null && it.code != null && it.msg != null) {
                    return OddsDetailResult(it.code, it.msg, it.success, null)
                }
            }
        }

        return null
    }

}