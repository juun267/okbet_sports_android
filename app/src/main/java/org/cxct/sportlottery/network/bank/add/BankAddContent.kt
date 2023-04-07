package org.cxct.sportlottery.network.bank.add


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class BankAddContent(
    @Json(name = "authorizeUrl")
    val authorizeUrl: String?,
) : Parcelable {
}