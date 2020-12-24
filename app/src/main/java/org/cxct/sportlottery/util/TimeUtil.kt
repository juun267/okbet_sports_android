package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {
    private const val TAG = "TimeUtil"

    @JvmStatic
    fun stampToDate(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm")
        return simpleDateFormat.format(Date(time))
    }

    @JvmStatic
    fun dateToStamp(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date).time
    }

    @JvmStatic
    fun timeFormat(time: Long?, format: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        var formattedTime = ""
        try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.timeZone = timeZone
            formattedTime = dateFormat.format(Date(time!!))
        } catch (e: Exception) {
            Log.e(TAG, "解析日期失敗!!!")
            e.printStackTrace()
        }
        return formattedTime
    }
}