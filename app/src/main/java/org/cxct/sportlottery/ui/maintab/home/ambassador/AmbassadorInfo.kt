package org.cxct.sportlottery.ui.maintab.home.ambassador

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.R

object AmbassadorInfo {

@Parcelize
data class Ambassador(
    @DrawableRes val topBanner: Int,
    @DrawableRes val infoPic: Int,
    val videoUrl: String,
    @StringRes val infoDetail: Int,
    @StringRes val summary: Int,
    val bottomBanner: List<Int>,
): Parcelable

val ambassadorList by lazy { mapOf(
    "Zebby" to Ambassador(
        topBanner = R.drawable.ambassador_banner_zeinab,
        infoPic = R.drawable.ambassador_info_zeinab,
        videoUrl = "https://upload.sportsadm.com/video/ZEINAB-30S-DDAY-WIDE-4-with-chargen-060724.mp4",
        infoDetail = R.string.P473,
        summary = R.string.P4731,
        bottomBanner = listOf(
            R.drawable.ambassador_banner_zeinab_detail_1,
            R.drawable.ambassador_banner_zeinab_detail_2,
            R.drawable.ambassador_banner_zeinab_detail_3,
            R.drawable.ambassador_banner_zeinab_detail_4,
            R.drawable.ambassador_banner_zeinab_detail_5,
            R.drawable.ambassador_banner_zeinab_detail_6,
            R.drawable.ambassador_banner_zeinab_detail_7,)
    ),
    "Matteo" to Ambassador(
        topBanner = R.drawable.ambassador_banner_matteo,
        infoPic = R.drawable.ambassador_info_matteo,
        videoUrl = "https://upload.sportsadm.com/video/Matteo-x-OKBet.mp4",
        infoDetail = R.string.P486,
        summary = R.string.P4861,
        bottomBanner = listOf(
            R.drawable.ambassador_banner_matteo_detail_1,
            R.drawable.ambassador_banner_matteo_detail_2,
            R.drawable.ambassador_banner_matteo_detail_3,
            R.drawable.ambassador_banner_matteo_detail_4,
            R.drawable.ambassador_banner_matteo_detail_5,
            R.drawable.ambassador_banner_matteo_detail_6,)
    )
)
}
}


