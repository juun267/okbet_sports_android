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
    X12_INCL_OT("1X2-INCL-OT"),
    HDP_SEG1("HDP-SEG1"),
    X12_SEG1("1X2-SEG1"),
    TG_OU_H_INCL_OT("TG&O/U-H-INCL-OT"),
    TG_OU_C_INCL_OT("TG&O/U-C-INCL-OT"),
    HDP_1ST("HDP-1ST"),
    OU_1ST("O/U-1ST"),
    TG_OU_H_1ST("TG&O/U-H-1ST"),
    TG_OU_C_1ST("TG&O/U-C-1ST"),
    X12_1ST("1X2-1ST"),
    BTS("BTS"),
    OE("O/E")
}