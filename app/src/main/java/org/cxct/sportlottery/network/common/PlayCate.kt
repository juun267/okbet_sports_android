package org.cxct.sportlottery.network.common

enum class PlayCate(val value: String) {

    UNCHECK("UNCHECK"),//未確定

    HDP("HDP"),//让球
    OU("O/U"),//大小
    OU_1ST("O/U-1ST"),//大/小-上半场
    OU_2ST("O/U-2ST"),//大/小-下半场
    CS("CS"),//波胆
    FGLG("FG/LG"),//最先进球/最后进球
    DC("DC"),//双重机会
    OE("O/E"),//单/双
    SCO("SCO"),//进球球员
    TG("TG"),//总进球数
    TG_("TG-"),//进球数-半場
    BTS("BTS"),//双方球队进球
    GT1ST("GT1ST"),//首个入球时间
    SBH("SBH"),//双半场进球
    WBH("WBH"),//赢得所有半场
    WEH("WEH"),//赢得任一半场
    WM("WM"),//净胜球数
    CLSH("CLSH"),//零失球
    HTFT("HT/FT"),//半场/全场
    W3("W3"),//三项让球
    TG_OU("TG&O/U"),//球队进球数&大/小
    C_OU("CORNER-O/U"),//角球大/小
    C_OE("CORNER-OE"),//角球单/双
    OU_I_OT("O/U-INCL-OT"),//大/小(含加时)
    OU_SEG("O/U-SEG"),//总得分大/小-第X节
    SINGLE_OU("1X2-O/U"),//独赢大/小

    SINGLE_FLG("1X2-FLG"),//独赢-最先進球
    OU_BTS("O/U-BTS"),//大小&双方球队进球
    SINGLE_BTS("1X2-BTS"),//独赢&双方球队进球
    DC_OU("DC-O/U"),//双重机会&大小

    //single
    SINGLE("1X2"),//独赢
    SINGLE_1ST("1X2-1ST"),//独赢-上半場
    SINGLE_2ST("1X2-2ST"),//独赢-下半場
    SINGLE_OT("1X2-INCL-OT"),//独赢(含加时)
    SINGLE_SEG("1X2-SEG"),//独赢-第X节
    SINGLE_SEG1("1X2-SEG1"),

    //single two item
    SINGLE_2("1X2"),
    SINGLE_1ST_2("1X2-1ST"),
    SINGLE_2ST_2("1X2-2ST"),
    SINGLE_OT_2("1X2-INCL-OT"),
    SINGLE_SEG_2("1X2-SEG"),

    HWMG_SINGLE("HWMG&1X2"),
    HDP_ONE_LIST("HDP"),
    NGOAL_1("NGOAL:1"),//第1个进球
    TWTN("TWTN"),//零失球獲勝
    DC_FLG("DC-FLG"),//双重机会&首次进球队伍
    DC_BTS("DC-BTS"),//双重机会&双方球队进球
    OU_OE("O/U-O/E"),//进球 大/小&进球数 单/双
    OU_TTS1ST("O/U-TTS1ST"),//进球 大/小&首次进球队伍

    OU_HDP("HDP&OU"),
    SET_HDP("SET-HDP"),
    OUTRIGHT("OUTRIGHT"),
    HDP_INCL_OT("HDP-INCL-OT"),
    HDP_SEG1("HDP-SEG1"),
    TG_OU_H_INCL_OT("TG&O/U-H-INCL-OT"),
    TG_OU_C_INCL_OT("TG&O/U-C-INCL-OT"),
    HDP_1ST("HDP-1ST"),
    TG_OU_H_1ST("TG&O/U-H-1ST"),
    TG_OU_C_1ST("TG&O/U-C-1ST"),

    EPS("EPS") //更优赔率
}