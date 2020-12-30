package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import timber.log.Timber
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
            Timber.e("解析日期失敗!!! \n$e")
            e.printStackTrace()
        }
        return formattedTime
    }

    fun getNowTimeStamp(): Long {
        val date = Calendar.getInstance().time
        return date.time / 1000
    }

    fun getTodayStartTimeStamp(): Long {
        val format = "yyyy-MM-dd"
        val date = Calendar.getInstance().time
        val timeFormat = timeFormat(date.time, format)
        return SimpleDateFormat(format).parse(timeFormat).time / 1000
    }
}