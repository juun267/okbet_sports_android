package org.cxct.sportlottery.network.bank

import org.cxct.sportlottery.network.Constants.BANK_MY
import retrofit2.Response
import retrofit2.http.POST

interface BankService {
    @POST(BANK_MY)
    suspend fun getBankMy(): Response<MyResult>
}