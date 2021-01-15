package org.cxct.sportlottery.network.bank

import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BankService {
    @GET(BANK_MY)
    suspend fun getBankMy(): Response<MyResult>

    @POST(BANK_ADD)
    suspend fun bankAdd(
        @Body bankAddRequest: BankAddRequest
    ): Response<BankAddResult>

    suspend fun bankDelete(
        @Path("id") id: String
    ): Response<BankDeleteResult>
}