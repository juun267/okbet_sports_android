package org.cxct.sportlottery.network.index.config


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NationCurrency(
    @Json(name = "currencyList")
    val currencyList: List<Currency>?,
    @Json(name = "nationCode")
    val nationCode: String,
    @Json(name = "nationName")
    val nationName: String?,
    @Json(name = "phoneCode")
    val phoneCode: String?
) {
    var isSelected: Boolean = false
}