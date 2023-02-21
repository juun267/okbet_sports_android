package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.playquotacom.PlayQuotaComResult
import retrofit2.Response

class PlayQuotaComRepository {

    suspend fun getPlayQuotaCom(): Response<PlayQuotaComResult> {
        return OneBoSportApi.playQuotaComService.playQuotaComList()
    }

}

