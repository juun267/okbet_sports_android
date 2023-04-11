package org.cxct.sportlottery.net.user.data

import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
class UserBasicInfoResponse(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    val  t:UserBasicData
) : BaseResult(){


    class UserBasicData(
        val fullName:String?,
        val birthday:String?,
        val salarySource:Int?,
        val province:String?,
        val city:String?
    )
}