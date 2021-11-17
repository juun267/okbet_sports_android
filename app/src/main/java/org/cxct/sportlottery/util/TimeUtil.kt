package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.util.Log
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.TimeRangeParams
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {
    const val YMD_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val YMD_FORMAT = "yyyy-MM-dd"
    const val DMY_FORMAT = "dd / MM / yyyy"
    const val MD_FORMAT = "MM-dd"
    const val MD_FORMAT_2 = "M-dd"
    const val VI_MD_FORMAT = "dd 'TH.'M"
    const val DM_FORMAT = "dd / MM"
    const val HM_FORMAT = "HH:mm"
    const val MD_HMS_FORMAT = "MM-dd HH:mm:ss"
    private const val YMDE_FORMAT = "yyyy-MMMM-d-EEE"
    private const val YMDE_HMS_FORMAT = "yyyy-MMMM-d-EEE HH:mm:ss"
    private const val DMY_HM_FORMAT = "MM-dd-yyyy HH:mm"

    fun stampToDateHMS(time: Long): String {
        return timeFormat(time, YMD_HMS_FORMAT)
    }

    fun stampToDateHMSTimeZone(time: Long): String {
        val timeZoneGTM = try {
            val gtm = SimpleDateFormat("Z").format(time).toInt().div(100)
            if (gtm < 0)
                "$gtm"
            else
                "+$gtm"
        } catch (e: NumberFormatException) {
            ""
        }
        return timeFormat(time, YMD_HMS_FORMAT) + " (GMT$timeZoneGTM)"
    }

    fun stampToDateHMSTimeZone(time: Date): String {
        return stampToDateHMSTimeZone(time.time)
    }

    /**
     * 時間(TimeInMillis) 轉換成 mm:ss 格式
     * @param time: TimeInMillis
     * @return :String
     */
    fun longToMmSs(time: Long?): String {
        return try {
            if (time == null) {
                "--:--"
            } else {
                val min = time / 1000 / 60
                val sec = time / 1000 % 60
                String.format("%02d:%02d", min, sec)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "--:--"
        }
    }

    /**
     * 時間(TimeInMillis) 轉換成 minute 格式
     * @param time: TimeInMillis
     * @return :String
     */
    fun longToMinute(time: Long): String {
        return try {
            val min = time / 1000 / 60
            String.format("%02d", min)
        } catch (e: Exception) {
            e.printStackTrace()
            "--:--"
        }
    }

    fun timeFormat(
        time: Long?,
        format: String,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        var formattedTime = ""
        try {
            val dateFormat = SimpleDateFormat(format, locale)
            dateFormat.timeZone = timeZone
            formattedTime = dateFormat.format(time)
        } catch (e: Exception) {
            Timber.e("解析日期失敗!!! \n$e")
            e.printStackTrace()
        }
        return formattedTime
    }

    enum class TimeType {
        START_OF_DAY, END_OF_DAY
    }

    fun dateToTimeStamp(
        date: String?,
        timeType: TimeType = TimeType.START_OF_DAY,
        dateFormatPattern: String = YMD_HMS_FORMAT,
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (date.isNullOrEmpty()) return null
        val formatter = SimpleDateFormat("$dateFormatPattern S", locale)
        val startTimeStamp = formatter.parse("$date 00:00:00 000")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59 999")?.time
        return if (timeType == TimeType.START_OF_DAY) startTimeStamp else endTimeStamp
    }

    fun dateToDateFormat(
        date: String?,
        newDateFormatPattern: String = MD_FORMAT
    ): String? {
        try {
            if (date.isNullOrEmpty()) return null
            val newFormatter = SimpleDateFormat(newDateFormatPattern, Locale.getDefault())
            return newFormatter.format(dateToTimeStamp(date))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * return : 星期幾
     */
    fun setupDayOfWeek(todayMillis: Long?): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayMillis ?:0

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday
            Calendar.MONDAY -> R.string.monday
            Calendar.TUESDAY -> R.string.tuesday
            Calendar.WEDNESDAY -> R.string.wednesday
            Calendar.THURSDAY -> R.string.thursday
            Calendar.FRIDAY -> R.string.friday
            Calendar.SATURDAY -> R.string.saturday
            else -> R.string.sunday
        }
    }

    fun setupDayOfWeekVi(todayMillis: Long?): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayMillis ?:0

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday2
            Calendar.MONDAY -> R.string.monday2
            Calendar.TUESDAY -> R.string.tuesday2
            Calendar.WEDNESDAY -> R.string.wednesday2
            Calendar.THURSDAY -> R.string.thursday2
            Calendar.FRIDAY -> R.string.friday2
            Calendar.SATURDAY -> R.string.saturday2
            else -> R.string.sunday2
        }
    }

    /**
     * return : 週幾
     */
    fun setupDayOfWeekAndToday(todayMillis: Long?): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayMillis ?:0

        val todayTimeMillis = Calendar.getInstance().timeInMillis

        return if (timeFormat(todayMillis, YMD_FORMAT) == timeFormat(todayTimeMillis, YMD_FORMAT)) {
            R.string.home_tab_today
        } else {
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> R.string.sunday2
                Calendar.MONDAY -> R.string.monday2
                Calendar.TUESDAY -> R.string.tuesday2
                Calendar.WEDNESDAY -> R.string.wednesday2
                Calendar.THURSDAY -> R.string.thursday2
                Calendar.FRIDAY -> R.string.friday2
                Calendar.SATURDAY -> R.string.saturday2
                else -> R.string.sunday2
            }
        }

    }

    /**
     * return : 星期幾
     */
    fun setupDayOfWeek(date: String?): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateToTimeStamp(date = date) ?:0

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday
            Calendar.MONDAY -> R.string.monday
            Calendar.TUESDAY -> R.string.tuesday
            Calendar.WEDNESDAY -> R.string.wednesday
            Calendar.THURSDAY -> R.string.thursday
            Calendar.FRIDAY -> R.string.friday
            Calendar.SATURDAY -> R.string.saturday
            else -> R.string.sunday
        }
    }

    fun getDefaultTimeStamp(minusDays: Int? = 6): TimeRangeParams {
        val cPair = getCalendarForDates(minusDays)
        val minusDayTimeStamp = cPair.first.timeInMillis
        val todayTimeStamp = cPair.second.timeInMillis
        return object : TimeRangeParams {
            //TODO simon review: TimeRangeParams 裡的 startTime、endTime 同時可能代表 timeStamp 也可能代表 日期(yyyy-MM-dd)，感覺最好拆開定義
            override val startTime: String
                get() = minusDayTimeStamp.toString()
            override val endTime: String
                get() = todayTimeStamp.toString()
        }
    }

    fun getDefaultDate(minusDays: Int? = 6): TimeRangeParams {
        val cPair = getCalendarForDates(minusDays)
        val minusDay = timeFormat(cPair.first.timeInMillis, YMD_FORMAT)
        val today = timeFormat(cPair.second.timeInMillis, YMD_FORMAT)
        return object : TimeRangeParams {
            override val startTime: String
                get() = minusDay
            override val endTime: String
                get() = today
        }
    }

    fun getMinusDateTimeStamp(minusDays: Int ?= 0): TimeRangeParams {
        val cPair = getCalendarForDates(minusDays)
        val minusDayTimeStamp = cPair.first.timeInMillis
        val todayTimeStamp = cPair.second.timeInMillis
        return object : TimeRangeParams {
            //TODO simon review: TimeRangeParams 裡的 startTime、endTime 同時可能代表 timeStamp 也可能代表 日期(yyyy-MM-dd)，感覺最好拆開定義
            override val startTime: String
                get() = minusDayTimeStamp.toString()
            override val endTime: String
                get() = todayTimeStamp.toString()
        }
    }

    fun getMinusDate(minusDays: Int, dateFormatPattern: String = MD_FORMAT): String {
        val mCalendar = getCalendarForDates(minusDays)
        return timeFormat(mCalendar.first.timeInMillis, dateFormatPattern)
    }

    fun getMinusDayOfWeek(minusDays: Int): Int {
        val mCalendar = getCalendarForDates(minusDays)

        return when (mCalendar.first.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday
            Calendar.MONDAY -> R.string.monday
            Calendar.TUESDAY -> R.string.tuesday
            Calendar.WEDNESDAY -> R.string.wednesday
            Calendar.THURSDAY -> R.string.thursday
            Calendar.FRIDAY -> R.string.friday
            Calendar.SATURDAY -> R.string.saturday
            else -> R.string.sunday
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
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar
    }

    fun getTodayStartTimeStamp(): Long {
        return getTodayStartTimeCalendar().timeInMillis
    }

    fun getTodayEndTimeStamp(): Long {
        return getTodayEndTimeCalendar().timeInMillis
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

    fun getDayDateTimeRangeParams(date: String, locale: Locale = Locale.getDefault()): TimeRangeParams {
        //指定日期 00:00:00 ~ 23:59:59:59
        //date : yyyy-MM-dd
        return object : TimeRangeParams {
            override val startTime: String
                get() = dateToTimeStamp(
                    date,
                    TimeType.START_OF_DAY,
                    dateFormatPattern = YMDE_HMS_FORMAT,
                    locale = locale
                ).toString()
            override val endTime: String
                get() = dateToTimeStamp(
                    date,
                    TimeType.END_OF_DAY,
                    dateFormatPattern = YMDE_HMS_FORMAT,
                    locale = locale
                ).toString()
        }
    }


    fun getEarlyAllTimeRangeParams(): TimeRangeParams {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTimeStamp = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 29)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTimeStamp = calendar.timeInMillis

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
        c.add(Calendar.DAY_OF_MONTH, 7)
        val startTimeStamp = c.timeInMillis

        c.add(Calendar.DAY_OF_MONTH, 23)
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 0)
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
        calendar.set(Calendar.MILLISECOND, 999)

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

    fun getFutureDate(day: Int, locale: Locale = Locale.getDefault()): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        repeat(day) {
            calendar.add(Calendar.DATE, 1)
            weekDateList.add(timeFormat(calendar.timeInMillis, YMDE_FORMAT, locale = locale))
        }
        return weekDateList
    }

    fun getRemainTime(timeStamp: Long?): Long {
        var remainTime = 0L
        try {
            timeStamp?.apply {
                remainTime = timeStamp - System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Timber.e("時間計算失敗!!! \n$e")
            e.printStackTrace()
        }
        return remainTime
    }

    /**
     * return: 時間是否為今日
     */
    fun isTimeToday(timeMillis: Long?): Boolean {
        val todayTimeMillis = Calendar.getInstance().timeInMillis

        return timeFormat(timeMillis ?: 0, YMD_FORMAT) == timeFormat(todayTimeMillis, YMD_FORMAT)
    }

    fun isTimeAtStart(timeStamp: Long?): Boolean{
        return (getRemainTime(timeStamp) < 60 * 60 * 1000L) && getRemainTime(timeStamp) > 0
    }

    fun stampToDateHM(time: Long): String {
        return timeFormat(time, DMY_HM_FORMAT)
    }

    fun stampToMD(time: Long): String {
        return timeFormat(time, MD_FORMAT_2)
    }

    fun stampToViMD(time: Long): String {
        return timeFormat(time, VI_MD_FORMAT)
    }

}