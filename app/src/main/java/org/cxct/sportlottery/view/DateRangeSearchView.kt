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
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.databinding.ComponentDateRangeNewSelectorBinding
import org.cxct.sportlottery.databinding.DialogBottomSheetCalendarBinding
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT_2
import splitties.systemservices.layoutInflater
import java.util.*

class DateRangeSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    var dateRange = -30
    var minusDays = 6
    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetViewBinding by lazy { DialogBottomSheetCalendarBinding.inflate(layoutInflater) }
    private val binding by lazy { ComponentDateRangeNewSelectorBinding.inflate(layoutInflater,this,false) }
    private val calendarPopup: PopupWindow by lazy { PopupWindow(context) }
    var timeZone: TimeZone = TimeZone.getDefault()

    val startTime: Long?
        get() = TimeUtil.dateToTimeStamp2(
            binding.tvStartDate.text.toString(),
            TimeUtil.TimeType.START_OF_DAY,
            timeZone = timeZone
        )


    val endTime: Long?
        get() = TimeUtil.dateToTimeStamp2(
            binding.tvEndDate.text.toString(),
            TimeUtil.TimeType.END_OF_DAY,
            timeZone = timeZone
        )

    val startDate: String
        get() = binding.tvStartDate.text.toString()

    val endDate: String
        get() = binding.tvEndDate.text.toString()



    init {
        addView(binding.root)
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
        binding.tvStartDate.text = TimeUtil.getDefaultDate2().startTime
        binding.tvEndDate.text = TimeUtil.getDefaultDate2().endTime
    }

    fun setOnClickSearchListener (search: () -> Unit) {
        binding.btnSearch.clickDelay {
            search.invoke()
        }
    }

    private fun initOnclick() {
        binding.llStartDateBox.setOnClickListener {
            bottomSheetViewBinding.calendar.setDateSelectedType(DateSelectedType.START)
            bottomSheetViewBinding.tvCalendarTitle.setText(R.string.start_date)
            binding.llStartDateBox.isSelected = !binding.llStartDateBox.isSelected
            binding.llEndDateBox.isSelected = false
            if (binding.llStartDateBox.isSelected) {
                calendarPopup.showAsDropDown(it)
            } else {
                calendarPopup.dismiss()
            }
        }

        binding.llEndDateBox.setOnClickListener {
            bottomSheetViewBinding.calendar.setDateSelectedType(DateSelectedType.END)
            bottomSheetViewBinding.tvCalendarTitle.setText(R.string.end_date)
            binding.llStartDateBox.isSelected = false
            binding.llEndDateBox.isSelected = !binding.llEndDateBox.isSelected
            if (binding.llEndDateBox.isSelected) {
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
        binding.tvStartDate.text = TimeUtil.timeFormat(start.timeInMillis, YMD_FORMAT_2)
    }

    private fun setRecordEndTime(end: Calendar) {
        binding.tvEndDate.text = TimeUtil.timeFormat(end.timeInMillis, YMD_FORMAT_2)
    }


    private fun setupCalendarBottomSheet() {
        setCalendarRange()
        calendarPopup.contentView = bottomSheetViewBinding.root
        calendarPopup.width = ViewGroup.LayoutParams.MATCH_PARENT
        calendarPopup.height = ViewGroup.LayoutParams.WRAP_CONTENT
        calendarPopup.setBackgroundDrawable(ColorDrawable())
        calendarPopup.isOutsideTouchable = true
        calendarPopup.setOnDismissListener {
            binding.llStartDateBox.isSelected = false
            binding.llEndDateBox.isSelected = false
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