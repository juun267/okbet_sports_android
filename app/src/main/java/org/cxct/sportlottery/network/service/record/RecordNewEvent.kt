package org.cxct.sportlottery.network.service.record

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.ServiceEventType

@Parcelize
@KeepMembers
class RecordNewEvent(
    override val eventType: String,
    val player: String = "",
    val games: String = "",
    val betAmount: String,
    var profitAmount: String,
    val h5ImgGame: String?= null,
    val betTime: Long? = null,
    val firmType: String = "",
    val gameEntryType: String?,
    val gameCode: String? = null // 如果gameCode为空则是体育投注
    ) : Parcelable,ServiceEventType {

        fun isSportBet() = gameCode.isEmptyStr()
        var isWS = false

    }