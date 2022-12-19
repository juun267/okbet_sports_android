package org.cxct.sportlottery.network.outright.odds

import android.os.Parcelable
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = mutableMapOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: @RawValue Map<String, DynamicMarket>,
    @Json(name = "oddsList")
    val oddsList: @RawValue List<String?>? = listOf(), //TODO Cheryl : 目前回傳都是null, 待測試
    @Json(name = "quickPlayCateList")
    override var quickPlayCateList: @RawValue MutableList<QuickPlayCate>? = mutableListOf(), //(新)赛事可玩的快捷玩法列表
    @Json(name = "oddsSort")
    override val oddsSort: String? = null,
    @Json(name = "betPlayCateNameMap")
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = mutableMapOf(),
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = mutableMapOf(),

    ) : MatchOdd, BaseExpandNode(),


    Parcelable {
    @IgnoredOnParcel
    override val oddsEps: EpsOdd? = null

    @IgnoredOnParcel
    override var playCateMappingList: List<PlayCateMapItem>? = null

    @IgnoredOnParcel
    var startDate: String = ""

    @IgnoredOnParcel
    var startTime: String = ""

    @IgnoredOnParcel
    //預設為第一項玩法展開
    var oddsExpand: MutableMap<String, Boolean>? = oddsMap?.mapValues {
        it.key == oddsMap?.keys?.firstOrNull()
    }?.toMutableMap()

    //僅給冠軍重組賠率項資料結構使用
    var outrightOddsList: MutableList<Any> = mutableListOf()

    override val childNode: MutableList<BaseNode>
        get() = _nodes

    @IgnoredOnParcel
    private val _nodes by lazy {
        val oddsNode = mutableListOf<BaseNode>()
        oddsMap?.entries?.toMutableList()?.forEach {
            val categoryOdds = CategoryOdds(dynamicMarkets?.get(it.key)?.get() ?: "", this, it.key,it.value?: mutableListOf())
            oddsNode.add(categoryOdds)
            categoryOddsMap[it.key] = categoryOdds
        }
        oddsNode
    }

    fun plusPlayCateOdds(playCate: String, oddList: MutableList<Odd>) {
        childNode?.add(CategoryOdds(dynamicMarkets?.get(playCate)?.get() ?: "", this, playCate,oddList))
    }

    @IgnoredOnParcel
    var oddIdsMap: MutableMap<String, MutableMap<String, Odd>> = mutableMapOf()  //用于本地计算

    @IgnoredOnParcel
    var categoryOddsMap = mutableMapOf<String, CategoryOdds>()    //用于本地计算

}