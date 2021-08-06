package org.cxct.sportlottery.util

/**
 * @author Kevin
 * @create 2021/6/30
 * @description
 */
fun <T> splitSameLength(source: List<T>?, splitItemNum: Int): List<List<T>> {
    val result = ArrayList<List<T>>()

    if (source != null && source.run { isNotEmpty() } && splitItemNum > 0) {
        if (source.size <= splitItemNum) {
            result.add(source)
        } else {
            val splitNum = if (source.size % splitItemNum == 0) source.size / splitItemNum else source.size / splitItemNum + 1
            var value: List<T>? = null
            for (i in 0 until splitNum) {
                value = if (i < splitNum - 1) {
                    source.subList(i * splitItemNum, (i + 1) * splitItemNum)
                } else {
                    source.subList(i * splitItemNum, source.size)
                }
                result.add(value)
            }
        }
    }
    return result
}