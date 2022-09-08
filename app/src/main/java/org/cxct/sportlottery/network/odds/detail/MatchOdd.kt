package org.cxct.sportlottery.network.odds.detail


import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.util.sortOddsMapByDetail

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "betPlayCateNameMap")
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "playCateTypeList")
    val playCateTypeList: List<PlayCateType>,
    @Json(name = "odds")
    var odds: MutableMap<String, CateDetailData>
){
    fun sortOddsMap() {
        this.odds.sortOddsMapByDetail()
    }
}