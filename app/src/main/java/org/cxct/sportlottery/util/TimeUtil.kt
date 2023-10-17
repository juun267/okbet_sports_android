package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.TimeRangeParams
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {
    const val YMD_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val YMD_HMS_FORMAT_CHANGE_LINE = "yyyy/MM/dd\nHH:mm:ss"
    const val YMD_HMS_FORMAT_CHANGE_LINE_2 = "yyyy/MM/dd HH:mm:ss"
    const val YMD_HM_FORMAT = "yyyy-MM-dd HH:mm"
    const val YMD_HM_FORMAT_2 = "yyyy/MM/dd HH:mm"
    const val YMD_FORMAT = "yyyy-MM-dd"
    const val YMD_FORMAT_2 = "yyyy/MM/dd"
    const val DMY_FORMAT = "yyyy / MM / dd /"
    const val MD_FORMAT = "MM-dd"
    const val DAY_FORMAT = "d"
    const val VI_MD_FORMAT = "dd 'TH.'M"
    const val DM_FORMAT = "MM / dd"
    const val HM_FORMAT = "HH:mm"
    const val HM_FORMAT_12 = "hh:mm" //小写的hh表示12小时制HH表示为24小时制
    const val HM_FORMAT_SS = "HH:mm:ss"
    const val HM_FORMAT_MS = "mm:ss"
    const val HM_FORMAT_SS_12 = "hh:mm:ss"
    const val MD_HMS_FORMAT = "MM-dd HH:mm:ss"
    const val DM_HM_FORMAT = "MM/dd HH:mm"
    const val YMDE_FORMAT = "yyyy-MM-dd"
    private const val YMDE_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val YMDE_FORMAT_1 = "yyyy-MM/dd-EEE"
    const val D_NARROW_MONTH = "dd/MMM"
    const val DAY_MONTH_YEAR= "dd.MM.yyyy"
    const val NEWS_TIME_FORMAT2 = "MMM dd, yyyy HH:mm:ss"
    //Feb 12, 2023 10:16AM 新闻时间格式
    const val NEWS_TIME_FORMAT = "MMM dd, yyyy h:mma"
    //Feb 12, 2023 10:16AM 新闻时间格式
    const val SELECT_MATCH_FORMAT = "MMM dd"

    const val EN_DATE_FORMAT = "MMM dd, yyyy"


    //    private const val YMDE_HMS_FORMAT = "yyyy-MM/dd-EEE HH:mm:ss"
    private const val DMY_HM_FORMAT = "yyyy-MM-dd HH:mm"
    private const val BIRTHDAY_FORMAT = "yyyy / MM / dd"
    const val HM_FORMAT_SSS = "HH:mm:ss:SSS"

    const val TIMEZONE_DEFAULT = "GMT-4"

    fun stampToDateHMS(time: Long): String {
        return timeFormat(time, YMD_HMS_FORMAT)
    }
    fun stampToDateHMSByRecord(time: Long): String {
        return timeFormat(time, YMD_HMS_FORMAT_CHANGE_LINE_2)
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
     * 生日專用格式
     */
    fun stampToRegisterBirthdayFormat(time: Date): String {
        return timeFormat(time.time, BIRTHDAY_FORMAT)
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

    /**
     * 時間(TimeInMillis) 轉換成 minute 格式 (倒數)
     * @param time: TimeInMillis
     * @return :String
     */
    fun longToCountDownMin(time: Long): String {
        return try {
            val min = time / 1000 / 60 + 60000 //倒數60秒顯示為1分鐘，因此加1分鐘
            String.format("%d", min)
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
        timeZone: TimeZone? = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (date.isNullOrEmpty()) return null
        val formatter = SimpleDateFormat("$dateFormatPattern", locale)
        formatter.timeZone = timeZone
        val startTimeStamp = formatter.parse("$date 00:00:00 000")?.time
        val endTimeStamp = formatter.parse("$date 23:59:59 999")?.time
        return if (timeType == TimeType.START_OF_DAY) startTimeStamp else endTimeStamp
    }
    fun dateToTimeStamp2(
        date: String?,
        timeType: TimeType = TimeType.START_OF_DAY,
        dateFormatPattern: String = YMD_HMS_FORMAT_CHANGE_LINE_2,
        timeZone: TimeZone? = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): Long? {
        if (date.isNullOrEmpty()) return null
        val formatter = SimpleDateFormat("$dateFormatPattern", locale)
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

    /**
     * return : 星期幾
     */
    fun setupDayOfWeekByCal(context: Context,calendar: Calendar): String {
        val id = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> R.string.sunday
            Calendar.MONDAY -> R.string.monday
            Calendar.TUESDAY -> R.string.tuesday
            Calendar.WEDNESDAY -> R.string.wednesday
            Calendar.THURSDAY -> R.string.thursday
            Calendar.FRIDAY -> R.string.friday
            Calendar.SATURDAY -> R.string.saturday
            else -> R.string.sunday
        }

        return context.getString(id)
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

    fun monthFormat(context: Context, todayMillis: Long?): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayMillis ?: 0

        val id = when (calendar.get(Calendar.MONTH)) {
            Calendar.JANUARY -> R.string.january
            Calendar.FEBRUARY -> R.string.february
            Calendar.MARCH -> R.string.march
            Calendar.APRIL -> R.string.april
            Calendar.MAY -> R.string.may
            Calendar.JUNE -> R.string.june
            Calendar.JULY -> R.string.july
            Calendar.AUGUST -> R.string.august
            Calendar.SEPTEMBER -> R.string.september
            Calendar.OCTOBER -> R.string.october
            Calendar.NOVEMBER -> R.string.november
            Calendar.DECEMBER -> R.string.december
            else -> R.string.january
        }

        return context.getString(id)
    }

    /**
     * return : 週幾
     */
//    fun setupDayOfWeekAndToday(todayMillis: Long?): Int {
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = todayMillis ?: 0
//
//        val todayTimeMillis = Calendar.getInstance().timeInMillis
//
//        return if (timeFormat(todayMillis, YMD_FORMAT) == timeFormat(todayTimeMillis, YMD_FORMAT)) {
//            R.string.home_tab_today
//        } else {
//            when (calendar.get(Calendar.DAY_OF_WEEK)) {
//                Calendar.SUNDAY -> R.string.sunday2
//                Calendar.MONDAY -> R.string.monday2
//                Calendar.TUESDAY -> R.string.tuesday2
//                Calendar.WEDNESDAY -> R.string.wednesday2
//                Calendar.THURSDAY -> R.string.thursday2
//                Calendar.FRIDAY -> R.string.friday2
//                Calendar.SATURDAY -> R.string.saturday2
//                else -> R.string.sunday2
//            }
//        }
//
//    }

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
        timeZone: TimeZone? = TimeZone.getDefault()
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

    fun getMinusDateTimeStamp(minusDays: Int? = 0): TimeRangeParams {
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

    /**
     * 帳戶歷史時間以美東時間做計算
     */
    fun getAccountHistoryTimeRangeParams(): TimeRangeParams {
        val endTimeCalendar = getTodayEndTimeCalendar()
        val startTimeCalendar = getTodayStartTimeCalendar()

        val eUSTimeZone = TimeZone.getTimeZone(TIMEZONE_DEFAULT)
        endTimeCalendar.timeZone = eUSTimeZone
        startTimeCalendar.timeZone = eUSTimeZone
        startTimeCalendar.add(Calendar.DATE, -7)

        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeCalendar.timeInMillis.toString()
            override val endTime: String
                get() = endTimeCalendar.timeInMillis.toString()

        }
    }


    fun getEarlyAllTimeRangeParams(): TimeRangeParams {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTimeStamp = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 30)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTimeStamp = calendar.timeInMillis

        return object : TimeRangeParams {
            override val startTime = startTimeStamp.toString()
            override val endTime = endTimeStamp.toString()
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

    fun getFutureDate(day: Int, locale: Locale = Locale.getDefault()): List<String> {
        val weekDateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        repeat(day) {
            if (it == 0)
                calendar.add(Calendar.DATE, 0)
            else
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

    fun getRemainMinute(timeStamp: Long?): Int {
        var remainTime = 0L
        var minute = 0
        try {
            timeStamp?.apply {
                remainTime = timeStamp - System.currentTimeMillis()
                minute = (remainTime / (1000 * 60)).toInt()
            }
        } catch (e: Exception) {
            Timber.e("時間計算失敗!!! \n$e")
            e.printStackTrace()
        }
        return minute
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

    fun isLastHour(timeStamp: Long?): Boolean {
        return (getRemainMinute(timeStamp) <= 60) && getRemainMinute(timeStamp) > 0
    }

    fun stampToDateHM(time: Long): String {
        return timeFormat(time, DMY_HM_FORMAT)
    }

    fun stampToViMD(time: Long): String {
        return timeFormat(time, VI_MD_FORMAT)
    }

    /**
     * 当前时间
     */

    fun nowTime(format: String?): String {
        return SimpleDateFormat(format).format(Date(System.currentTimeMillis()))
    }

    /**
     * 比较2个时间
     */
    fun dateDiffDay(startTime: String?, endTime: String?, format: String?): Long {
        if (startTime == null || endTime == null) {
            return 0
        }
        // 按照传入的格式生成一个simpledateformate对象
        val sd = SimpleDateFormat(format)
        val nd = (1000 * 24 * 60 * 60).toLong() // 一天的毫秒数
        val nh = (1000 * 60 * 60).toLong() // 一小时的毫秒数
        val nm = (1000 * 60).toLong() // 一分钟的毫秒数
        val ns: Long = 1000 // 一秒钟的毫秒数
        val diff: Long
        var day: Long = 0
        try {
            // 获得两个时间的毫秒时间差异
            diff = (sd.parse(endTime).time
                    - sd.parse(startTime).time)
            day = diff / nd // 计算差多少天
            val hour = diff % nd / nh // 计算差多少小时
            val min = diff % nd % nh / nm // 计算差多少分钟
            val sec = diff % nd % nh % nm / ns // 计算差多少秒
            // 输出结果
            return (day * 24 * 60 * 60) + (hour * 60 * 60) + (min * 60) + sec
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0

    }

    /**
     * 时间前推或后推秒,
     */

    fun getPreTime(nowTime: String, postponeTime: Long, format: String?): String {
        var format = SimpleDateFormat(format);
        var mydate1 = "";
        try {
            var date = format.parse(nowTime);
            var time = (date.getTime() / 1000) + postponeTime
            date.setTime(time * 1000);
            mydate1 = format.format(date);
        } catch (e: ParseException) {
        }
        return mydate1;
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
     * @strTime 为string类型的时间
     * @formatType 为要转换的格式 yyy-MM-dd HH:mm:ss
     * 返回值为一个Date类型的值
     */
    fun getStringFormatDate(strTime: String, formatType: String): Date {
        val simpleDateFormat = SimpleDateFormat(formatType)
        return simpleDateFormat.parse(strTime)
    }

    /**
     * Date转换字符串 年月日转换
     */
    fun dateToStringFormatYMD(
        date: Date?,
    ): String? {
        try {
            if (date == null) return null
            val   newDateFormatPattern: String = YMD_FORMAT
            val newFormatter = SimpleDateFormat(newDateFormatPattern, Locale.getDefault())
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
     * 將日期使用的分隔符號從"-"改為"/"
     */
    fun String.formatDateDelimiter(): String {
        return this.replace("-", "/")
    }

    /**
     * oldDate 2020-01-30
     * oldF yyyy-MM-dd
     * newF MM-dd-yyyy
     */
    fun changeDateFormat(oldDate: String,oldF: String,newF: String):String{
        var sdf = SimpleDateFormat("yyyy-MM-dd")
        val date: Date = sdf.parse(oldDate)
        sdf = SimpleDateFormat("MM/dd/yyyy")
        val yourFormatedDateString = sdf.format(date)
        return yourFormatedDateString.toString()
    }
}