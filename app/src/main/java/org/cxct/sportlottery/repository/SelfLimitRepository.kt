package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.feedback.*
import org.cxct.sportlottery.network.user.selflimit.FrozeRequest
import org.cxct.sportlottery.network.user.selflimit.FrozeResult
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitRequest
import org.cxct.sportlottery.network.user.selflimit.PerBetLimitResult
import retrofit2.Response

object SelfLimitRepository {

    suspend fun froze(frozeRequest: FrozeRequest): Response<FrozeResult> {
        return OneBoSportApi.userService.froze(frozeRequest)
    }

    suspend fun setPerBetLimit(perBetLimitRequest: PerBetLimitRequest): Response<PerBetLimitResult> {
        return OneBoSportApi.userService.setPerBetLimit(perBetLimitRequest)
    }

}