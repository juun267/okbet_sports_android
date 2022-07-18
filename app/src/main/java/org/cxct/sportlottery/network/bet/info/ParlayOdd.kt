package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParlayOdd(
    @Json(name = "max")
    var max: Int,
    @Json(name = "min")
    val min: Int,
    @Json(name = "num")
    val num: Int,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "hkOdds")
    var hkOdds: Double?,
    @Json(name = "malayOdds")
    var malayOdds: Double,
    @Json(name = "indoOdds")
    var indoOdds: Double,
    @Json(name = "parlayType")
    val parlayType: String
) {
    //以下新增參數時, 需至BaseOddButtonViewModel.updateBetOrderParlay將舊物件的參數賦予新物件, 否則數值會遺失
    var sendOutStatus: Boolean = true
    var isInputBet = false
    var betAmount: Double = 0.0
    var inputBetAmountStr: String?= betAmount.toString()
        set(value) {
            field = if (value.isNullOrEmpty()) betAmount.toString() else value
        }
    var amountError: Boolean = false
    var allSingleInput: String? = null //僅給投注單填充所有單注使用
}
