package org.cxct.sportlottery.enum

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