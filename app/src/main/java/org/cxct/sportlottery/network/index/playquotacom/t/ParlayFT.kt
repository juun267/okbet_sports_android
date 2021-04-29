package org.cxct.sportlottery.network.index.playquotacom.t


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParlayFT(
    @Json(name = "code")
    override val code: String,
    @Json(name = "gameType")
    override val gameType: String,
    @Json(name = "id")
    override val id: Int,
    @Json(name = "max")
    override val max: Int,
    @Json(name = "min")
    override val min: Int,
    @Json(name = "name")
    override val name: String,
    @Json(name = "platformId")
    override val platformId: Int
) : PlayQuota