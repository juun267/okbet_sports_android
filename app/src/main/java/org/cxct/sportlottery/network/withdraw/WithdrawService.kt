package org.cxct.sportlottery.network.withdraw

import org.cxct.sportlottery.network.Constants.WITHDRAW_ADD
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WithdrawService {
    @POST(WITHDRAW_ADD)
    suspend fun addWithdraw(
        @Body withdrawAddRequest: WithdrawAddRequest
    ): Response<WithdrawAddResult>
}