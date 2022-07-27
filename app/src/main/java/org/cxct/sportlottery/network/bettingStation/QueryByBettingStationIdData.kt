package org.cxct.sportlottery.network.bettingStation

import com.squareup.moshi.Json

data class QueryByBettingStationIdData(
    @Json(name = "addr")
    val addr: String,
    @Json(name = "cityName")
    val cityName: String,
    @Json(name = "code")
    val code: String,
    @Json(name = "countryName")
    val countryName: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "lat")
    val lat: Int,
    @Json(name = "lon")
    val lon: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "officeEndTime")
    val officeEndTime: String,
    @Json(name = "officeStartTime")
    val officeStartTime: String,
    @Json(name = "pic")
    val pic: String,
    @Json(name = "provinceName")
    val provinceName: String,
    @Json(name = "state")
    val state: Int,
    @Json(name = "telephone")
    val telephone: String,
    @Json(name = "type")
    val type: Int
)