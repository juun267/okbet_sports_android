package org.cxct.sportlottery.network.credential

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class OcrResult (
    @Json(name = "ID_NUMBER")
    val idNumber: String?,

    @Json(name = "FIRST_NAME")
    val firstName: String?,

    @Json(name = "MIDDLE_NAME")
    val middleName: String?,

    @Json(name = "LAST_NAME")
    val lastName: String?,

    @Json(name = "SEX")
    val sex: String?,

    @Json(name = "DATE_OF_BIRTH")
    val dateOfBirth: String?,

    @Json(name = "EXPIRY_DATE")
    val expireDate: String?,

    @Json(name = "ADDRESS")
    val address: String?,
): Parcelable
