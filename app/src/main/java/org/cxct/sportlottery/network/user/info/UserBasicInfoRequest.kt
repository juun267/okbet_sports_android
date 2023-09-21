package org.cxct.sportlottery.network.user.info

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class UserBasicInfoRequest(
    val fullName:String?,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val birthday:String?,
    val salarySource:Int?,
    val province:String?,
    val city:String?,
    val phone:String?,
    val email:String
)