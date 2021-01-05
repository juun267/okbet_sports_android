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

    enum class TimeType {
        START, END
    }
    fun dateToTimeStamp(date: String, timeType: TimeType = TimeType.START): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeStamp = formatter.parse("$date 00:00:00")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59")?.time
        return if (timeType == TimeType.START) startTimeStamp else endTimeStamp
    }

    fun getNowTimeStamp(): Long {
        return System.currentTimeMillis()
    }

    fun getTodayStartTimeStamp(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getOneWeekDate(): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val format = "yyyy-MM-dd"

        repeat(7) {
            calendar.add(Calendar.DATE, 1)
            weekDateList.add(timeFormat(calendar.time.time, format))
        }
        return weekDateList
    }

    fun getOneWeekDate(): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val format = "yyyy-MM-dd"

        repeat(7) {
            calendar.add(Calendar.DATE, 1)
            weekDateList.add(timeFormat(calendar.time.time, format))
        }
        return weekDateList
    }

    fun getOneWeekDate(): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val format = "yyyy-MM-dd"

        repeat(7) {
            calendar.add(Calendar.DATE, 1)
            weekDateList.add(timeFormat(calendar.time.time, format))
        }
        return weekDateList
    }
}