package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class OCRInfo(val birthday: String?,
                   val firstName: String?,
                   val middleName: String?,
                   val lastName: String?,
                   val imgUrl: String?,
                   val identityNumber: String?): Parcelable