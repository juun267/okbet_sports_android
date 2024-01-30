package org.cxct.sportlottery.ui.profileCenter.profile

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
data class UserInfoDetailsEntity(
    val t: Uide, override val code: Int, override val msg: String, override val success: Boolean
) : BaseResult()

@KeepMembers
data class Uide(
    var address: String? = "",
    val birthday: String? = "",
    val gender: Int?=null,
    val city: String? = "",
    val nationality: String? = "",
    val natureOfWork: String? = "",
    var permanentAddress: String? = "",
    val permanentCity: String? = "",
    val permanentProvince: String? = "",
    var permanentZipCode: String? = "",
    var placeOfBirth: String? = "",
    val province: String? = "",
    val salarySource: SalarySource? = null,
    var zipCode: String? = ""
)

@KeepMembers
data class SalarySource(
    val id: Int? = -1,
    val name: String? = ""
)