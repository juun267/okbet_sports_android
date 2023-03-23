package org.cxct.sportlottery.common

/**
 * @author Kevin
 * @create 2021/7/14
 * @description 後續將可共用的enum class集中於此
 */
//socket進來的spread比較
enum class SpreadState(val state: Int) {
    SAME(0),
    DIFFERENT(1)
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
enum class OddSpreadForSCO(val playCode: String) {
    SCORE_1ST_O("SCORE-1ST-O"),
    SCORE_ANT_O("SCORE-ANT-O"),
    SCORE_LAST_O("SCORE-LAST-O"),
    SCORE_N("SCORE-N")
}

enum class OddSpreadForSCOCompare(val playCode: String) {
    SCORE_1ST("SCORE-1ST"),
    SCORE_ANT("SCORE-ANT"),
    SCORE_LAST("SCORE-LAST"),
    SCORE_N("SCORE-N")
}