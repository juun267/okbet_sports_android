package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.GameType

@JsonClass(generateAdapter = true)
@KeepMembers
data class Sport(
    @Json(name = "num")
    var num: Int,
    @Json(name = "items")
    val items: List<Item>
){
    fun numOfESport():Int?{
        return items?.firstOrNull { it.code== GameType.ES.key }?.num
    }
}
