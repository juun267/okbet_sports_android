package org.cxct.sportlottery.net.user.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.ArithUtil

@KeepMembers
@Parcelize
data class UserVip(
    val exp: Long,
    val levelCode: String?,
    val levelName: String,
    val protectionLevelGrowthValue: Long,
    val protectionStatus: Int,
    val rewardInfo: List<RewardInfo>,
    val upgradeExp: Long,
    val birthday: String?=null
): Parcelable{
    fun getExpPercent():Double{
       return if (upgradeExp==0L||exp>upgradeExp){
            100.0
        }else{
            ArithUtil.div(ArithUtil.mul(exp.toDouble(),100.0),upgradeExp.toDouble())
        }
    }
}