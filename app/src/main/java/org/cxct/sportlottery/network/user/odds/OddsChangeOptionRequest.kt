package org.cxct.sportlottery.network.user.odds

import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author kevin
 * @create 2022/6/6
 * @description
 */
@KeepMembers
data class OddsChangeOptionRequest(
    val oddsChangeOption: Int
)
