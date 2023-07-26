package org.cxct.sportlottery.network.credential

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class CredentialInitialRequest(
     val metaInfo: String? = null,//设备信息
     val docType: String? = null //证件类型
)

//enum class DocType(val showName: String, val value: String){
//    PASSPORT("Passport","00000001003"),
//    UM_ID("UM ID","00630000001"), //Unified Multi-Purpose identity card
//    TIN_ID("TIN ID","00630000002"), //Taxpayer Identification Number
//    DRIVE_LICENSE("Driver’s license","00630000004"),
//    PHIL_HEALTH_ID("PhilHealth ID","00630000024"),
//    SSS_ID("SSS ID","00630000020"), //Social Security System
//    POSTAL_ID("Postal ID", "00630000016"),
//    PRC_ID("PRC ID", "00630000017"),
//    VOTER_ID("Voter's ID","00630000022"),
//    PASSPORT_OLD("Passport-old version(PH)", "00630000031"),
//    PASSPORT_NEW("Passport-new version(PH)", "00630000032")
//}