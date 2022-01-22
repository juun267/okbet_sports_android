package org.cxct.sportlottery.network.credential

data class CredentialInitialRequest(
     val metaInfo: String? = null,//设备信息
     val docType: String? = null //证件类型
)

enum class DocType(val value: String){
    Passport("00000001003"), UMID("00630000001")
}