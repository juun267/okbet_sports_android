package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class ParlayOdd(
    @Json(name = "max")
    val max: Long,
    @Json(name = "min")
    val min: Long,
    @Json(name = "num")
    val num: Int,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "hkOdds")
    val hkOdds: Double?,
    @Json(name = "malayOdds")
    val malayOdds: Double,
    @Json(name = "indoOdds")
    val indoOdds: Double,
    @Json(name = "parlayType")
    val parlayType: String,
) {

    var isInputBet = false
    var input: String? = null
    var betAmount: Double = 0.0
    var inputBetAmountStr: String?= betAmount.toString()
        set(value) {
            field = if (value.isNullOrEmpty()) betAmount.toString() else value
        }
    var amountError: Boolean = false
    //region填充單注使用
    var allSingleInput: String? = null //僅給投注單填充所有單注使用
    var singleInput: String? = null
    //endregion
}
