package org.cxct.sportlottery.ui.profileCenter.timezone

data class TimeZone(
    val name: String,
    val country_zh: String,
    val country_en: String,
    val country_vi: String,
    val country_ph: String,
    val city_zh: String,
    val city_en: String,
    val city_vi: String,
    val city_ph: String,
    var isSelected: Boolean = false
)
