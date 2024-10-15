package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class KYCVerifyConfig(
    val kycVerifyBirthplaceRequired: Int,
    val kycVerifyBirthplaceShow: Int,
    val kycVerifyCurrAddressRequired: Int,
    val kycVerifyCurrAddressShow: Int,
    val kycVerifyGenderRequired: Int,
    val kycVerifyGenderShow: Int,
    val kycVerifyIncomeRequired: Int,
    val kycVerifyIncomeShow: Int,
    val kycVerifyNationalityRequired: Int,
    val kycVerifyNationalityShow: Int,
    val kycVerifyPermanentAddressRequired: Int,
    val kycVerifyPermanentAddressShow: Int,
    val kycVerifyWorkRequired: Int,
    val kycVerifyWorkShow: Int
)