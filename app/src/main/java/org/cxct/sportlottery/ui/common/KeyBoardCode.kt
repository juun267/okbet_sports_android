package org.cxct.sportlottery.ui.common

import org.cxct.sportlottery.R

enum class KeyBoardCode (val code: Int, val value: String){
    DELETE(-999, ""),
    INSERT_0(48, "0"),
    INSERT_00(-48, "00"),
    PLUS_100(-100, "100"),
    PLUS_1000(-1000, "1000"),
    PLUS_10000(-10000, "10000")
}