package org.cxct.sportlottery.net.chat.data


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
data class UnPacketRow(
    val id: Int,
    val roomId: Int,
    val currency: String,
    val rechMoney: Int,
    val betMoney: Int,
    val createBy: String,
    val createDate: Long,
    val status: Int,
    val packetType: Int,
    val platformId: Int,
):Parcelable