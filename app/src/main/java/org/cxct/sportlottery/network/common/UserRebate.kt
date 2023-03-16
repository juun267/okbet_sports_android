package org.cxct.sportlottery.network.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class UserRebate(
    @Json(name = "gameCate")
    val gameCate: String,
    @Json(name = "cateId")
    val cateId: Int,
    @Json(name = "rebate")
    val rebate: Int,
) : java.io.Serializable
