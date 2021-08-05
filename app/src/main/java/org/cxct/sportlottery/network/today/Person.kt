package org.cxct.sportlottery.network.today


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Person(val name: String = "Carlson", val age: Int? = 0)
