package org.cxct.sportlottery.network.bettingStation

import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class AreaAll(
    @Json(name = "cities")
    val cities: List<City>,
    @Json(name = "countries")
    val countries: MutableList<Country>,
    @Json(name = "provinces")
    val provinces: List<Province>
)

@KeepMembers
data class City(
    @Json(name = "countryId")
    val countryId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "provinceId")
    val provinceId: Int
)

@KeepMembers
data class Country(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)

@KeepMembers
data class Province(
    @Json(name = "countryId")
    val countryId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)