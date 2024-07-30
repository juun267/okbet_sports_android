package org.cxct.sportlottery.network.odds

import android.os.Parcelable
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.network.odds.list.OddStateParams
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.DiscountUtils.applyDiscount
import org.cxct.sportlottery.util.DiscountUtils.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import java.math.BigDecimal
import java.math.RoundingMode

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
    internal var originalOdds: String? = null, //赔率(load data)

    @Json(name = "marketSort")
    var marketSort: Int? = null, //marketSort

    @Json(name = "status")
    var status: Int? = null, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注

    @Json(name = "producerId")
    var producerId: Int? = null, //赔率生产者

    @Json(name = "nameMap")
    var nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map

    @Json(name = "extInfoMap")
    val extInfoMap: Map<String?, String?>? = null, //保存各语系extInfo对应值的map

    @Json(name = "extInfo")
    var extInfo: String? = null,

    @Json(name = "playCode")
    val playCode: String? = null,

    @Json(name = "rowSort")
    var rowSort: Int? = null,

    @Json(name = "version")
    var version: Long

) : OddStateParams, Parcelable, BaseNode() {

    var isOnlyEUType = true
        set(value) {
            if (field != value) {
                field = value

                //更新歐盤以外的賠率
                updateHkOdds()
                updateMalayOdds()
                updateIndoOdds()
            }
        }

    @Json(name = "oddsForShow")
    var odds: Double? = originalOdds?.toDoubleOrNull()

    @Json(name = "hkOddsForShow")
    var hkOdds: Double? = updateHkOdds() //香港

    @Json(name = "malayOddsForShow")
    var malayOdds: Double? = updateMalayOdds() //馬來盤

    @Json(name = "indoOddsForShow")
    var indoOdds: Double? = updateIndoOdds() //印尼盤

    var isSelected: Boolean = false

    var nextScore: String? = "" //FT 玩法下個進球會使用到

    var replaceScore: String? = "" //翻譯裡面要顯示{S}的分數會放在Key值的冒號後面

    override var oddState: Int = OddState.SAME.state

    @Transient
    override var runnable: Runnable? = null

    var outrightCateKey: String? = null

    var isExpand = false //投注項是否展開

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

    /**
     * 更新香港盤賠率並返回其值
     */
    private fun updateHkOdds(): Double? {
        val newHkOdds = if (isOnlyEUType) odds else (odds?.minus(1))

        hkOdds = newHkOdds
        return newHkOdds
    }

    /**
     * 更新馬來盤賠率並返回其值
     */
    private fun updateMalayOdds(): Double? {
        val newMalayOdds = if (isOnlyEUType) odds else (odds?.minus(1))?.convertToMYOdds()

        malayOdds = newMalayOdds
        return newMalayOdds
    }

    /**
     * 更新印尼盤賠率並返回其值
     */
    private fun updateIndoOdds(): Double? {
        val newIndoOdds = if (isOnlyEUType) odds else (odds?.minus(1))?.convertToIndoOdds()

        indoOdds = newIndoOdds
        return newIndoOdds
    }

    fun updateDiscount(discount: BigDecimal) {
        val oddsDiscount = originalOdds?.toBigDecimalOrNull()?.applyDiscount(discount)
            .toString().toDoubleOrNull()
        if (isOnlyEUType) {
            odds = oddsDiscount
            hkOdds = oddsDiscount
            malayOdds = oddsDiscount
            indoOdds = oddsDiscount
        } else {
            val hkOddsDiscount =
                originalOdds?.toBigDecimalOrNull()?.subtract(BigDecimal.ONE)?.applyHKDiscount(discount)
            val hkOddsHalfUp = hkOddsDiscount?.setScale(2, RoundingMode.HALF_UP)
                .toString().toDoubleOrNull()
            odds = oddsDiscount
            hkOdds = hkOddsDiscount.toString().toDoubleOrNull()
            malayOdds = hkOddsHalfUp?.convertToMYOdds()
            indoOdds = hkOddsHalfUp?.convertToIndoOdds()
        }

        //盤口為正常投注狀況時才去判斷是賠率值是否需要鎖盤
        if (status == BetStatus.ACTIVATED.code) {
            updateBetStatus()
        }
    }

    /**
     * 歐盤賠率小於1或香港盤賠率小於0時需將盤口鎖上
     *
     * @since 根據不同用戶經折扣率(Discount), 水位(margin)計算過後可能與原盤口狀態不同
     * @see org.cxct.sportlottery.network.bet.info.MatchOdd.updateBetStatus 若有調整此處一併調整
     */
    private fun updateBetStatus() {
        //歐盤賠率小於1或香港盤賠率小於0
        if (((this.odds?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO) <= BigDecimal.ONE) ||
            ((this.hkOdds?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO) <= BigDecimal.ZERO)
        ) {
            this.status = BetStatus.LOCKED.code
        }
    }

}
