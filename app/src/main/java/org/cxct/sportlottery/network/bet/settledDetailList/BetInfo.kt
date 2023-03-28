package org.cxct.sportlottery.network.bet.settledDetailList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class BetInfo(
    @Json(name = "playMaxBetSingleBet")
    val playMaxBetSingleBetString: String?,
    @Json(name = "maxBetMoney")
    val maxBetMoneyString: String?,
    @Json(name = "minBetMoney")
    val minBetMoneyString: String?,
    @Json(name = "maxPayout")
    val maxPayoutString: String?,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoneyString: String?,
    @Json(name = "minParlayBetMoney")
    val minParlayBetMoneyString: String?,
    @Json(name = "maxParlayPayout")
    val maxParlayPayoutString: String?,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoneyString: String?,
    @Json(name = "minCpBetMoney")
    val minCpBetMoneyString: String?,
    @Json(name = "maxCpPayout")
    val maxCpPayoutString: String?
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
