package org.cxct.sportlottery.network.withdraw

import org.cxct.sportlottery.network.Constants.GET_TWO_FACTOR_STATUS
import org.cxct.sportlottery.network.Constants.SEND_TWO_FACTOR
import org.cxct.sportlottery.network.Constants.VALIDATE_TWO_FACTOR
import org.cxct.sportlottery.network.Constants.WITHDRAW_ADD
import org.cxct.sportlottery.network.Constants.WITHDRAW_LIST
import org.cxct.sportlottery.network.Constants.WITHDRAW_UW_CHECK
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddRequest
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResultData
import org.cxct.sportlottery.network.withdraw.list.WithdrawListRequest
import org.cxct.sportlottery.network.withdraw.list.WithdrawListResult
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.network.withdraw.uwcheck.WithdrawUwCheckResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WithdrawService {
    @POST(WITHDRAW_ADD)
    suspend fun addWithdraw(
        @Body withdrawAddRequest: WithdrawAddRequest,
    ): Response<WithdrawAddResultData>

    @POST(WITHDRAW_LIST)
    suspend fun getWithdrawList(
        @Body withdrawListRequest: WithdrawListRequest
    ): Response<WithdrawListResult>

    @GET(WITHDRAW_UW_CHECK)
    suspend fun getWithdrawUwCheck(): Response<WithdrawUwCheckResult>

    @GET(GET_TWO_FACTOR_STATUS)
    suspend fun getTwoFactorStatus(): Response<NetResult>

    @POST(SEND_TWO_FACTOR)
    suspend fun sendTwoFactor(): Response<NetResult>

    @POST(VALIDATE_TWO_FACTOR)
    suspend fun validateTwoFactor(
        @Body validateTwoFactorRequest: ValidateTwoFactorRequest
    ): Response<NetResult>

}