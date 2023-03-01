package com.archit.calendardaterangepicker.customviews

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.archit.calendardaterangepicker.R
import com.archit.calendardaterangepicker.R.layout
import com.archit.calendardaterangepicker.manager.LanguageManager
import com.archit.calendardaterangepicker.manager.LanguageManager.Language
import com.archit.calendardaterangepicker.models.CalendarStyleAttrImpl
import com.archit.calendardaterangepicker.models.CalendarStyleAttributes
import java.text.DateFormatSymbols
import java.util.*

class DateRangeCalendarView : LinearLayout, DateRangeCalendarViewApi {
    private lateinit var tvYearTitle: CustomTextView
    private lateinit var imgVNavLeft: AppCompatImageView
    private lateinit var imgVNavRight: AppCompatImageView
    private lateinit var adapterEventCalendarMonths: AdapterEventCalendarMonths
    private lateinit var locale: Locale
    private val selectedLocale: Language? by lazy { LanguageManager.getSelectLanguage(context) }
    private lateinit var vpCalendar: ViewPager
    private lateinit var calendarStyleAttr: CalendarStyleAttributes
    private lateinit var mDateRangeCalendarManager: CalendarDateRangeManagerImpl

    constructor(context: Context) : super(context) {
        setupLocale()
        initViews(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupLocale()
        initViews(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupLocale()
        initViews(context, attrs)
    }

    private fun initViews(context: Context, attrs: AttributeSet?) {
        locale = when (selectedLocale) {
            Language.ZH, Language.ZHT -> Locale.CHINESE
            Language.VI -> Locale("vi")
            Language.PHI -> Locale("phi")
            null -> Locale.getDefault()
            else -> Locale.US
        }
        calendarStyleAttr = CalendarStyleAttrImpl(context, attrs)
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(layout.layout_calendar_container, this, true)
        val rlHeaderCalendar = findViewById<LinearLayout>(R.id.rlHeaderCalendar)
        rlHeaderCalendar.background = calendarStyleAttr.headerBg
        tvYearTitle = findViewById(R.id.tvYearTitle)
        tvYearTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarStyleAttr.textSizeTitle)
        imgVNavLeft = findViewById(R.id.imgVNavLeft)
        imgVNavRight = findViewById(R.id.imgVNavRight)
        vpCalendar = findViewById(R.id.vpCalendar)
        val defStartMonth = Calendar.getInstance().clone() as Calendar
        defStartMonth.add(Calendar.MONTH, -TOTAL_ALLOWED_MONTHS)
        val defEndMonth = Calendar.getInstance().clone() as Calendar
        defEndMonth.add(Calendar.MONTH, TOTAL_ALLOWED_MONTHS)
        mDateRangeCalendarManager = CalendarDateRangeManagerImpl(defStartMonth, defEndMonth, calendarStyleAttr)
        adapterEventCalendarMonths = AdapterEventCalendarMonths(context, mDateRangeCalendarManager, calendarStyleAttr)
        vpCalendar.adapter = adapterEventCalendarMonths
        vpCalendar.offscreenPageLimit = 0
        vpCalendar.currentItem = TOTAL_ALLOWED_MONTHS
        setCalendarYearTitle(TOTAL_ALLOWED_MONTHS)
        setListeners()
    }

    private fun setupLocale() {
        val res = context.resources
        val dm = res.displayMetrics
        val config = res.configuration

        Locale.setDefault(
            when (selectedLocale) {
                Language.ZH, Language.ZHT -> Locale.CHINESE
                Language.VI -> Locale("vi")
                Language.PHI ->Locale("phi")
                null -> Locale.getDefault()
                else -> Locale.US
            }
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(
                when (selectedLocale) {
                    Language.ZH, Language.ZHT -> Locale.CHINESE
                    Language.VI -> Locale("vi")
                    Language.PHI ->Locale("phi")
                    null -> Locale.getDefault()
                    else -> Locale.US
                }
            )
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            res.updateConfiguration(config, dm)
        }
    }

    private fun setListeners() {
        vpCalendar.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                setCalendarYearTitle(position)
                setNavigationHeader(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        imgVNavLeft.setOnClickListener {
            val newPosition = vpCalendar.currentItem - 1
            if (newPosition > -1) {
                vpCalendar.currentItem = newPosition
            }
        }
        imgVNavRight.setOnClickListener {
            val newPosition = vpCalendar.currentItem + 1
            if (newPosition < mDateRangeCalendarManager.getVisibleMonthDataList().size) {
                vpCalendar.currentItem = newPosition
            }
        }
    }

    /**
     * To set start date or end date now.
     *
     * @param dateSelectedType current modify date type
     */
    fun setDateSelectedType(dateSelectedType: DateSelectedType) {
        mDateRangeCalendarManager.setDateSelectedType(dateSelectedType)
    }

    /**
     * To set navigation header ( Left-Right button )
     *
     * @param position New page position
     */
    private fun setNavigationHeader(position: Int) {
        imgVNavRight.visibility = View.VISIBLE
        imgVNavLeft.visibility = View.VISIBLE
        if (mDateRangeCalendarManager.getVisibleMonthDataList().size == 1) {
            imgVNavLeft.visibility = View.INVISIBLE
            imgVNavRight.visibility = View.INVISIBLE
        } else if (position == 0) {
            imgVNavLeft.visibility = View.INVISIBLE
        } else if (position == mDateRangeCalendarManager.getVisibleMonthDataList().size - 1) {
            imgVNavRight.visibility = View.INVISIBLE
        }
    }

    /**
     * To set calendar year title
     *
     * @param position data list position for getting date
     */
    private fun setCalendarYearTitle(position: Int) {
        val currentCalendarMonth = mDateRangeCalendarManager.getVisibleMonthDataList()[position]
//        var dateText = DateFormatSymbols(locale).months[currentCalendarMonth[Calendar.MONTH]]
//        dateText = dateText.substring(0, 1).toUpperCase(Locale.getDefault()) + dateText.subSequence(1, dateText.length)
//        val yearTitle = dateText + " " + currentCalendarMonth[Calendar.YEAR]

        val month = currentCalendarMonth[Calendar.MONTH] + 1
        val yearTitle: String = when (selectedLocale) {
            Language.ZH, Language.ZHT -> {
                "${currentCalendarMonth[Calendar.YEAR]}${getLocalString(R.string.year)} ${getLocalString(getMonthText(month))}"
            }

            else -> {
                "${getLocalString(getMonthText(month))} ${currentCalendarMonth[Calendar.YEAR]}"
            }
        }
        tvYearTitle.text = yearTitle
        tvYearTitle.setTextColor(calendarStyleAttr.titleColor)
    }

    private fun getMonthText(month: Int) = when (month) {
        1 -> R.string.month_1
        2 -> R.string.month_2
        3 -> R.string.month_3
        4 -> R.string.month_4
        5 -> R.string.month_5
        6 -> R.string.month_6
        7 -> R.string.month_7
        8 -> R.string.month_8
        9 -> R.string.month_9
        10 -> R.string.month_10
        11 -> R.string.month_11
        else -> R.string.month_12
    }

    fun getLocalString(resId: Int): String {
        val localizedContext = getLocalizedContext(context)
        return localizedContext.resources.getString(resId)
    }

    fun getLocalizedContext(context: Context): Context {
        val locale = LanguageManager.getSetLanguageLocale(context)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(locale)
        return context.createConfigurationContext(conf)
    }

    /**
     * To set calendar listener
     *
     * @param calendarListener Listener
     */
    override fun setCalendarListener(calendarListener: CalendarListener) {
        adapterEventCalendarMonths.setCalendarListener(calendarListener)
    }

    /**
     * To apply custom fonts to all the text views
     *
     * @param fonts - Typeface that you want to apply
     */
    override fun setFonts(fonts: Typeface) {
        tvYearTitle.typeface = fonts
        calendarStyleAttr.fonts = fonts
        adapterEventCalendarMonths.invalidateCalendar()
    }

    /**
     * To remove all selection and redraw current calendar
     */
    override fun resetAllSelectedViews() {
        mDateRangeCalendarManager.resetSelectedDateRange()
        adapterEventCalendarMonths.resetAllSelectedViews()
    }

    /**
     * To set week offset. To start week from any of the day. Default is 0 (Sunday).
     *
     * @param offset 0-Sun, 1-Mon, 2-Tue, 3-Wed, 4-Thu, 5-Fri, 6-Sat
     */
    override fun setWeekOffset(offset: Int) {
        calendarStyleAttr.weekOffset = offset
        adapterEventCalendarMonths.invalidateCalendar()
    }

    /**
     * To set left navigation ImageView drawable
     */
    override fun setNavLeftImage(leftDrawable: Drawable) {
        imgVNavLeft.setImageDrawable(leftDrawable)
    }

    /**
     * To set right navigation ImageView drawable
     */
    override fun setNavRightImage(rightDrawable: Drawable) {
        imgVNavRight.setImageDrawable(rightDrawable)
    }

    /**
     * Sets start and end date.<br></br>
     * <B>Note:</B><br></br>
     * You can not set end date before start date.<br></br>
     * If you are setting custom month range than do not call this before calling (@method setVisibleMonthRange).<br></br>
     * If you have selected date selection mode as `single` or `fixed_range` then end date will be ignored.
     *
     * @param startDate Start date
     * @param endDate   End date
     */
    override fun setSelectedDateRange(startDate: Calendar, endDate: Calendar) {
        mDateRangeCalendarManager.setSelectedDateRange(startDate, endDate)
        adapterEventCalendarMonths.notifyDataSetChanged()
    }

    /**
     * To get start date.
     */
    override val startDate: Calendar?
        get() = mDateRangeCalendarManager.getMinSelectedDate()

    /**
     * To get end date.
     */
    override val endDate: Calendar?
        get() = mDateRangeCalendarManager.getMaxSelectedDate()

    /**
     * To get editable mode.
     */
    /**
     * To set editable mode. Default value will be true.
     *
     * @param isEditable true if you want user to select date range else false
     */
    override var isEditable: Boolean
        get() = adapterEventCalendarMonths.isEditable
        set(isEditable) {
            adapterEventCalendarMonths.isEditable = isEditable
        }

    /**
     * To provide month range to be shown to user. If start month is greater than end month than it will give [IllegalArgumentException].<br></br>
     * By default it will also make selectable date range as per visible month's dates. If you want to customize the selectable date range then
     * use [.setSelectableDateRange].<br></br><br></br>
     * **Note:** Do not call this method after calling date selection method [.setSelectableDateRange]
     * / [.setSelectedDateRange] as it will reset date selection.
     *
     * @param startMonth Start month of the calendar
     * @param endMonth   End month of the calendar
     */
    override fun setVisibleMonthRange(startMonth: Calendar, endMonth: Calendar) {
        mDateRangeCalendarManager.setVisibleMonths(startMonth, endMonth)
        adapterEventCalendarMonths.notifyDataSetChanged()
        vpCalendar.currentItem = 0
        setCalendarYearTitle(0)
        setNavigationHeader(0)
    }

    /**
     * To set current visible month.
     *
     * @param calendar Month to be set as current
     */
    override fun setCurrentMonth(calendar: Calendar) {
        vpCalendar.currentItem = mDateRangeCalendarManager.getMonthIndex(calendar)
    }

    override fun setSelectableDateRange(startDate: Calendar, endDate: Calendar) {
        mDateRangeCalendarManager.setSelectableDateRange(startDate, endDate)
        adapterEventCalendarMonths.notifyDataSetChanged()
    }

    /**
     * Sets number of days only when date selection mode is <B>free_range</B>. If date selection mode is not set to `free_range`
     * then exception will be thrown.<br></br>/
     * **Note:** Default number of days selection is 7 days from the selected date.
     *
     * @param numberOfDaysSelection - Number of days that needs to be selected from the selected date.
     */
    override fun setFixedDaysSelection(numberOfDaysSelection: Int) {
        calendarStyleAttr.fixedDaysSelectionNumber = numberOfDaysSelection
        adapterEventCalendarMonths.invalidateCalendar()
    }

    companion object {
        private const val TOTAL_ALLOWED_MONTHS = 30
    }
}