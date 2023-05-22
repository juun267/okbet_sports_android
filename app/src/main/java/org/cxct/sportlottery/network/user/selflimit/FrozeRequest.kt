package org.cxct.sportlottery.network.user.selflimit

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class FrozeRequest(val frozeDay: Int)
