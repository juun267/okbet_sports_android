package org.cxct.sportlottery.net.money.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class FirstDepositDetail(
    /**
     * 限时首充奖励
     */
    val activityConfigDailyTimeLimit: FirstDepositConfig?=null,
    /**
     * 限时首充到期时间
     */
    val expireTime: Int=0,
    /**
     * 普通首充奖励
     */
    val isFirstDeposit: FirstDepositConfig?=null,
    /**
     * 限时首充次日奖励
     */
    val activityConfigAfterLimitDay: FirstDepositConfig?=null,
    /**
     * 普通首充次日奖励
     */
    val activityConfigAfterDay: FirstDepositConfig?=null,
    /**
     * 当前用户首充活动状态
     * 0-能参加限时首充
     * 1-能参加普通首充
     * 2-可领取普通隔日奖励
     * 3-可领取限时首充奖励
     */
    val userStatus: Int? ,
    /**
     *  有效投注流水
     */
    val validBetMoney: Double = 0.0
):Parcelable{
    fun getCurrentDepositConfig():FirstDepositConfig? =
      when(userStatus){
          0->  activityConfigDailyTimeLimit
          1->  isFirstDeposit
          2->  isFirstDeposit
          3->  activityConfigAfterLimitDay
          else-> null
      }
}