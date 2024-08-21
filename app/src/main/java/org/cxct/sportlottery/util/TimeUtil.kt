package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.TimeRangeParams
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {
    const val YMD_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val YMD_HMS_FORMAT_CHANGE_LINE = "yyyy/MM/dd\nHH:mm:ss"
    const val YMD_HMS_FORMAT_CHANGE_LINE_2 = "yyyy/MM/dd HH:mm:ss"
    const val YMD_HM_FORMAT_2 = "yyyy/MM/dd HH:mm"
    const val DMY_HM_FORMAT = "dd/MM/yyyy HH:mm"
    const val YMD_FORMAT = "yyyy-MM-dd"
    const val YMD_FORMAT_2 = "yyyy/MM/dd"
    const val MD_FORMAT = "MM-dd"
    const val HM_FORMAT = "HH:mm"
    const val HM_FORMAT_12 = "hh:mm" //小写的hh表示12小时制HH表示为24小时制
    const val HM_FORMAT_SS = "HH:mm:ss"
    const val HM_FORMAT_MS = "mm:ss"

    const val DM_HM_FORMAT = "MM/dd HH:mm"
    const val YMDE_FORMAT = "yyyy-MM-dd"
    private const val YMDE_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss"

    const val DAY_MONTH_YEAR= "dd.MM.yyyy"
    const val NEWS_TIME_FORMAT2 = "MMM dd, yyyy HH:mm:ss"
    //Feb 12, 2023 10:16AM 新闻时间格式
    const val NEWS_TIME_FORMAT = "MMM dd, yyyy h:mma"
    //Feb 12, 2023 10:16AM 新闻时间格式
    const val SELECT_MATCH_FORMAT = "MMM dd"

    const val EN_DATE_FORMAT = "MMM dd, yyyy"

    const val TIMEZONE_DEFAULT = "GMT-4"

    fun stampToDateHMS(time: Long): String {
        return timeFormat(time, YMD_HMS_FORMAT)
    }
    fun stampToDateHMSByRecord(time: Long): String {
        return timeFormat(time, YMD_HMS_FORMAT_CHANGE_LINE_2)
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
            String.format("%d", min)
        } catch (e: Exception) {
            e.printStackTrace()
            "--:--"
        }
    }

    /**
     * 時間(TimeInMillis) 轉換成 second 格式
     * @param time: TimeInMillis
     * @return :String
     */
    fun longToSecond(time: Long): String {
        return try {
            val sec = time / 1000
            String.format("%d", sec)
        } catch (e: Exception) {
            e.printStackTrace()
            "-"
        }
    }

    /**
     * 時間轉換成日期格式
     * @param time: TimeInMillis
     * @return :String
     */
    fun dateToFormat(time: Date, format: String = YMD_HMS_FORMAT): String {
        return try {
            val dateFormatter: DateFormat = SimpleDateFormat(format)
            dateFormatter.isLenient = false
            return dateFormatter.format(time)
        } catch (e: Exception) {
            e.printStackTrace()
            "-"
        }
    }

    fun timeFormatUTC4(time: Long, format: String): String {
        return timeFormat(time, format, SimpleTimeZone(-4 * 3600_000, "UTC-4"))
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
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (date.isNullOrEmpty()) return null
        val formatter = SimpleDateFormat(dateFormatPattern, locale)
        formatter.timeZone = timeZone
        val startTimeStamp = formatter.parse("$date 00:00:00 000")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59 999")?.time
        return if (timeType == TimeType.START_OF_DAY) startTimeStamp else endTimeStamp
    }
    fun dateToTimeStamp2(
        date: String?,
        timeType: TimeType = TimeType.START_OF_DAY,
        dateFormatPattern: String = YMD_HMS_FORMAT_CHANGE_LINE_2,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (date.isNullOrEmpty()) return null
        val formatter = SimpleDateFormat(dateFormatPattern, locale)
        formatter.timeZone = timeZone
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

    fun dateToDateFormat(
        date: Date?,
        newDateFormatPattern: String = MD_FORMAT
    ): String? {
        try {
            if (date == null) return null
            val newFormatter = SimpleDateFormat(newDateFormatPattern, Locale.getDefault())
            return newFormatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun setupDayOfWeekVi(context: Context,calendar: Calendar): String {
        val id = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday2
            Calendar.MONDAY -> R.string.monday2
            Calendar.TUESDAY -> R.string.tuesday2
            Calendar.WEDNESDAY -> R.string.wednesday2
            Calendar.THURSDAY -> R.string.thursday2
            Calendar.FRIDAY -> R.string.friday2
            Calendar.SATURDAY -> R.string.saturday2
            else -> R.string.sunday2
        }

        return context.getString(id)
    }

    /**
     * return : 星期幾
     */
    fun setupDayOfWeek(date: String?): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateToTimeStamp(date = date) ?: 0

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

    fun getDefaultTimeStamp(
        minusDays: Int? = 6,
        timeZone: TimeZone = TimeZone.getDefault()
    ): TimeRangeParams {
        val cPair = getCalendarForDates(minusDays)

        val minusDayTimeStamp = cPair.first.apply { this.timeZone = timeZone }.timeInMillis
        val todayTimeStamp = cPair.second.apply { this.timeZone = timeZone }.timeInMillis
        return object : TimeRangeParams {
            //TODO simon review: TimeRangeParams 裡的 startTime、endTime 同時可能代表 timeStamp 也可能代表 日期(yyyy-MM-dd)，感覺最好拆開定義
            override val startTime: String
                get() = minusDayTimeStamp.toString()
            override val endTime: String
                get() = todayTimeStamp.toString()
        }
    }

    fun getDefaultDate2(minusDays: Int? = 6): TimeRangeParams {
        val cPair = getCalendarForDates(minusDays)
        val minusDay = timeFormat(cPair.first.timeInMillis, YMD_FORMAT_2)
        val today = timeFormat(cPair.second.timeInMillis, YMD_FORMAT_2)
        return object : TimeRangeParams {
            override val startTime: String
                get() = minusDay
            override val endTime: String
                get() = today
        }
    }

    fun getNowTimeStamp(): Long {
        return System.currentTimeMillis()
    }

    private fun getTodayStartTimeCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun getTodayEndTimeCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
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
            override val startTime = getTodayStartTimeStamp().toString()
            override val endTime = getTodayEndTimeStamp().toString()
        }
    }

    fun getCalendarForDates(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = getTodayEndTimeCalendar()
        val minusDaysCalendar = getTodayStartTimeCalendar()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair(minusDaysCalendar, todayCalendar)
    }

    fun getDayDateTimeRangeParams(
        date: String,
        locale: Locale = Locale.getDefault()
    ): TimeRangeParams {
        //指定日期 00:00:00 ~ 23:59:59:59
        //date : yyyy-MM-dd
        return object : TimeRangeParams {
            override val startTime = dateToTimeStamp(
                date,
                TimeType.START_OF_DAY,
                dateFormatPattern = YMDE_HMS_FORMAT,
                locale = locale
            ).toString()
            override val endTime = dateToTimeStamp(
                date,
                TimeType.END_OF_DAY,
                dateFormatPattern = YMDE_HMS_FORMAT,
                locale = locale
            ).toString()
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
    fun getInHrRangeParams(hours: Int): TimeRangeParams {
        val calendar = Calendar.getInstance()
        val startTimeStamp = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        val endTimeStamp = calendar.timeInMillis

        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()
        }
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

    //[Martin] 這會回傳剩餘幾天
    fun getRemainDay(timeStamp: Long?): Int {
        var remainTime = 0L
        var day = 0
        try {
            timeStamp?.apply {
                remainTime = timeStamp - System.currentTimeMillis()
                day = (remainTime / (1000 * 60 * 60 * 24)).toInt() + 1
            }
        } catch (e: Exception) {
            Timber.e("時間計算失敗!!! \n$e")
            e.printStackTrace()
        }
        return day
    }

    /**
     * return: 時間是否為今日
     */

    fun isTimeToday(timeMillis: Long?): Boolean {
        val todayTimeMillis = Calendar.getInstance().timeInMillis

        return timeFormat(timeMillis ?: 0, YMD_FORMAT) == timeFormat(todayTimeMillis, YMD_FORMAT)
    }

    fun isTimeInPlay(timeStamp: Long?): Boolean {
        return getRemainTime(timeStamp) <= 0
    }

    fun isTimeAtStart(timeStamp: Long?): Boolean {
        return (getRemainTime(timeStamp) < 60 * 60 * 1000L) && getRemainTime(timeStamp) > 0
    }

    /**
     * 获取上下午时间 格式为 hh:mm:ssAM
     */
    fun getDateFormat12(time:Long):String{
        var times = ""
        var cal=Calendar.getInstance()
        cal.timeInMillis = time
        times = if (cal.get(Calendar.AM_PM) == 0){
            "AM"
        }else {
            "PM"
        }
        val timeFormat = timeFormat(time, HM_FORMAT_12)
        return "$timeFormat $times"
    }

    /**
     * Date转换字符串 年月日转换
     */
    fun dateToStringFormatYMD(
        date: Date?,
        format: String = YMD_FORMAT
    ): String? {
        try {
            if (date == null) return null
            val newFormatter = SimpleDateFormat(format, Locale.getDefault())
            return newFormatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun timeStampToDateString(
        date: Long?,
        newDateFormatPattern: String,
        locale: Locale = Locale.getDefault(),
    ): String? {
        try {
            if (date == null) return null
            val newFormatter = SimpleDateFormat(newDateFormatPattern, locale)
            return newFormatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Date转换字符串 时分秒转换
     */
    fun dateToStringFormatHMS(
        date: Date?,

    ): String? {
        val timeZoneGTM = try {
            val gtm = SimpleDateFormat("Z").format(Date().time).toInt().div(100)
            if (gtm < 0)
                "$gtm"
            else
                "+$gtm"
        } catch (e: NumberFormatException) {
            ""
        }
        try {
            if (date == null) return null
            val newDateFormatPattern: String = HM_FORMAT_SS
            val newFormatter = SimpleDateFormat(newDateFormatPattern, Locale.getDefault())
            return newFormatter.format(date) + " (GMT$timeZoneGTM)"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 用于倒计时显示剩余时间时分秒格式
     */
    fun showCountDownHMS(millisUntilFinished: Long):String{
        val hours = (millisUntilFinished / 1000) / 3600
        val minutes = (millisUntilFinished / 1000 % 3600) / 60
        val seconds = (millisUntilFinished / 1000 % 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}