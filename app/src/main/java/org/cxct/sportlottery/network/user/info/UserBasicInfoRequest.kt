package org.cxct.sportlottery.network.user.info

data class UserBasicInfoRequest(
    val fullName:String?,
    val birthday:String?,
    val salarySource:Int?,
    val province:String?,
    val city:String?,
    val phone:String?,
    val email:String
)