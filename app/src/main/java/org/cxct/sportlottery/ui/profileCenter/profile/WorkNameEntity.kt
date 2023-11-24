package org.cxct.sportlottery.ui.profileCenter.profile

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
data class WorkNameEntity(
    var rows: MutableList<String>,
    override val code: Int,
    override val msg: String,
    override val success: Boolean
): BaseResult()