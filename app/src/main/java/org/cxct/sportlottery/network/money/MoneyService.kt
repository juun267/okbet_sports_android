package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MoneyService {

    @GET(Constants.RECHARGE_CONFIG_MAP)
    suspend fun getRechCfg(
        @Query("x-lang") languageType: String? = null //语言设置,zh:中文简体,zht:中文繁体,en:英语，vi:越南(Global)
    ): Response<MoneyRechCfgResult>

}