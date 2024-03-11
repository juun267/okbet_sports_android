package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ComponentDateRangeNewSelector2Binding
import org.cxct.sportlottery.databinding.DialogBottomSheetCalendarBinding
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT_2
import splitties.systemservices.layoutInflater
import java.util.*

class DateRangeSearchView2  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {


    var dateRange = -30
    private var minusDays = 30
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetViewBinding by lazy { DialogBottomSheetCalendarBinding.inflate(layoutInflater,this,false) }
    private val selectorBinding by lazy { ComponentDateRangeNewSelector2Binding.inflate(layoutInflater,this,false) }
    private val calendarPopup: PopupWindow by lazy { PopupWindow(context) }
    var timeZone: TimeZone = TimeZone.getDefault()

    val startTime: Long?
        get() = TimeUtil.dateToTimeStamp2(
            selectorBinding.tvStartDate.text.toString(),
            TimeUtil.TimeType.START_OF_DAY,
            timeZone = timeZone
        )


    val endTime: Long?
        get() = TimeUtil.dateToTimeStamp2(
            selectorBinding.tvEndDate.text.toString(),
            TimeUtil.TimeType.END_OF_DAY,
            timeZone = timeZone
        )

    val startDate: String
        get() = selectorBinding.tvStartDate.text.toString()

    val endDate: String
        get() = selectorBinding.tvEndDate.text.toString()

    init {
        addView(selectorBinding.root)
        dateRange = typedArray.getInteger(R.styleable.CalendarBottomSheetStyle_dateRange, -30)
        minusDays = typedArray.getInteger(R.styleable.CalendarBottomSheetStyle_minusDays, minusDays)

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
        selectorBinding.tvStartDate.text = TimeUtil.getDefaultDate2(minusDays).startTime
        selectorBinding.tvEndDate.text = TimeUtil.getDefaultDate2().endTime
    }

    fun setOnClickSearchListener (search: () -> Unit) {
        selectorBinding.btnSearch.setOnClickListener {
            search.invoke()
        }
    }

    private fun initOnclick() {
        selectorBinding.llStartDateBox.setOnClickListener {
            bottomSheetViewBinding.calendar.setDateSelectedType(DateSelectedType.START)
            bottomSheetViewBinding.tvCalendarTitle.setText(R.string.start_date)
            selectorBinding.llStartDateBox.isSelected = !selectorBinding.llStartDateBox.isSelected
            selectorBinding.llEndDateBox.isSelected = false
            if (selectorBinding.llStartDateBox.isSelected) {
                calendarPopup.showAsDropDown(it)
            } else {
                calendarPopup.dismiss()
            }
        }

        selectorBinding.llEndDateBox.setOnClickListener {
            bottomSheetViewBinding.calendar.setDateSelectedType(DateSelectedType.END)
            bottomSheetViewBinding.tvCalendarTitle.setText(R.string.end_date)
            selectorBinding.llStartDateBox.isSelected = false
            selectorBinding.llEndDateBox.isSelected = !selectorBinding.llEndDateBox.isSelected
            if (selectorBinding.llEndDateBox.isSelected) {
                calendarPopup.showAsDropDown(it)
            } else {
                calendarPopup.dismiss()
            }
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
        selectorBinding.tvStartDate.text = TimeUtil.timeFormat(start.timeInMillis, YMD_FORMAT_2)
    }

    private fun setRecordEndTime(end: Calendar) {
        selectorBinding.tvEndDate.text = TimeUtil.timeFormat(end.timeInMillis, YMD_FORMAT_2)
    }


    private fun setupCalendarBottomSheet() {
        setCalendarRange()
        calendarPopup.setContentView(bottomSheetViewBinding.root)
        calendarPopup.width = ViewGroup.LayoutParams.MATCH_PARENT
        calendarPopup.height = ViewGroup.LayoutParams.WRAP_CONTENT
        calendarPopup.setBackgroundDrawable(ColorDrawable())
        calendarPopup.isOutsideTouchable = true
        calendarPopup.setOnDismissListener {
            selectorBinding.llStartDateBox.isSelected = false
            selectorBinding.llEndDateBox.isSelected = false
        }
        bottomSheetViewBinding.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar,
            ) {
                setRecordTimeRange(dateSelectedType, startDate)
                calendarPopup.dismiss()
            }

            override fun onDateRangeSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar,
                endDate: Calendar,
            ) {
                setRecordTimeRange(null, startDate, endDate)
                calendarPopup.dismiss()
            }
        })
    }

    private fun setCalendarRange() {
        val calendarToday = TimeUtil.getTodayEndTimeCalendar()
        val calendarPastMonth = TimeUtil.getTodayEndTimeCalendar()
        calendarPastMonth.add(Calendar.DATE, dateRange)
        bottomSheetViewBinding.calendar.setSelectableDateRange(calendarPastMonth, calendarToday)
        bottomSheetViewBinding.calendar.setSelectedDateRange(TimeUtil.getCalendarForDates(minusDays).first, TimeUtil.getCalendarForDates(minusDays).second)
    }

}