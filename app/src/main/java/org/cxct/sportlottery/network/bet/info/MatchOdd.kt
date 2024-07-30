package org.cxct.sportlottery.network.bet.info

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.SpreadState
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.util.DiscountUtils.applyDiscount
import org.cxct.sportlottery.util.DiscountUtils.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import java.math.BigDecimal
import java.math.RoundingMode

@Parcelize
@KeepMembers
data class MatchOdd(
    val awayName: String?,
    val homeName: String?,
    val inplay: Int,
    val leagueId: String,
    val leagueName: String?,
    val matchId: String,
    val originalOdds: String?,
    var odds: Double,
    var hkOdds: Double,
    var malayOdds: Double,
    var indoOdds: Double,
    var oddsId: String,
    val playCateId: Int,
    val playCateName: String,
    val playCode: String,
    val playId: Int,
    val playName: String,
    val producerId: Int,
    var spread: String,
    val startTime: Long?,
    var status: Int?,
    var gameType: String,
    var homeScore: Int,
    var awayScore: Int,
    var version: Long,//用于比较新旧赔率
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    override val matchInfo: org.cxct.sportlottery.network.odds.MatchInfo?,
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = null,
    override val oddsSort: String? = null,
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null,
    override val oddsEps: EpsOdd? = null,
) : Parcelable, org.cxct.sportlottery.network.common.MatchOdd {
    var oddState: Int = OddState.SAME.state

    @Transient
    var runnable: Runnable? = null //賠率變更，按鈕顏色變換任務
    var oddsHasChanged = false
    var spreadState: Int = SpreadState.SAME
    var extInfo: String? = null //球員名稱
    var isOnlyEUType: Boolean = false
    var homeCornerKicks: Int? = null
    var awayCornerKicks: Int? = null
    var categoryCode: String? = null
    var nextScore: String? = "" //FT 玩法下個進球會使用到
    var replaceScore: String? = "" //翻譯裡面要顯示{S}的分數會放在Key值的冒號後面

    /**
     * 折扣率更新
     */
    fun updateDiscount(discount: BigDecimal) {
        if (playCode != PlayCate.LCS.value) {
            val oddsDiscountNullable = this.originalOdds?.toBigDecimalOrNull()?.applyDiscount(discount)
            oddsDiscountNullable?.let { oddsDiscount ->
                if (isOnlyEUType) {
                    this.odds = oddsDiscount.toDouble()
                    this.hkOdds = oddsDiscount.toDouble()
                    this.malayOdds = oddsDiscount.toDouble()
                    this.indoOdds = oddsDiscount.toDouble()
                } else {
                    val hkOddsDiscountNullable =
                        originalOdds?.toBigDecimalOrNull()?.subtract(BigDecimal.ONE)?.applyHKDiscount(discount)
                    odds = oddsDiscount.toDouble()
                    hkOddsDiscountNullable?.let { hkOddsDiscount ->
                        val hkOddsHalfUp = hkOddsDiscount.setScale(2, RoundingMode.HALF_UP).toDouble()
                        hkOdds = hkOddsDiscount.toDouble()
                        malayOdds = hkOddsHalfUp.convertToMYOdds()
                        indoOdds = hkOddsHalfUp.convertToIndoOdds()
                    }
                }
                //盤口為正常投注狀況時才去判斷是賠率值是否需要鎖盤
                if (status == BetStatus.ACTIVATED.code) {
                    updateBetStatus()
                }
            }
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

