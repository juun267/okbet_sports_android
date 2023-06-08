package org.cxct.sportlottery.util

import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.OddsUtil.updateBetStatus

object OddsUtil {

    fun MutableList<Odd?>.updateBetStatus() {
        //整个玩法封盘
        forEach { odd ->
            if (odd?.updateBetStatus() == true) {
                forEach { it?.status = BetStatus.LOCKED.code }
                return
            }
        }
    }

    fun MutableList<Odd>.updateBetStatus_1() {
        //整个玩法封盘
        forEach { odd ->
            if (odd?.updateBetStatus()) {
                forEach { it?.status = BetStatus.LOCKED.code }
                return
            }
        }
    }

    /**
     * 歐盤賠率小於1或香港盤賠率小於0時需將盤口鎖上
     *
     * @since 根據不同用戶經折扣率(Discount), 水位(margin)計算過後可能與原盤口狀態不同
     * @see MatchOddUtil.updateBetStatus 若有調整此處一併調整
     */
    fun Odd?.updateBetStatus(): Boolean {
        //歐盤賠率小於1或香港盤賠率小於0
        if (((this?.odds ?: 0.0) <= 1.0) || ((this?.hkOdds ?: 0.0) <= 0)) {
            this?.status = BetStatus.LOCKED.code
            return true
        }
        return false
    }
}