package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class GameStatus(val code: Int) {
    NOT_STARTED(0),
    NOW_PLAYING(1),
    ENDED(2),
    POSTPONED(3),
    CANCELED(4)
}
