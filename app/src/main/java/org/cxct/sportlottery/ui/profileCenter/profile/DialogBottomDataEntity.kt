package org.cxct.sportlottery.ui.profileCenter.profile

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class DialogBottomDataEntity(
    var name: String = "",
    var flag: Boolean = false,
    var id: Int = 0
) {
}