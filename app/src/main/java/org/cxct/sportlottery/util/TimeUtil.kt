package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.os.Build
import org.cxct.sportlottery.network.common.TimeRangeParams
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {
    private const val TAG = "TimeUtil"
    private val ymdhmsFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val ymdFormat = SimpleDateFormat("yyyy-MM-dd")

    fun timeStampToDate(time: Long?): String? {
        if (time == null) return null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return simpleDateFormat.format(time)
    }

    fun timeStampToDay(time: Long?): String? {
        if (time == null) return null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(time)
    }

    @JvmStatic
    fun stampToDate(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm")
        return simpleDateFormat.format(Date(time))
    }

    @JvmStatic
    fun stampToDateInOddsDetail(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("MM-dd  HH:mm")
        return simpleDateFormat.format(Date(time))
    }

    @JvmStatic
    fun stampToDateHMS(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
        return simpleDateFormat.format(Date(time))
    }

    @JvmStatic
    fun stampToDateHMS(time: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
        return simpleDateFormat.format(time)
    }

    @JvmStatic
    fun stampToTimeHMS(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        return simpleDateFormat.format(Date(time))
    }

    fun stampToDateHMSTimeZone(time: Long): String {
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
            val currentLocalTime = calendar.time
            //Android 6.0以下會Crash
            val timeZoneFormat: SimpleDateFormat?
            val timeZoneGTM: Int?
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                timeZoneFormat = SimpleDateFormat("Z")
                timeZoneGTM = timeZoneFormat.format(currentLocalTime).toInt()
            } else {
                timeZoneFormat = SimpleDateFormat("X")
                timeZoneGTM = timeZoneFormat.format(currentLocalTime).toInt()
            }
            return simpleDateFormat.format(Date(time)) + " (GMT+" + timeZoneGTM + ")"
        }catch (e: Exception){
            e.printStackTrace()
            return ""
        }
    }

    fun stampToDateHMSTimeZone(time: Date): String {
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss")
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
            val currentLocalTime = calendar.time
            //Android 6.0以下會Crash
            val timeZoneFormat: SimpleDateFormat?
            val timeZoneGTM: Int?
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                timeZoneFormat = SimpleDateFormat("Z")
                timeZoneGTM = timeZoneFormat.format(currentLocalTime).toInt()
            } else {
                timeZoneFormat = SimpleDateFormat("X")
                timeZoneGTM = timeZoneFormat.format(currentLocalTime).toInt()
            }
            return simpleDateFormat.format(time) + " (GMT+" + timeZoneGTM + ")"
        }catch (e: Exception){
            e.printStackTrace()
            return ""
        }
    }

    @JvmStatic
    fun dateToStamp(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date).time
    }

    @JvmStatic
    fun stampToDateTime(date: Date): String {
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
        if (date.isEmpty()) return null
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeStamp = formatter.parse("$date 00:00:00")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59")?.time
        return if (timeType == TimeType.START_OF_DAY) startTimeStamp else endTimeStamp
    }

    fun getDefaultTimeStamp(): TimeRangeParams {
        val minusDay = ymdFormat.format(getCalendarForDates(6).first.time)
        val today = ymdFormat.format(getCalendarForDates(6).second.time)
        val startTimeStamp = ymdhmsFormat.parse("$minusDay 00:00:00")?.time
        val endTimeStamp = ymdhmsFormat.parse("$today 23:59:59")?.time

        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()

        }
    }

    fun getDefaultDate(): TimeRangeParams {
        val minusDay = ymdFormat.format(getCalendarForDates(6).first.time)
        val today = ymdFormat.format(getCalendarForDates(6).second.time)
        return object : TimeRangeParams {
            override val startTime: String
                get() = minusDay.toString()
            override val endTime: String
                get() = today.toString()

        }
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

    fun getCalendarForDates(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = getTodayEndTimeCalendar()
        val minusDaysCalendar = getTodayStartTimeCalendar()
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

    fun getRemainTime(timeStamp: Long): Long {
        val calendar = Calendar.getInstance()
        val startTimeStamp = calendar.timeInMillis

        return timeStamp - startTimeStamp
    }

    fun toCalendar(date: Date?): Calendar? {
        val c = Calendar.getInstance()
        c.time = date
        return c
    }
}