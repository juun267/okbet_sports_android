package org.cxct.sportlottery.network.user.info

data class UserBasicInfoRequest(
    val fullName:String?,
    val birthDay:String?,
    val salarySource:Int?,
    val province:String?,
    val city:String?
)