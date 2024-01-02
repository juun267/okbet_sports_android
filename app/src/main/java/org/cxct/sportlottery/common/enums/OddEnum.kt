package org.cxct.sportlottery.common.enums

/**
 * @author Kevin
 * @create 2021/7/14
 * @description 後續將可共用的enum class集中於此
 */
//socket進來的spread比較
object SpreadState {
    const val SAME = 0
    const val  DIFFERENT = 1
}

//socket進來的新賠率較大或較小
enum class OddState(val state: Int) {
    SAME(0),
    LARGER(1),
    SMALLER(2)
}

//0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
enum class BetStatus(val code: Int) {
    ACTIVATED(0),
    LOCKED(1),
    DEACTIVATED(2)
}

//球員玩法 其他(第一、任何、最後) 無進球
object OddSpreadForSCO {
    const val SCORE_1ST_O = "SCORE-1ST-O"
    const val SCORE_ANT_O = "SCORE-ANT-O"
    const val SCORE_LAST_O = "SCORE-LAST-O"
    const val SCORE_N = "SCORE-N"
}

object OddSpreadForSCOCompare {
    const val SCORE_1ST = "SCORE-1ST"
    const val SCORE_ANT = "SCORE-ANT"
    const val SCORE_LAST = "SCORE-LAST"
    const val SCORE_N = "SCORE-N"
}