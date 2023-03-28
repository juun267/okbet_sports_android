package org.cxct.sportlottery.network.bettingStation


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import java.io.Serializable

@JsonClass(generateAdapter = true) @KeepMembers
data class BettingStation(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "cityName")
    val cityName: String,
    @Json(name = "code")
    val code: String,
    @Json(name = "countryName")
    val countryName: String,
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lon")
    val lon: Double,
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
    val type: Int,
    @Json(name = "addr")
    val addr: String
) : Serializable {
    var isSelected = false
    var appointmentTime: String = ""
}