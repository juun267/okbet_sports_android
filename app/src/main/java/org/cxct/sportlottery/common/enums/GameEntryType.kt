package org.cxct.sportlottery.common.enums

import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
enum class GameEntryType(val key: String) {
    OKGAMES("OK_GAMES"),
    OKLIVE("OK_LIVE"),
}