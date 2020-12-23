package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {

    @JvmStatic
    fun stampToDate(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm")
        return simpleDateFormat.format(Date(time))
    }

    @JvmStatic
    fun dateToStamp(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date).time
    }

}