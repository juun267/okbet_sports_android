package org.cxct.sportlottery.network.service.match_odds_change


import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType
import org.cxct.sportlottery.repository.GamePlayNameRepository.getPlayCateMap
import org.cxct.sportlottery.repository.GamePlayNameRepository.getPlayCateSupportOddsTypeSwitch
import org.cxct.sportlottery.repository.GamePlayNameRepository.getPlayNameMap

@JsonClass(generateAdapter = true)
@KeepMembers
data class MatchOddsChangeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.MATCH_ODDS_CHANGE,
    @Json(name = "eventId")
    val eventId: String?,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int?,
    @Json(name = "odds")
    var odds: Map<String, Odds>?, // key -> GameBetType ; value -> Odds
    @Json(name = "updateMode")
    val updateMode: Int? = null, //2 为全量更新需要替换本地全部玩法
) : ServiceEventType {
    fun isReplaceAll() = 2 == updateMode
}

/**
 * protobuf FrontWsEvent 資料結構轉換
 */
fun FrontWsEvent.MatchOddsChangeEvent.transferMatchOddsChangeEvent(): MatchOddsChangeEvent {
    this.let { protoEvent ->
        return MatchOddsChangeEvent(
            eventId = protoEvent.eventId,
            isLongTermEvent = protoEvent.isLongTermEvent,
            odds = transferOddsMap(protoEvent.gameType)
        )
    }
}

/**
 * protobuf FrontWsEvent 轉換取得oddsMap
 */
fun FrontWsEvent.MatchOddsChangeEvent.transferOddsMap(gameType: String): Map<String, Odds> {
    this.let { matchOddsChangeEvent ->
        val oddsMap = mutableMapOf<String, Odds>()
        matchOddsChangeEvent.oddsListList.forEach { oddsDetailVO ->
            oddsMap[oddsDetailVO.playCateCode] = oddsDetailVO.transferOdds(gameType)
        }

        return oddsMap
    }
}

/**
 * protobuf FrontWsEvent 轉換取得odds
 */
fun FrontWsEvent.OddsDetailVO.transferOdds(gameType: String): Odds {
    this.let { oddsDetailVO ->
        return Odds(
            typeCodes = oddsDetailVO.typeCodes,
            name = oddsDetailVO.name,
            odds = oddsDetailVO.oddsList.transferOddList(
                gameType,
                getPlayCateSupportOddsTypeSwitch(gameType, getPlayCateMappingCode(oddsDetailVO.playCateCode))
            ),
            nameMap = getPlayCateMap(gameType, getPlayCateMappingCode(oddsDetailVO.playCateCode)),
            rowSort = oddsDetailVO.rowSort
        )
    }
}

/**
 * 部分玩法後綴帶有spread, 剔除後綴後玩法才能正確取得翻譯及順序
 */
private fun getPlayCateMappingCode(playCateCode: String): String = if (playCateCode.contains(":")) {
    playCateCode.split(":").getOrNull(0) ?: ""
} else {
    playCateCode
}

/**
 * protobuf FrontWsEvent 轉換取得oddList
 */
fun List<FrontWsEvent.NullableOddsWithPlayNameVO>.transferOddList(
    gameType: String,
    supportOddsTypeSwitch: Boolean?,
): MutableList<Odd?> {
    this.let { list ->
        val oddMutableList = mutableListOf<Odd?>()

        list.forEach { nullableOddsWithPlayNameVO ->
            if (nullableOddsWithPlayNameVO.hasOddsWithPlayNameVO()) {
                oddMutableList.add(
                    Odd(
                        id = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.id,
                        name = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.name,
                        spread = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.spread,
                        originalOdds = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.odds,
                        marketSort = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.marketSort,
                        status = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.status,
                        producerId = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.producerId,
                        nameMap = getOddNameMap(
                            nullableOddsWithPlayNameVO.oddsWithPlayNameVO.nameMapMap,
                            gameType = gameType,
                            playCode = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.playCode
                        ),
                        extInfoMap = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.extInfoMapMap,
                        extInfo = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.extInfo,
                        playCode = nullableOddsWithPlayNameVO.oddsWithPlayNameVO.playCode
                    ).apply {
                        supportOddsTypeSwitch?.let {
                            isOnlyEUType = !it
                        }
                    }
                )
            }
        }

        return oddMutableList
    }
}

/**
 * 若socket的Odd nameMap為空, 則從api獲取的玩法翻譯資源中找到對應的nameMap
 */
fun getOddNameMap(socketNameMap: Map<String, String>, gameType: String, playCode: String): Map<String?, String?> {
    return if (socketNameMap.isNotEmpty()) socketNameMap.toMap() else getPlayNameMap(gameType, playCode)
}
