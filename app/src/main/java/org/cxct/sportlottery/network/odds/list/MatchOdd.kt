package org.cxct.sportlottery.network.odds.list


import android.os.Parcelable
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.util.sortOddsMap

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "betPlayCateNameMap")
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    ),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>? = null,
    @Json(name = "quickPlayCateList")
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null,
    @Json(name = "oddsSort")
    override var oddsSort: String? = null,
    var bkEndCarkOFLCount: Int = 0, //2024-03-18新增，用户在该场比赛上的投注数量
) : MatchOdd, BaseExpandNode(), Parcelable {

    override val childNode: MutableList<BaseNode> = mutableListOf()

    override val oddsEps: EpsOdd? = null
    var isSelected = true

    fun sortOddsMap() {
        this.oddsMap?.sortOddsMap()
    }

    var csTabSelected: PlayCate = PlayCate.CS

    @Transient
    @IgnoredOnParcel
    var oddIdsMap: MutableMap<String, MutableMap<String, Odd>> = mutableMapOf()  //用于本地计算

    // 列表的父节点
    @Transient
    lateinit var parentNode: BaseNode
    //篮球末位比分，记录当前选中的末位比分玩法类型
    @Transient
    @IgnoredOnParcel
    var selectPlayCode: String = PlayCate.FS_LD_CS.value
    @Transient
    @IgnoredOnParcel
    var selectPlayOdds: MutableMap<String, Odd>? = null
}

object TimeCounting {
    val STOP = 1
}