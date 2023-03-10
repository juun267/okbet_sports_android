package org.cxct.sportlottery.network.service.odds_change

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceChannel
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class OddsChangeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.ODDS_CHANGE.value,
    @Json(name = "eventId")
    val eventId: String?,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int?, //是否是冠军玩法，1：是，0：否
    @Json(name = "oddsList")
    val oddsList: MutableList<OddsList> = mutableListOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarkets>? = mapOf(), //动态市场字典(冠军玩法有效)， key: 对应odds的key, value: {zh:"xxx",en:"cxxewe"}
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>? = null,
    @Json(name = "gameType")
    val gameType: String? = null,
    @Json(name = "playCateNum")
    val playCateNum: Int? = null,
    @Json(name = "betPlayCateNameMap")
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?
) : ServiceEventType, ServiceChannel {
    override var channel: String? = null
    var odds: MutableMap<String, MutableList<Odd>?> = mutableMapOf() //key=>玩法类型code, value=>赔率列表
}

data class OddsList (
    @Json(name = "playCateCode")
    val playCateCode:String?,
    @Json(name = "oddsList")
    val oddsList: MutableList<Odd>?
)
