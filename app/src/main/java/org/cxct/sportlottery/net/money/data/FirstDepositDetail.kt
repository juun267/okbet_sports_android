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
    val expireTime: Long=0,
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
     * 4-普通首充等待领取
     * 5-限时首充等待领取
     */
    val userStatus: Int? ,
    /**
     *  有效投注流水
     */
    val validBetMoney: Double = 0.0,
    /**
     * 首充完成时间
     */
    val rechTime: Long = 0,
    /**
     * 1是可以参与签到，0是无签到
     */
    val isSign: Int = 0,
    /**
     * 签到可以领取的奖励
     */
     val signReward: Int?,
    /**
     * 次日奖励金额
     */
    val rewardAmount: Int?

):Parcelable{
    fun getCurrentDepositConfig():FirstDepositConfig? =
      when(userStatus){
          0->  activityConfigDailyTimeLimit
          1->  isFirstDeposit
          2,4->  activityConfigAfterDay
          3,5->  activityConfigAfterLimitDay
          else-> null
      }
}