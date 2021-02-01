package org.cxct.sportlottery.network.matchresult.playlist

/**
 * 紀錄第一層RecyclerView的位置及第三層需使用到的資料 ( Map (settleRvPosition, GameDetailRvData) )
 * settleRvPosition, gameResultRvPosition : 需刷新的position 第一層, 第二層
 */
data class RvPosition(val settleRvPosition: Int, val gameResultRvPosition: Int)

class SettlementRvData(
    var settleRvPosition: Int,
    var gameResultRvPosition: Int,
    var settlementRvMap: MutableMap<RvPosition, MatchResultPlayListResult> = mutableMapOf()
)