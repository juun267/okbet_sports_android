package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
class UserBasicInfoResponse(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    val  t:UserBasicData
) : BaseResult(){


    @KeepMembers
    class UserBasicData(
        val firstName: String?,
        val middleName: String?,
        val lastName: String?,
        val fullName:String?,
        val birthday:String?,
        val salarySource:Int?,
        val province:String?,
        val city:String?,
        var phone:String?,
        var email:String?
    )
}