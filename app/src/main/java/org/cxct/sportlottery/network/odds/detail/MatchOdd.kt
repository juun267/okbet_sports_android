package org.cxct.sportlottery.network.odds.detail


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.repository.GamePlayNameRepository
import org.cxct.sportlottery.util.sortOddsMapByDetail

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class MatchOdd(
    @Json(name = "betPlayCateNameMap")
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo,
    @Json(name = "playCateTypeList")
    val playCateTypeList: List<PlayCateType>,
    @Json(name = "odds")
    var odds: MutableMap<String, CateDetailData>,
) : Parcelable, org.cxct.sportlottery.network.common.MatchOdd {
    fun sortOddsMap() {
        this.odds.sortOddsMapByDetail()
    }

    fun setupIsOnlyEUType() {
        this.odds.forEach { (playCateCode, cateDetailData) ->
            val supportOddsTypeSwitch = GamePlayNameRepository.getPlayCateSupportOddsTypeSwitch(
                matchInfo.gameType,
                playCateCode
            ) ?: false
            cateDetailData.odds.forEach { odd ->
                odd?.isOnlyEUType = !supportOddsTypeSwitch
            }
        }
    }

    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = null
    override val oddsSort: String? = null
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null
    override val oddsEps: EpsOdd? = null
}