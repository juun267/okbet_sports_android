package org.cxct.sportlottery.network.bank

import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BankService {
    @POST(BANK_MY)
    suspend fun getBankMy(): Response<MyResult>

    @POST(BANK_ADD)
    suspend fun bankAdd(
        @Body bankAddRequest: BankAddRequest
    ): Response<BankAddResult>
}