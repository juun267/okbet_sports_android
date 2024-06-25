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
import org.cxct.sportlottery.repository.GamePlayNameRepository

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
    val oddsList: List<FrontWsEvent.OddsMarketVO> = mutableListOf(),
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: List<QuickPlayCate>? = null,
    @Json(name = "gameType")
    val gameType: String? = null,
    @Json(name = "playCateNum")
    val playCateNum: Int? = null,
    @Json(name = "updateMode")
    val updateMode: Int? = null, //2 为全量更新需要替换本地全部玩法
) : ServiceEventType, ServiceChannel {
    override var channel: String? = null

    /**
     * 從oddsList重組資料結構至Map<String, MutableList<Odd?>?>
     * 配置玩法類別是否為僅有歐洲盤(是否支援切換盤口)
     */
    var odds: MutableMap<String, MutableList<Odd>?> = oddsList.associateBy(
        keySelector = { it.playCateCode.toString() },
        valueTransform = { oddsMarketVO ->
            oddsMarketVO.oddsListList
                .filter { nullableOddsVO -> nullableOddsVO.hasOddsVO() } //只保留有OddV0的
                .map { nonNullOddV0 -> nonNullOddV0.transferOdd() }.toMutableList()
        }).setupIsOnlyEUType().toMutableMap() //key=>玩法类型code, value=>赔率列表

    /**
     * 玩法多語系名稱Map
     * 從api找對應玩法的多語系名稱
     *
     * @see org.cxct.sportlottery.network.sport.SportService.getIndexResourceJson
     */
    var playCateNameMap: MutableMap<String?, MutableMap<String?, String?>?> =
        GamePlayNameRepository.getPlayCateMenuDetailsListMap(
            gameType,
            oddsList.associateBy { it.playCateCode.split(":").firstOrNull() ?: "" }.keys.toList()
        )

    var betPlayCateNameMap: MutableMap<String?, MutableMap<String?, String?>?> =
        GamePlayNameRepository.getBetPlayCateListMap(
            gameType,
            oddsList.associateBy { it.playCateCode.split(":").firstOrNull() ?: "" }.keys.toList()
        )

    /**
     * OddsChangeEvent nullableOddsVO 資料結構轉換
     * @see Odd
     * @see FrontWsEvent.OddsVO
     */
    private fun FrontWsEvent.NullableOddsVO.transferOdd(): Odd {
        this.let { nullableOddsVO ->
            return Odd(
                id = nullableOddsVO.oddsVO.id,
                name = nullableOddsVO.oddsVO.name,
                spread = nullableOddsVO.oddsVO.spread,
                originalOdds = nullableOddsVO.oddsVO.odds,
                marketSort = nullableOddsVO.oddsVO.marketSort,
                status = nullableOddsVO.oddsVO.status,
                producerId = nullableOddsVO.oddsVO.producerId,
                nameMap = nullableOddsVO.oddsVO.nameMapMap,
                extInfoMap = nullableOddsVO.oddsVO.extInfoMapMap,
                extInfo = nullableOddsVO.oddsVO.extInfo,
                playCode = nullableOddsVO.oddsVO.playCode
            )
        }
    }

    /**
     * 配置該玩法是否僅有歐洲盤(是否支援盤口切換)
     * 有支援盤口切換 => 不僅有歐洲盤
     *
     * @see Odd.isOnlyEUType 是否僅有歐洲盤
     * @see org.cxct.sportlottery.network.sport.IndexResourceJsonResult.IndexResourceList.GameTypeResource.PlayCate.supportOddsTypeSwitch 是否支援切換盤口
     */
    private fun Map<String, MutableList<Odd>?>.setupIsOnlyEUType(): Map<String, MutableList<Odd>?> {
        GamePlayNameRepository.getPlayCateListSupportOddsTypeSwitch(gameType, this.keys)
            .let { oddsTypeSwitchMap ->
                oddsTypeSwitchMap.forEach { (playCateCode, switch) ->
                    this[playCateCode]?.forEach { odd ->
                        //有支援盤口切換 => 不僅有歐洲盤
                        odd.isOnlyEUType = !switch
                    }
                }
            }
        return this
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
            oddsList = protoEvent.oddsListList,
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
