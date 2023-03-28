package org.cxct.sportlottery.network.outright.odds

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class DynamicMarket(
    @Json(name = "en")
    val en: String?,
    @Json(name = "zh")
    val zh: String?,
    @Json(name = "vi")
    val vi: String?,
    @Json(name = "th")
    val th: String?,
    @Json(name = "ph")
    val ph:String?
) : Parcelable

fun DynamicMarket.get(): String {
    return when (LanguageManager.getSelectLanguage(LocalUtils.getLocalizedContext())) {
        LanguageManager.Language.ZH -> zh
        LanguageManager.Language.VI -> vi
        LanguageManager.Language.TH -> th
        LanguageManager.Language.PHI -> ph
        else -> en
    } ?: ""
}
