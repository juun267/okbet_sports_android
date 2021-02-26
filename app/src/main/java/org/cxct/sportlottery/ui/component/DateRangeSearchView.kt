package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.content_rv_bank_list_new.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.finance.data.RechargeTime
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class DateRangeSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.CalendarBottomSheetStyle_calendarLayout, R.layout.dialog_bottom_sheet_calendar) }
    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val calendarBottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.component_date_range_selector, this, false)
        addView(view)

        try {
            setupCalendarBottomSheet()
            initOnclick()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }


    fun getStartAndEndDate(): Pair<Long?, Long?> {
        val startTimeStamp = TimeUtil.dateToTimeStamp(tv_start_date.text.toString(), TimeUtil.TimeType.START_OF_DAY)
        val endTimeStamp = TimeUtil.dateToTimeStamp(tv_end_date.text.toString(), TimeUtil.TimeType.START_OF_DAY)
        return startTimeStamp to endTimeStamp
    }

    fun setOnClickSearchListener (search: () -> Unit) {
        btn_search.setOnClickListener {
            search.invoke()
        }
    }

    private fun initOnclick() {
        ll_start_date.setOnClickListener {
            bottomSheetView.calendar.setDateSelectedType(DateSelectedType.START)
            calendarBottomSheet.show()
        }

        ll_end_date.setOnClickListener {
            bottomSheetView.calendar.setDateSelectedType(DateSelectedType.END)
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
        val startDateStr = TimeUtil.timeFormat(start.timeInMillis, "yyyy-MM-dd")
        val startDate = RechargeTime(startDateStr, TimeUtil.getDayDateTimeRangeParams(startDateStr))
        tv_start_date.text = startDate.date
    }

    private fun setRecordEndTime(end: Calendar) {
        val endDateStr = TimeUtil.timeFormat(end.timeInMillis, "yyyy-MM-dd")
        val endDate = RechargeTime(endDateStr, TimeUtil.getDayDateTimeRangeParams(endDateStr))
        tv_end_date.text = endDate.date
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
        calendarPastMonth.add(Calendar.DATE, -30)
        bottomSheetView.calendar.setSelectableDateRange(calendarPastMonth, calendarToday)
    }

}