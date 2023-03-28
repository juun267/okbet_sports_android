package org.cxct.sportlottery.network.odds

import android.os.Parcelable
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.network.odds.list.OddStateParams
import org.cxct.sportlottery.common.proguard.KeepMembers

/**
 * @author Kevin
 * @create 2021/7/23
 * @description 統一Odd格式
 */
@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class Odd(

    @Json(name = "id")
    val id: String? = "", //赔率id

    @Json(name = "name")
    val name: String? = null, //玩法名称（如果是球员玩法，则名称代码球员名称）

    @Json(name = "spread")
    var spread: String? = null, //让分或大小分值 (如果是球员玩法，则表示球员ID)

    @Json(name = "odds")
    var odds: Double? = null, //赔率

    @Json(name = "hkOdds")
    var hkOdds: Double? = null, //香港

    @Json(name = "malayOdds")
    var malayOdds: Double? = null, //馬來盤

    @Json(name = "indoOdds")
    var indoOdds: Double? = null, //印尼盤

    @Json(name = "marketSort")
    var marketSort: Int? = null, //marketSort

    @Json(name = "status")
    var status: Int? = null, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注

    @Json(name = "producerId")
    var producerId: Int? = null, //赔率生产者

    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map

    @Json(name = "extInfoMap")
    val extInfoMap: Map<String?, String?>? = null, //保存各语系extInfo对应值的map

    @Json(name = "extInfo")
    var extInfo: String? = null,

    @Json(name = "playCode")
    val playCode: String? = null,

    @Json(name = "rowSort")
    val rowSort: Int? = null

) : OddStateParams, Parcelable, BaseNode() {

    var isSelected: Boolean? = false

    var nextScore: String? = "" //FT 玩法下個進球會使用到

    var replaceScore: String? = "" //翻譯裡面要顯示{S}的分數會放在Key值的冒號後面

    override var oddState: Int = OddState.SAME.state

    @Transient
    override var runnable: Runnable? = null

    var outrightCateKey: String? = null

    var isExpand = false //投注項是否展開

    //odds有機會一開始推null回來
    var isOnlyEUType = odds == hkOdds && odds == malayOdds && odds == indoOdds && odds != null && hkOdds != null && malayOdds != null && indoOdds!= null

    @Transient
    override val childNode: MutableList<BaseNode>? = null

    // 列表的父节点
    @Transient
    lateinit var parentNode: BaseNode

    fun updateOdd(newOdd: Double) {

        val odd = odds
        odds = newOdd

        if (odd == null || odd == newOdd) {
            oddState = OddState.SAME.state
            return
        }

        if (odd > newOdd) {
            oddState = OddState.SMALLER.state
            return
        }

        oddState = OddState.LARGER.state
    }

}