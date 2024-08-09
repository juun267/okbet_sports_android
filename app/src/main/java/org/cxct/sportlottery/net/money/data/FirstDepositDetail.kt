package org.cxct.sportlottery.net.money.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class FirstDepositDetail(
    /**
     * 隔日奖励 (如果为空 则表示无法获取首冲隔日奖励)
     */
    val activityConfigAfterDay: IsFirstDeposit?=null,
    /**
     * 限时首充奖励
     */
    val activityConfigDailyTimeLimit: IsFirstDeposit?=null,
    /**
     * 限时首充到期时间(如果为null则表示用户无资格参加限时
     */
    val expireTime: Int=0,
    /**
     * 普通首充详情 参数与dailyConfig返回的充值字段一致
     */
    val isFirstDeposit: IsFirstDeposit?=null
):Parcelable{

    /**
     * 获取当前首充状态，1为限时首充，2为普通首充，3为隔天领取活动，0无活动可以参与
     */
    fun getDepositState():Int=
        when{
            expireTime>0 -> 1
            isFirstDeposit!=null-> 2
            activityConfigAfterDay!=null -> 3
            else -> 0
        }


}