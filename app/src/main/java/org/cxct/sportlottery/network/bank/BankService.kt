package org.cxct.sportlottery.network.bank

import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_DELETE
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.bank.add.BankAddRequest
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankMyResult
import retrofit2.Response
import retrofit2.http.*

interface BankService {
    @GET(BANK_MY)
    suspend fun getBankMy(): Response<BankMyResult>

    @POST(BANK_ADD)
    suspend fun bankAdd(
        @Body bankAddRequest: BankAddRequest
    ): Response<BankAddResult>

    @DELETE(BANK_DELETE)
    suspend fun bankDelete(
        @Path("id") id: String
    ): Response<BankDeleteResult>
}