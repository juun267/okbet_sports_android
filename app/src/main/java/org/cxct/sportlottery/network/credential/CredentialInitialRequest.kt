package org.cxct.sportlottery.network.credential

data class CredentialInitialRequest(
     val metaInfo: String? = null,//设备信息
     val docType: String? = null //证件类型
)

enum class DocType(val showName: String, val value: String){
    Passport("Passport","00000001003"), UM_ID("UM ID","00630000001")
}