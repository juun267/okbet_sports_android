package org.cxct.sportlottery.ui.sport.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class SelectDate(
    val date: Date,
    val name: String,
    val label: String,
    val startTime: String,
    val endTime: String
):Parcelable