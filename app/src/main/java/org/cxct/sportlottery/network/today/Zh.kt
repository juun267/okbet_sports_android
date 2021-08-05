package org.cxct.sportlottery.network.today


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Zh(
    @Json(name = "coupon_africa_me")
    val couponAfricaMe: String,
    @Json(name = "coupon_all")
    val couponAll: String,
    @Json(name = "coupon_asia")
    val couponAsia: String,
    @Json(name = "coupon_australia")
    val couponAustralia: String,
    @Json(name = "coupon_brazil")
    val couponBrazil: String,
    @Json(name = "coupon_china")
    val couponChina: String,
    @Json(name = "coupon_copa")
    val couponCopa: String,
    @Json(name = "coupon_european_domestic_cups")
    val couponEuropeanDomesticCups: String,
    @Json(name = "coupon_european_leagues")
    val couponEuropeanLeagues: String,
    @Json(name = "coupon_japan")
    val couponJapan: String,
    @Json(name = "coupon_north_america")
    val couponNorthAmerica: String,
    @Json(name = "coupon_south_america")
    val couponSouthAmerica: String,
    @Json(name = "coupon_south_korea")
    val couponSouthKorea: String,
    @Json(name = "coupon_thailand")
    val couponThailand: String,
    @Json(name = "coupon_uefa")
    val couponUefa: String,
    @Json(name = "coupon_uk")
    val couponUk: String?
)