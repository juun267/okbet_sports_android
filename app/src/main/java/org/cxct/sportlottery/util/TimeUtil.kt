package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import org.cxct.sportlottery.network.common.TimeRangeParams
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
    fun stampToDateHMS(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
        return simpleDateFormat.format(Date(time))
    }

    fun stampToDateHMSTimeZone(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
        return simpleDateFormat.format(Date(time)) + " (" + TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT) + ")"
    }

    @JvmStatic
    fun dateToStamp(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date).time
    }

    @JvmStatic
    fun stampToDateTime(date:Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(date)
    }

    @JvmStatic
    fun timeFormat(
        time: Long?,
        format: String,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
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
        START_OF_DAY, END_OF_DAY
    }

    fun dateToTimeStamp(date: String, timeType: TimeType = TimeType.START_OF_DAY): Long? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeStamp = formatter.parse("$date 00:00:00")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59")?.time
        return if (timeType == TimeType.START_OF_DAY) startTimeStamp else endTimeStamp
    }

    fun getNowTimeStamp(): Long {
        return System.currentTimeMillis()
    }

    fun getTodayStartTimeCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun getTodayEndTimeCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 59)
        return calendar
    }

    fun getTodayStartTimeStamp(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getTodayEndTimeStamp(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 59)
        return c.timeInMillis
    }

    fun getTodayTimeRangeParams(): TimeRangeParams {
        //match type 今日 00:00:00 ~ 23:59:59:59
        return object : TimeRangeParams {
            override val startTime: String
                get() = getTodayStartTimeStamp().toString()
            override val endTime: String
                get() = getTodayEndTimeStamp().toString()
        }
    }

    fun getDateInCalendar(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = TimeUtil.getTodayEndTimeCalendar()
        val minusDaysCalendar = TimeUtil.getTodayStartTimeCalendar()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair(minusDaysCalendar, todayCalendar)
    }

    fun getDayDateTimeRangeParams(date: String): TimeRangeParams {
        //指定日期 00:00:00 ~ 23:59:59:59
        //date : yyyy-MM-dd
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val startTimeStamp = formatter.parse("$date 00:00:00")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59")?.time
        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()
        }
    }

    fun getOtherEarlyDateTimeRangeParams(): TimeRangeParams {
        //match type 早盤 串關 其他早盤 第7日00:00:00 ～ 第30日23:59:59
        //date : yyyy-MM-dd
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_MONTH, 8)
        val startTimeStamp = c.timeInMillis

        c.add(Calendar.DAY_OF_MONTH, 22)
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 59)
        val endTimeStamp = c.timeInMillis

        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()
        }
    }

    fun getParlayAllTimeRangeParams(): TimeRangeParams {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 59)

        return object : TimeRangeParams {
            override val startTime: String?
                get() = null
            override val endTime: String
                get() = calendar.timeInMillis.toString()
        }
    }

    fun getParlayTodayTimeRangeParams(): TimeRangeParams {
        return object : TimeRangeParams {
            override val startTime: String?
                get() = null
            override val endTime: String
                get() = getTodayEndTimeStamp().toString()
        }
    }

    fun getAtStartTimeRangeParams(): TimeRangeParams {
        val calendar = Calendar.getInstance()
        val startTimeStamp = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, 60)
        val endTimeStamp = calendar.timeInMillis

        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()
        }
    }

    fun getFutureDate(day: Int): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val format = "yyyy-MM-dd"

        repeat(day) {
            calendar.add(Calendar.DATE, 1)
            weekDateList.add(timeFormat(calendar.time.time, format))
        }
        return weekDateList
    }
}