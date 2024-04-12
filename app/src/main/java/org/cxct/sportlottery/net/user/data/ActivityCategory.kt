package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ActivityCategory(val id: Int, val platformId: Int, val name: String)
