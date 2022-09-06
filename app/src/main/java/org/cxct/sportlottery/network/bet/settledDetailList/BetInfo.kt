package org.cxct.sportlottery.network.bet.settledDetailList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BetInfo(
    @Json(name = "playMaxBetSingleBet")
    private val playMaxBetSingleBetString: String?,
    @Json(name = "maxBetMoney")
    private val maxBetMoneyString: String?,
    @Json(name = "minBetMoney")
    private val minBetMoneyString: String?,
    @Json(name = "maxPayout")
    private val maxPayoutString: String?,
    @Json(name = "maxParlayBetMoney")
    private val maxParlayBetMoneyString: String?,
    @Json(name = "minParlayBetMoney")
    private val minParlayBetMoneyString: String?,
    @Json(name = "maxParlayPayout")
    private val maxParlayPayoutString: String?,
    @Json(name = "maxCpBetMoney")
    private val maxCpBetMoneyString: String?,
    @Json(name = "minCpBetMoney")
    private val minCpBetMoneyString: String?,
    @Json(name = "maxCpPayout")
    private val maxCpPayoutString: String?
) {
    val playMaxBetSingleBet get() = playMaxBetSingleBetString?.toBigDecimal()
    val maxBetMoney get() = maxBetMoneyString?.toBigDecimal()
    val minBetMoney get() = minBetMoneyString?.toBigDecimal()
    val maxPayout get() = maxPayoutString?.toBigDecimal()
    val maxParlayBetMoney get() = maxParlayBetMoneyString?.toBigDecimal()
    val minParlayBetMoney get() = minParlayBetMoneyString?.toBigDecimal()
    val maxParlayPayout get() = maxParlayPayoutString?.toBigDecimal()
    val maxCpBetMoney get() = maxCpBetMoneyString?.toBigDecimal()
    val minCpBetMoney get() = minCpBetMoneyString?.toBigDecimal()
    val maxCpPayout get() = maxCpPayoutString?.toBigDecimal()
}
