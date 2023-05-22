package org.cxct.sportlottery.network.credential

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class CredentialCompleteRequest(
     val transactionId: String? = null,
     val isReturnImage: String? = "Y" //是否需要传回扫描脸部图片 N 和 Y
)
