package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.component_date_range_new_selector.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.btn_search
import kotlinx.android.synthetic.main.component_date_range_selector.view.ll_end_date
import kotlinx.android.synthetic.main.component_date_range_selector.view.tv_end_date
import kotlinx.android.synthetic.main.component_date_range_selector.view.tv_start_date
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import java.util.*

class DateRangeSearchNewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    var dateRange = -30
    private var minusDays = 6
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.CalendarBottomSheetStyle_calendarLayout, R.layout.dialog_bottom_sheet_calendar) }
    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val calendarBottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }
    var timeZone: TimeZone = TimeZone.getDefault()

    val startTime: Long?
        get() = TimeUtil.dateToTimeStamp(
            tv_start_date.text.toString(),
            TimeUtil.TimeType.START_OF_DAY,
            timeZone = timeZone
        )

    val endTime: Long?
        get() = TimeUtil.dateToTimeStamp(
            tv_end_date.text.toString(),
            TimeUtil.TimeType.END_OF_DAY,
            timeZone = timeZone
        )

    val startDate: String
        get() = tv_start_date.text.toString()

    val endDate: String
        get() = tv_end_date.text.toString()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.component_date_range_new_selector, this, false)
        addView(view)
        dateRange = typedArray.getInteger(R.styleable.CalendarBottomSheetStyle_dateRange, -30)
        minusDays = typedArray.getInteger(R.styleable.CalendarBottomSheetStyle_minusDays, 6)

        try {
            initDate(minusDays)
            setupCalendarBottomSheet()
            initOnclick()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }

    private fun initDate(minusDays: Int) {
        tv_start_date.text = TimeUtil.getDefaultDate().startTime
        tv_end_date.text = TimeUtil.getDefaultDate().endTime
    }

    fun setOnClickSearchListener (search: () -> Unit) {
        btn_search.setOnClickListener {
            search.invoke()
        }
    }

    private fun initOnclick() {
        ll_start_date_box.setOnClickListener {
            bottomSheetView.calendar.setDateSelectedType(DateSelectedType.START)
            bottomSheetView.tv_calendar_title.setText(R.string.start_date)
            calendarBottomSheet.show()
        }

        ll_end_date_box.setOnClickListener {
            bottomSheetView.calendar.setDateSelectedType(DateSelectedType.END)
            bottomSheetView.tv_calendar_title.setText(R.string.end_date)
            calendarBottomSheet.show()
        }
    }

    fun setRecordTimeRange(dateSelectedType: DateSelectedType? = null, start: Calendar, end: Calendar? = null) {

        if (end != null) {
            setRecordStartTime(start)
            setRecordEndTime(end)
        } else {
            when (dateSelectedType) {
                DateSelectedType.START -> {
                    setRecordStartTime(start)
                }
                DateSelectedType.END -> {
                    setRecordEndTime(start)
                }
            }
        }
    }

    private fun setRecordStartTime(start: Calendar) {
        tv_start_date.text = TimeUtil.timeFormat(start.timeInMillis, YMD_FORMAT)
    }

    private fun setRecordEndTime(end: Calendar) {
        tv_end_date.text = TimeUtil.timeFormat(end.timeInMillis, YMD_FORMAT)
    }

    private fun setupCalendarBottomSheet() {
        setCalendarRange()
        calendarBottomSheet.setContentView(bottomSheetView)
        bottomSheetView.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(dateSelectedType: DateSelectedType, startDate: Calendar) {
                setRecordTimeRange(dateSelectedType, startDate)
                calendarBottomSheet.dismiss()
            }

            override fun onDateRangeSelected(dateSelectedType: DateSelectedType, startDate: Calendar, endDate: Calendar) {
                setRecordTimeRange(null, startDate, endDate)
                calendarBottomSheet.dismiss()
            }
        })
    }

    private fun setCalendarRange() {
        val calendarToday = TimeUtil.getTodayEndTimeCalendar()
        val calendarPastMonth = TimeUtil.getTodayEndTimeCalendar()
        calendarPastMonth.add(Calendar.DATE, dateRange)
        bottomSheetView.calendar.setSelectableDateRange(calendarPastMonth, calendarToday)
        bottomSheetView.calendar.setSelectedDateRange(TimeUtil.getCalendarForDates(minusDays).first, TimeUtil.getCalendarForDates(minusDays).second)
    }

}