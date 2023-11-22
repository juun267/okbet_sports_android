package org.cxct.sportlottery.network.service.odds_change

import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceChannel
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
@KeepMembers
data class OddsChangeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.ODDS_CHANGE,
    @Json(name = "eventId")
    val eventId: String?,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int?, //是否是冠军玩法，1：是，0：否
    @Json(name = "oddsList")
    val oddsList: MutableList<OddsList> = mutableListOf(),
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>? = null,
    @Json(name = "gameType")
    val gameType: String? = null,
    @Json(name = "playCateNum")
    val playCateNum: Int? = null,
//    @Json(name = "betPlayCateNameMap")
//    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
//    @Json(name = "playCateNameMap")
//    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?
) : ServiceEventType, ServiceChannel {
    override var channel: String? = null
    var odds: MutableMap<String, MutableList<Odd>?> = mutableMapOf() //key=>玩法类型code, value=>赔率列表

    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null

    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
}

@KeepMembers
data class OddsList(
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "oddsList")
    val oddsList: MutableList<Odd>?,
)

/**
 * OddsChangeEvent nullableOddsVO 資料結構轉換
 * @see Odd
 * @see FrontWsEvent.OddsVO
 */
private fun FrontWsEvent.NullableOddsVO.transferOdd(): Odd? {
    this.let { nullableOddsVO ->
        return if (nullableOddsVO.hasOddsVO()) {
            Odd(
                id = nullableOddsVO.oddsVO.id,
                name = nullableOddsVO.oddsVO.name,
                spread = nullableOddsVO.oddsVO.spread,
                odds = nullableOddsVO.oddsVO.odds.toDouble(),
                marketSort = nullableOddsVO.oddsVO.marketSort,
                status = nullableOddsVO.oddsVO.status,
                producerId = nullableOddsVO.oddsVO.producerId,
                nameMap = nullableOddsVO.oddsVO.nameMapMap,
                extInfoMap = nullableOddsVO.oddsVO.extInfoMapMap,
                extInfo = nullableOddsVO.oddsVO.extInfo,
                playCode = nullableOddsVO.oddsVO.playCode
            )
        } else {
            null
        }
    }
}

/**
 * protobuf FrontWsEvent 資料結構轉換
 */
fun FrontWsEvent.OddsChangeEvent.transferOddsChangeEvent(): OddsChangeEvent {
    this.let { protoEvent ->
        return OddsChangeEvent(
            eventId = protoEvent.eventId,
            isLongTermEvent = protoEvent.isLongTermEvent,
            oddsList = protoEvent.oddsListList.transferOddsList(),
            gameType = protoEvent.gameType,
            quickPlayCateList = protoEvent.quickPlayCateListList.transferQuickPlayCateList(),
            playCateNum = protoEvent.playCateNum
        )
    }
}

/**
 * OddsChangeEvent quickPlayCateListList資料結構轉換
 * @see QuickPlayCate
 * @see FrontWsEvent.QuickPlayCateVO
 */
private fun List<FrontWsEvent.QuickPlayCateVO>.transferQuickPlayCateList(): List<QuickPlayCate> {
    this.let { quickPlayCateVOList: List<FrontWsEvent.QuickPlayCateVO> ->
        val quickPlayCateList = mutableListOf<QuickPlayCate>()
        quickPlayCateVOList.forEach { quickPlayCateVO ->
            quickPlayCateList.add(
                QuickPlayCate(
                    code = quickPlayCateVO.code,
                    gameType = quickPlayCateVO.gameType,
                    name = quickPlayCateVO.name,
                    sort = quickPlayCateVO.sort
                )
            )
        }
        return quickPlayCateList
    }
}

/**
 * 轉換當前使用的MutableList<OddsList>
 */
private fun List<FrontWsEvent.OddsMarketVO>.transferOddsList(): MutableList<OddsList> {
    /*新增返回列表*/
    val list = mutableListOf<OddsList>()

    /*socket回傳資料轉換map key=>玩法类型code, value=>赔率列表*/
    val odds: MutableMap<String, MutableList<Odd?>> = this.associateBy(
        keySelector = {
            it.playCateCode.toString()
        },
        valueTransform = {
            it.oddsListList.map { nullableOddsVO -> nullableOddsVO.transferOdd() }.toMutableList()
        }
    ).toMutableMap()

    /*迴圈新增 不添加賠率為null項目*/
    odds.forEach { (key, value) ->
        val newValue = mutableListOf<Odd>()
        value.forEach { odd -> odd?.let { newValue.add(it) } }
        list.add(
            OddsList(key, newValue)
        )
    }
    return list
}
