package org.cxct.sportlottery.util

object TextUtil {
    fun split(str: String): MutableList<String> {
        return str.split(",").toMutableList()
    }
}