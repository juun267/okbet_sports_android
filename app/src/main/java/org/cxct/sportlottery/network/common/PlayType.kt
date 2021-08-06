package org.cxct.sportlottery.network.common

enum class PlayType(val code: String) {
    OU_HDP("O/U&HDP"),
    X12("1X2"),
    OU("O/U"),
    HDP("HDP"),
    SET_HDP("SET-HDP"),
    OUTRIGHT("OUTRIGHT"),
    HDP_INCL_OT("HDP-INCL-OT"),
    OU_INCL_OT("O/U-INCL-OT"),
    X12_INCL_OT("1X2-INCL-OT")
}