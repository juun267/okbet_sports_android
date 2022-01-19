package org.cxct.sportlottery.network.credential

data class CredentialCompleteRequest(
     val transactionId: String? = null,
     val isReturnImage: String? = "Y" //是否需要传回扫描脸部图片 N 和 Y
)
