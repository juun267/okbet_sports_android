package org.cxct.sportlottery.network.service.order_settlement

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class OrderSettlementEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.ORDER_SETTLEMENT.value,
    @Json(name = "sportBet")
    val sportBet: SportBet? //返回消息，正常消息： pong, 已过期： timeout
) : ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class SportBet(
    val uniqNo: String?,
    val orderNo: String?,
    val userId: Int?,
    val userName: String?,
    val testFlag: Int?, //测试试玩账号类型：0-普通账号，2-内部测试账号，1-游客
    val gameType: String?,
    val matchOdds: List<MatchOdds>? = listOf(),
    val stake: Double?,
    val num: Int?,
    val totalAmount: Double?,
    val winnable: Double?,
    val grossWin: Double?,
    val netWin: Double?,
    val rebate: Double?,
    val rebateAmount: Double?,
    val win: Double?,
    val status: Int?,  //状态 0：未确认，1：未结算，2：赢，3：赢半，4：输，5：输半，6：和，7：已取消 //statusNameMap
    val cancelReason: String?,
    val cancelledBy: String?, //取消触发来源（ source: 数据源，own: 自有平台），数据源取消的注单允许回滚
    val addTime: String?,
    val settleTime: String?,
    val parlay: Int?,
    val parlayType: String?,
    val workerNo: Int?,
    val platformId: Int?,
    val winCount: Double?,
    val isChampionship: Int?, //0：普通投注，1：冠军投注
)

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdds(
    val oddsId: String?,
    val matchId: String?,
    val leagueName: String?,
    val homeName: String?,
    val homeId: String?,
    val awayName: String?,
    val awayId: String?,
    val playId: Int?,
    val playCode: String?,
    val playName: String?,
    val odds: Double?,
    val hkOdds: Double?,
    val oddsType: String?,
    val playCateId: Int?,
    val playCateName: String?,
    val startTime: String?,
    val spread: String?,
    val status: Int?, //状态 0：未确认，1：未结算，2：赢，3：赢半，4：输，5：输半，6：和，7：已取消 //statusNameMap
    val mtsSelections: String?,
)

enum class Status(val code: Int) {
    UN_CHECK(0), //未确认
    UN_DONE(1), //未结算
    WIN(2), //赢
    WIN_HALF(3),//赢半
    LOSE(4), //输
    LOSE_HALF(5), //输半
    DRAW(6), //和
    CANCEL(7)//已取消
}