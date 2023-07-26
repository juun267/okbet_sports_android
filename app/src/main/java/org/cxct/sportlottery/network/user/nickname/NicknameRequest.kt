package org.cxct.sportlottery.network.user.nickname

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class NicknameRequest(val nickName: String)
