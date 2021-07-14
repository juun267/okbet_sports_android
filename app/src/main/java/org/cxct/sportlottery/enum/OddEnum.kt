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