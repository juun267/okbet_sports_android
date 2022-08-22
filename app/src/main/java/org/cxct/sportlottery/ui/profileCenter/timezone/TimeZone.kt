package org.cxct.sportlottery.ui.profileCenter.timezone

data class TimeZone(
    val name: String,
    val country_zh: String,
    val country_en: String,
    val country_vi: String,
    val country_th: String,
    val country_in: String,
    val country_my: String,
    val country_kn: String,
    val country_jp: String,
    val city_zh: String,
    val city_en: String,
    val city_vi: String,
    val city_th: String,
    val city_in: String,
    val city_my: String,
    val city_kn: String,
    val city_jp: String,
    var isSelected: Boolean = false
)
