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
    var birthday: String? = "",
    var gender: Int?=null,
    var city: String? = "",
    var nationality: String? = "",
    var natureOfWork: String? = "",
    var permanentAddress: String? = "",
    var permanentCity: String? = "",
    var permanentProvince: String? = "",
    var permanentZipCode: String? = "",
    var placeOfBirth: String? = "",
    var province: String? = "",
    var salarySource: SalarySource? = null,
    var zipCode: String? = ""
)

@KeepMembers
data class SalarySource(
    val id: Int? = -1,
    val name: String? = ""
)