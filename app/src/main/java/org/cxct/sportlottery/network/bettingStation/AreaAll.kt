package org.cxct.sportlottery.network.bettingStation

import com.squareup.moshi.Json


data class AreaAll(
    @Json(name = "cities")
    val cities: List<City>,
    @Json(name = "countries")
    val countries: MutableList<Country>,
    @Json(name = "provinces")
    val provinces: List<Province>
)

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

data class Country(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)

data class Province(
    @Json(name = "countryId")
    val countryId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)