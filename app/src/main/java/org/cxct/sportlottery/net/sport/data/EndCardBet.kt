package org.cxct.sportlottery.net.sport.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class EndCardBet(
    val id: Int,
    val matchId: String,
    val betMoney: Int,
    val cardNum: Int,
    val lastDigit1: Int,
    val lastDigit2: Int,
    val lastDigit3: Int,
    val lastDigit4: Int,
    val extra: Int,
    val scoreHf: Int,
    val betMyself: List<String>?,
    val lastBetName: Map<String, String>?):Parcelable
