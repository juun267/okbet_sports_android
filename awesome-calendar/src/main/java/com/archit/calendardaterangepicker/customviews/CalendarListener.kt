package com.archit.calendardaterangepicker.customviews

import java.util.Calendar

interface CalendarListener {
    /**
     * Called on first date selection.
     * @param startDate First selected date.
     */
    fun onFirstDateSelected(dateSelectedType: DateSelectedType, startDate: Calendar)

    /**
     * Called on first and last date selection.
     * @param startDate First date.
     * @param endDate Last date.
     */
    fun onDateRangeSelected(dateSelectedType: DateSelectedType, startDate: Calendar?, endDate: Calendar)
}