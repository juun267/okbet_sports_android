package org.cxct.sportlottery.network.odds.detail


import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo

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
        this.odds.forEach { (_, value) ->
            if (value?.odds.size!! > 3 && value.odds.first()?.marketSort != 0 && (value.odds.first()?.odds != value.odds.first()?.malayOdds)) {
                value?.odds.sortBy {
                    it?.marketSort
                }
            }
        }
    }
}