package org.cxct.sportlottery.network.money

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MoneyService {

    @GET(Constants.RECHARGE_CONFIG_MAP)
    suspend fun getRechCfg(): Response<MoneyRechCfgResult>

    @POST(Constants.USER_RECHARGE_ADD)
    suspend fun rechargeAdd(
        @Body moneyAddRequest: MoneyAddRequest
    ): Response<MoneyAddResult>

    @POST(Constants.USER_RECHARGE_ONLINE_PAY)
    suspend fun rechargeOnlinePay(
        @Body moneyAddRequest: MoneyAddRequest
//        @Query("rechCfgId") rechCfgId: Int,//充值配置id
//        @Query("bankCode") bankCode: String,//银行代码
//        @Query("depositMoney") depositMoney: Int,//充值金额
//        @Query("payer") payer: String,//充值账号
//        @Query("payerName") payerName: String,//充值人名称
//        @Query("payerBankName") payerBankName: String,//充值银行名称
//        @Query("payerInfo") payerInfo: String,//充值附加信息
//        @Query("depositDate") depositDate: String,//充值日期
    ): Response<MoneyAddResult>

}