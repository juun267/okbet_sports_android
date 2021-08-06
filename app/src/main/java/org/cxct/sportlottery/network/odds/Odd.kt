package org.cxct.sportlottery.network.odds

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.odds.list.OddStateParams

/**
 * @author Kevin
 * @create 2021/7/23
 * @description 統一Odd格式
 */
@JsonClass(generateAdapter = true)
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

    @Json(name = "status")
    var status: Int = BetStatus.ACTIVATED.code, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注

    @Json(name = "producerId")
    var producerId: Int? = null, //赔率生产者

    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map

    @Json(name = "extInfoMap")
    val extInfoMap: Map<String?, String?>? = null, //保存各语系extInfo对应值的map

    @Json(name = "extInfo")
    val extInfo: String? = null


) : OddStateParams {

    var isSelected: Boolean? = false

    override var oddState: Int = OddState.SAME.state

    @Transient
    override var runnable: Runnable? = null

    var itemViewVisible = true

    var outrightCateKey: String? = null

}