package org.cxct.sportlottery.network.withdraw.uwcheck

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
class UwCheckData(
    @Json(name = "needCheck")
    val needCheck: Boolean?,
    @Json(name = "checkList")
    val checkList: List<CheckList>?,
    @Json(name = "total")
    val total: TotalData?
):Parcelable