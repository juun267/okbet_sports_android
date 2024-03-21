package org.cxct.sportlottery.network.bet.list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class EndingCardOFLWinnable(
    val cardMoney: Int?,
    val lastDigit1Result: Boolean?,
    val lastDigit1Score: String?,
    val lastDigit1Winnable: Int?,
    val lastDigit2Result: Boolean?,
    val lastDigit2Score: String?,
    val lastDigit2Winnable: Int?,
    val lastDigit3Result: Boolean?,
    val lastDigit3Score: String?,
    val lastDigit3Winnable: Int?,
    val lastDigit4Result: Boolean?,
    val lastDigit4Score: String?,
    val lastDigit4Winnable: Int?,
    val lastDigitHFResult: Boolean?,
    val lastDigitHFScore: String?,
    val lastDigitHFWinnable: Int?
):Parcelable