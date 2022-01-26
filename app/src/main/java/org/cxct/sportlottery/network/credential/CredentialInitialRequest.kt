package org.cxct.sportlottery.network.credential

data class CredentialInitialRequest(
     val metaInfo: String? = null,//设备信息
     val docType: String? = null //证件类型
)

enum class DocType(val showName: String, val value: String){
    UM_ID("UM ID","00630000001"), //Unified Multi-Purpose identity card
    TIN_ID("TIN ID","00630000002"), //Taxpayer Identification Number
    PASSPORT("Passport","00000001003"),
    PHIL_HEALTH_CARD("PhilHealth Card","00630000024"),
    DRIVE_LICENSE("Driver’s license","00630000004"),
    SSS_ID("SSS ID","00630000020"), //Social Security System
    VOTER_ID("Voter's ID","00630000022"),
    HKID_1("HKID1","08520000001"), //Current HKID (Hong Kong Identity Card)
    HKID_2("HKID2","08520000002"), //New HKID (Hong Kong Identity Card)
    MY_KAD("MyKad","00600000001"),
    CHINA_ID("China ID","00860000001"),
    //確定不會用到後，可刪除下列選項
//    Postal_ID("Postal ID","00630000016"),
//    PRC_ID("PRC ID","00630000017"),
//    PASSPORT_OLD("Passport-old version","00630000031"),
//    PASSPORT_NEW("Passport-new version","00630000032"),
//    E_KTP("e-KTP","00620000001"),
}