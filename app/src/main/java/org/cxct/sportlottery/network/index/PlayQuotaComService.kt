package org.cxct.sportlottery.network.index

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.playquotacom.PlayQuotaComResult
import retrofit2.Response
import retrofit2.http.GET

interface PlayQuotaComService {

    @GET(Constants.PLAYQUOTACOM_LIST)
    suspend fun playQuotaComList(): Response<PlayQuotaComResult>

}