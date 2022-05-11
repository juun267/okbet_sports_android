package org.cxct.sportlottery.network.common

enum class PlayCate(val value: String) {

    UNCHECK("UNCHECK"),//未確定

    ADVANCE("ADVANCE"),//會晉級
    BTS("BTS"),//双方球队进球
    BTS_1ST("BTS-1ST"),
    BTS_2ST("BTS-2ST"),
    BTS_OT("BTS-OT"),
    CLSH("CLSH"),//零失球
    CORNER_1ST_HDP("CORNER-1ST-HDP"),
    CORNER_1ST_OE("CORNER-1ST-OE"),
    CORNER_1ST_OU("CORNER-1ST-O/U"),
    CORNER_1ST_SINGLE("CORNER-1ST-1X2"),
    CORNER_2ST_OU("CORNER-2ST-O/U"),
    CORNER_DC("CORNER-DC"),
    CORNER_FIRST("CORNER-FIRST"),
    CORNER_HDP("CORNER-HDP"),
    CORNER_HDP_SEG1("CORNER-HDP-SEG1"),
    CORNER_HDP_SEG2("CORNER-HDP-SEG2"),
    CORNER_HDP_SEG3("CORNER-HDP-SEG3"),
    CORNER_HDP_SEG4("CORNER-HDP-SEG4"),
    CORNER_HDP_SEG5("CORNER-HDP-SEG5"),
    CORNER_HDP_SEG6("CORNER-HDP-SEG6"),
    CORNER_LAST("CORNER-LAST"),
    CORNER_OE("CORNER-OE"),//角球单/双
    CORNER_OU("CORNER-O/U"),//角球大/小
    CORNER_OU_SEG1("CORNER-O/U-SEG1"),
    CORNER_OU_SEG2("CORNER-O/U-SEG2"),
    CORNER_OU_SEG3("CORNER-O/U-SEG3"),
    CORNER_OU_SEG4("CORNER-O/U-SEG4"),
    CORNER_OU_SEG5("CORNER-O/U-SEG5"),
    CORNER_OU_SEG6("CORNER-O/U-SEG6"),
    CORNER_SINGLE("CORNER-1X2"),
    CORNER_SINGLE_SEG1("CORNER-1X2-SEG1"),
    CORNER_SINGLE_SEG2("CORNER-1X2-SEG2"),
    CORNER_SINGLE_SEG3("CORNER-1X2-SEG3"),
    CORNER_SINGLE_SEG4("CORNER-1X2-SEG4"),
    CORNER_SINGLE_SEG5("CORNER-1X2-SEG5"),
    CORNER_SINGLE_SEG6("CORNER-1X2-SEG6"),
    CS("CS"),//波胆
    CS_1ST_SD("CS-1ST-SD"),
    CS_OT("CS-OT"),
    CS_SEG("CS-SEG"),
    CS_SEG1("CS-SEG1"),
    LCS("LCS"),//反波胆
    DC("DC"),//双重机会
    DC_BTS("DC-BTS"),//双重机会&双方球队进球
    DC_FLG("DC-FLG"),//双重机会&首次进球队伍
    DC_OT("DC-OT"),
    DC_OU("DC-O/U"),//双重机会&大小
    DOUBLE_D("DOUBLE-D"),
    EPS("EPS"), //更优赔率
    EXTRA_TIME("EXTRA-TIME"),
    FGLG("FG/LG"),//最先进球/最后进球
    FGM("FGM"),
    FOUL_BALL_FIRST("FOUL-BALL-FIRST"),
    FOUL_BALL_LAST("FOUL-BALL-LAST"),
    FREE_KICK_FIRST("FREE-KICK-FIRST"),
    FREE_KICK_LAST("FREE-KICK-LAST"),
    FS_LD_C("FS-LD-C"),
    FS_LD_H("FS-LD-H"),
    DOUBLE_D_P("DOUBLE-D-P"),
    TRIPLE_D_P("TRIPLE-D-P"),
    FSM("FSM"),
    GOAL_KICK_FIRST("GOAL-KICK-FIRST"),
    GOAL_KICK_LAST("GOAL-KICK-LAST"),
    GT1ST("GT1ST"),//首个入球时间
    GT3("GT3"),
    HDP("HDP"),//让球
    HDP_1ST("HDP-1ST"),
    HDP_1ST_OT("HDP-1ST-OT"),
    HDP_2ST("HDP-2ST"),
    HDP_2ST_INCL_OT("HDP-2ST-INCL-OT"),
    HDP_INCL_OT("HDP-INCL-OT"),
    HDP_OT("HDP-OT"),
    HDP_SEG1("HDP-SEG1"),
    HDP_SEG2("HDP-SEG2"),
    HDP_SEG3("HDP-SEG3"),
    HDP_SEG4("HDP-SEG4"),
    HDP_SEG5("HDP-SEG5"),
    HDP_SEG6("HDP-SEG6"),
    HT_FT("HT/FT"),//半场/全场
    HWMG("HWMG"),
    HWMG_SINGLE("HWMG&1X2"),
    KICK_OFF("KICK-OFF"),
    LOSE_SEG1_CHAMP("LOSE-SEG1&CHAMP"),
    LS_SEG1("LS-SEG1"),
    LS_SEG2("LS-SEG2"),
    LS_SEG3("LS-SEG3"),
    LS_SEG4("LS-SEG4"),
    LSM("LSM"),
    N_CORNER_GOAL("N-CORNER-GOAL"),
    NGOAL("NGOAL"),//第N个进球
    NGOAL_OT("NGOAL-OT"),
    NPENALTY("NPENALTY"),
    OE("O/E"),//单/双
    Q_OE("-OE"),//快捷玩法的单/双
    OE_1ST("O/E-1ST"),
    OE_2ST_INCL_OT("O/E-2ST-INCL-OT"),
    OE_INCL_OT("O/E-INCL-OT"),
    OE_OT("O/E-OT"),
    OE_SEG1("O/E-SEG1"),
    OE_SEG2("O/E-SEG2"),
    OE_SEG3("O/E-SEG3"),
    OE_SEG4("O/E-SEG4"),
    OE_SEG5("O/E-SEG5"),
    OFFSIDE_FIRST("OFFSIDE-FIRST"),
    OFFSIDE_LAST("OFFSIDE-LAST"),
    OU("O/U"),//大小
    OU_1ST("O/U-1ST"),//大/小-上半场
    OU_1ST_OT("O/U-1ST-OT"),
    OU_2ST("O/U-2ST"),//大/小-下半场
    OU_2ST_INCL_OT("O/U-2ST-INCL-OT"),
    OU_BTS("O/U-BTS"),//大小&双方球队进球
    OU_HDP("HDP&OU"),
    OU_INCL_OT("O/U-INCL-OT"),//大/小(含加时)
    OU_OE("O/U-O/E"),//进球 大/小&进球数 单/双
    OU_OT("O/U-OT"),
    OU_SEG1("O/U-SEG1"),
    OU_SEG2("O/U-SEG2"),
    OU_SEG3("O/U-SEG3"),
    OU_SEG4("O/U-SEG4"),
    OU_SEG5("O/U-SEG5"),
    OU_SEG6("O/U-SEG6"),
    OU_TTS1ST("O/U-TTS1ST"),//进球 大/小&首次进球队伍
    OUTRIGHT("OUTRIGHT"),
    OWN_GOAL("OWN-GOAL"),
    P_ASSIST_OU("P-ASSIST-O/U"),
    P_BLOCK_OU("P-BLOCK-O/U"),
    P_DC("P-DC"),
    P_HDP("P-HDP"),
    P_HDP_1ST("P-HDP-1ST"),
    P_OE("P-O/E"),
    P_OE_1ST("P-O/E-1ST"),
    P_OU("P-O/U"),
    P_OU_1ST("P-O/U-1ST"),
    P_REBOUND_OU("P-REBOUND-O/U"),
    P_SCO_OU("P-SCO-O/U"),
    P_SINGLE("P-1X2"),
    P_SINGLE_1ST("P-1X2-1ST"),
    P_STEAL_OU("P-STEAL-O/U"),
    P_THREE_OU("P-THREE-O/U"),
    PENALTY("PENALTY"),
    PENALTY_1ST_HDP("PENALTY-1ST-HDP"),
    PENALTY_1ST_OE("PENALTY-1ST-OE"),
    PENALTY_1ST_OU("PENALTY-1ST-O/U"),
    PENALTY_1ST_SINGLE("PENALTY-1ST-1X2"),
    PENALTY_AWARDED("PENALTY-AWARDED"),
    PENALTY_DC("PENALTY-DC"),
    PENALTY_FIRST("PENALTY-FIRST"),
    PENALTY_HDP("PENALTY-HDP"),
    PENALTY_HDP_SEG1("PENALTY-HDP-SEG1"),
    PENALTY_HDP_SEG2("PENALTY-HDP-SEG2"),
    PENALTY_HDP_SEG3("PENALTY-HDP-SEG3"),
    PENALTY_HDP_SEG4("PENALTY-HDP-SEG4"),
    PENALTY_HDP_SEG5("PENALTY-HDP-SEG5"),
    PENALTY_HDP_SEG6("PENALTY-HDP-SEG6"),
    PENALTY_LAST("PENALTY-LAST"),
    PENALTY_OE("PENALTY-OE"),
    PENALTY_OU("PENALTY-O/U"),
    PENALTY_OU_SEG1("PENALTY-O/U-SEG1"),
    PENALTY_OU_SEG2("PENALTY-O/U-SEG2"),
    PENALTY_OU_SEG3("PENALTY-O/U-SEG3"),
    PENALTY_OU_SEG4("PENALTY-O/U-SEG4"),
    PENALTY_OU_SEG5("PENALTY-O/U-SEG5"),
    PENALTY_OU_SEG6("PENALTY-O/U-SEG6"),
    PENALTY_SINGLE("PENALTY-1X2"),
    PENALTY_SINGLE_SEG1("PENALTY-1X2-SEG1"),
    PENALTY_SINGLE_SEG2("PENALTY-1X2-SEG2"),
    PENALTY_SINGLE_SEG3("PENALTY-1X2-SEG3"),
    PENALTY_SINGLE_SEG4("PENALTY-1X2-SEG4"),
    PENALTY_SINGLE_SEG5("PENALTY-1X2-SEG5"),
    PENALTY_SINGLE_SEG6("PENALTY-1X2-SEG6"),
    PK("PK"),
    QUALIFYING_METHOD("QUALIFYING_METHOD"),
    RED_CARD_PLAYER("RED-CARD-PLAYER"),
    RTG2("RTG2"),
    RTG3("RTG3"),
    RTP10("RTP10"),
    RTP20("RTP20"),
    SBH("SBH"),//双半场进球
    SCO("SCO"),//进球球员
    SEG_POINT("SEG-POINT"),
    SET_HDP("SET-HDP"),
    SINGLE("1X2"),//独赢
    SINGLE_ND("1X2-ND"),//独赢(无和)
    SINGLE_1ST("1X2-1ST"),//独赢-上半場
    SINGLE_1ST_ND("1X2-1ST-ND"),
    SINGLE_1ST_OT("1X2-1ST-OT"),
    SINGLE_2ST("1X2-2ST"),//独赢-下半場
    SINGLE_2ST_INCL_OT("1X2-2ST-INCL-OT"),
    SINGLE_BTS("1X2-BTS"),//独赢&双方球队进球
    SINGLE_BTS_Y("1X2-BTS-Y"),
    SINGLE_BTS_N("1X2-BTS-N"),
    SINGLE_FLG("1X2-FLG"),//独赢-最先進球
    SINGLE_INCL_OT("1X2-INCL-OT"),
    SINGLE_OT("1X2-OT"),
    SINGLE_OU("1X2-O/U"),//独赢大/小
    SINGLE_OU_O("1X2-O/U-O"),
    SINGLE_OU_U("1X2-O/U-U"),
    SINGLE_SEG1("1X2-SEG1"),
    SINGLE_SEG1_GAMES_1("1X2_SEG1_GAMES:1"),
    SINGLE_SEG1_GAMES_2("1X2_SEG1_GAMES:2"),
    SINGLE_SEG1_GAMES_3("1X2_SEG1_GAMES:3"),
    SINGLE_SEG1_GAMES_4("1X2_SEG1_GAMES:4"),
    SINGLE_SEG1_GAMES_5("1X2_SEG1_GAMES:5"),
    SINGLE_SEG1_ND("1X2-SEG1-ND"),
    SINGLE_SEG2("1X2-SEG2"),
    SINGLE_SEG2_GAMES_1("1X2_SEG2_GAMES:1"),
    SINGLE_SEG2_GAMES_2("1X2_SEG2_GAMES:2"),
    SINGLE_SEG2_GAMES_3("1X2_SEG2_GAMES:3"),
    SINGLE_SEG2_GAMES_4("1X2_SEG2_GAMES:4"),
    SINGLE_SEG2_GAMES_5("1X2_SEG2_GAMES:5"),
    SINGLE_SEG2_ND("1X2-SEG2-ND"),
    SINGLE_SEG3("1X2-SEG3"),
    SINGLE_SEG3_GAMES_1("1X2_SEG3_GAMES:1"),
    SINGLE_SEG3_GAMES_2("1X2_SEG3_GAMES:2"),
    SINGLE_SEG3_GAMES_3("1X2_SEG3_GAMES:3"),
    SINGLE_SEG3_GAMES_4("1X2_SEG3_GAMES:4"),
    SINGLE_SEG3_GAMES_5("1X2_SEG3_GAMES:5"),
    SINGLE_SEG3_ND("1X2-SEG3-ND"),
    SINGLE_SEG4("1X2-SEG4"),
    SINGLE_SEG4_GAMES_1("1X2_SEG4_GAMES:1"),
    SINGLE_SEG4_GAMES_2("1X2_SEG4_GAMES:2"),
    SINGLE_SEG4_GAMES_3("1X2_SEG4_GAMES:3"),
    SINGLE_SEG4_GAMES_4("1X2_SEG4_GAMES:4"),
    SINGLE_SEG4_GAMES_5("1X2_SEG4_GAMES:5"),
    SINGLE_SEG4_ND("1X2-SEG4-ND"),
    SINGLE_SEG5("1X2-SEG5"),
    SINGLE_SEG5_GAMES_1("1X2_SEG5_GAMES:1"),
    SINGLE_SEG5_GAMES_2("1X2_SEG5_GAMES:2"),
    SINGLE_SEG5_GAMES_3("1X2_SEG5_GAMES:3"),
    SINGLE_SEG5_GAMES_4("1X2_SEG5_GAMES:4"),
    SINGLE_SEG5_GAMES_5("1X2_SEG5_GAMES:5"),
    SINGLE_SEG6("1X2-SEG6"),
    SINGLE_SEG7("1X2-SEG7"),
    SINGLE_SEG8("1X2-SEG8"),
    SUBSTITUTION_FIRST("SUBSTITUTION-FIRST"),
    SUBSTITUTION_LAST("SUBSTITUTION-LAST"),
    TG("TG"),//总进球数
    TG_1ST("TG-1ST"),
    TG_OE_C("TG&O/E-C"),
    TG_OE_C_1ST("TG&O/E-C-1ST"),
    TG_OE_H("TG&O/E-H"),
    TG_OE_H_1ST("TG&O/E-H-1ST"),
    TG_OT("TG-OT"),
    TG_OU("TG&O/U"),//球队进球数&大/小
    TG_OU_C("TG&O/U-C"),
    TG_OU_C_1ST("TG&O/U-C-1ST"),
    TG_OU_C_2ST_INCL_OT("TG&O/U-C-2ST-INCL-OT"),
    TG_OU_C_INCL_OT("TG&O/U-C-INCL-OT"),
    TG_OU_C_SEG1("TG&O/U-C-SEG1"),
    TG_OU_C_SEG2("TG&O/U-C-SEG2"),
    TG_OU_C_SEG3("TG&O/U-C-SEG3"),
    TG_OU_C_SEG4("TG&O/U-C-SEG4"),
    TG_OU_H("TG&O/U-H"),
    TG_OU_H_1ST("TG&O/U-H-1ST"),
    TG_OU_H_2ST_INCL_OT("TG&O/U-H-2ST-INCL-OT"),
    TG_OU_H_INCL_OT("TG&O/U-H-INCL-OT"),
    TG_OU_H_SEG1("TG&O/U-H-SEG1"),
    TG_OU_H_SEG2("TG&O/U-H-SEG2"),
    TG_OU_H_SEG3("TG&O/U-H-SEG3"),
    TG_OU_H_SEG4("TG&O/U-H-SEG4"),
    TG_OU_OT_C("TG&O/U-OT-C"),
    TG_OU_OT_H("TG&O/U-OT-H"),
    TIE_BREAK("TIE-BREAK"),
    TRIPLE_D("TRIPLE-D"),
    TWFB("TWFB"),
    TWTN("TWTN"),//零失球獲勝
    W3("W3"),//三项让球
    WBH("WBH"),//赢得所有半场
    WEH("WEH"),//赢得任一半场
    WIN_SEG1_CHAMP("WIN-SEG1&CHAMP"),
    WINNER("WINNER"),
    WM("WM"),//净胜球数
    WM_INCL_OT("WM-INCL-OT"),
    WM_SD("WM-SD"),
    WM_SEG1("WM-SEG1"),
    WM_SEG2("WM-SEG2"),
    WM_SEG3("WM-SEG3"),
    WM_SEG4("WM-SEG4"),
    WM3_SD_OT("WM3-SD-OT"),
    GTD("GTD"),
    MOV("MOV"),
    MOV_UFC("MOV-UFC"),
    ROUND("ROUND"),
    ROUND_UFC("ROUND-UFC"),
    TO_WIN_THE_TOSS("TO-WIN-THE-TOSS"),//抛币中获胜
    TOP_TEAM_BATSMAN_H("TOP-TEAM-BATSMAN-H"),//球队最佳击球手|{H}
    TOP_TEAM_BATSMAN_C("TOP-TEAM-BATSMAN-C"),//球队最佳击球手|{C}
    TOP_TEAM_BOWLER_H("TOP-TEAM-BOWLER-H"),//球队最佳投球手|{H}
    TOP_TEAM_BOWLER_C("TOP-TEAM-BOWLER-C"),//球队最佳投球手|{C}
    MOST_MATCH_FOURS("MOST-MATCH-FOURS"),//Most Match Fours
    MOST_MATCH_SIXES("MOST-MATCH-SIXES"),//获得最多一击满分(6分)的次数
    HIGHEST_OPENING_PARTNERSHIP("HIGHEST-OPENING-PARTNERSHIP"),//最高配对选手开场得分
    RUN_AT_FALL_OF_1ST_WICKET_H("RUN-AT-FALL-OF-1ST-WICKET-H"),//被击倒跑位得分第1三柱门({S}})|{H}
    RUN_AT_FALL_OF_1ST_WICKET_C("RUN-AT-FALL-OF-1ST-WICKET-C"),//被击倒跑位得分第1三柱门({S}})|{C}
    WICKET_METHOD_1ST("1ST-WICKET-METHOD"),//首次取得三柱门的方式
    WICKET_METHOD_H_1ST("1ST-WICKET-METHOD-H"),//首次取得三柱门的方式|{H}
    WICKET_METHOD_C_1ST("1ST-WICKET-METHOD-C"),//首次取得三柱门的方式|{C}
    OVER_RUNS_2_WAY_H_1ST("1ST-OVER-RUNS-2-WAY-H"),
    OVER_RUNS_2_WAY_C_1ST("1ST-OVER-RUNS-2-WAY-C"),
    TWTT("TWTT"),
    T_BATSMAN_H("T-BATSMAN-H"),
    T_BATSMAN_C("T-BATSMAN-C"),
    T_BOWLER_H("T-BOWLER-H"),
    T_BOWLER_C("T-BOWLER-C"),
    MOST_FOUR("MOST-FOUR"),
    MOST_SIX("MOST-SIX"),
    HOP("HOP"),
    RAFO_1ST_W_H("RAFO-1ST-W-H"),
    RAFO_1ST_W_C("RAFO-1ST-W-C"),
    W_METHOD_1ST("1ST-W-METHOD"),
    W_METHOD_H_1ST("1ST-W-METHOD-H"),
    W_METHOD_C_1ST("1ST-W-METHOD-C"),
    O_R_2_WAY_H_1ST("1ST-O-R-2-WAY-H"),
    O_R_2_WAY_C_1ST("1ST-O-R-2-WAY-C"),
    NMO_1ST_H("NMO-1ST-H"),
    NMO_1ST_C("NMO-1ST-C"),
    NMO_2ND_H("NMO-2ND-H"),
    NMO_2ND_C("NMO-2ND-C"),
    FIL("FIL"),
    MODW_1ST_H("MODW-1ST-H"),
    MODW_1ST_C("MODW-1ST-C"),
    MODW_2ND_H("MODW-2ND-H"),
    MODW_2ND_C("MODW-2ND-C"),
    S_RAFO_1ST_W_H("S-RAFO-1ST-W-H"),
    S_RAFO_1ST_W_C("S-RAFO-1ST-W-C"),
    S_RAFO_2ND_W_H("S-RAFO-2ND-W-H"),
    S_RAFO_2ND_W_C("S-RAFO-2ND-W-C"),
    S_MR_1ST_H("S-MR-1ST-H"),
    S_MR_1ST_C("S-MR-1ST-C"),
    S_MR_2ND_H("S-MR-2ND-H"),
    S_MR_2ND_C("S-MR-2ND-C"),
    NMO_H("NMO-H"),
    NMO_C("NMO-C"),
    S_MR_H("S_MR_H"),
    S_MR_C("S_MR_C"),
    MOD_W_H("MOD_W_H"),
    MOD_W_C("MOD_W_C"),
    OU_2_WAY_1ST_C("O/U-2-WAY-1ST-C"),
    OU_2_WAY_1ST_H("O/U-2-WAY-1ST-H"),
    BOTTOM_NAVIGATION("BOTTOM_NAVIGATION"),
    NO_DATA("NO_DATA");

    companion object {
        /**
         * 判斷是否需要顯示spread欄位
         */
        fun needShowSpread(code: String?): Boolean {
            return when (code) {
                DOUBLE_D.value, TRIPLE_D.value, DOUBLE_D_P.value, TRIPLE_D_P.value -> false
                else -> true
            }
        }

        /**
         * 判斷是否需要顯示當前角球資訊
         */
        fun needShowCurrentCorner(code: String?): Boolean {
            return when (code) {
                CORNER_1ST_HDP.value,
                CORNER_1ST_OE.value,
                CORNER_1ST_OU.value,
                CORNER_2ST_OU.value,
                CORNER_HDP.value,
                CORNER_HDP_SEG1.value,
                CORNER_HDP_SEG2.value,
                CORNER_HDP_SEG3.value,
                CORNER_HDP_SEG4.value,
                CORNER_HDP_SEG5.value,
                CORNER_HDP_SEG6.value,
                CORNER_OE.value,//角球单/双
                CORNER_OU.value,//角球大/小
                CORNER_OU_SEG1.value,
                CORNER_OU_SEG2.value,
                CORNER_OU_SEG3.value,
                CORNER_OU_SEG4.value,
                CORNER_OU_SEG5.value,
                CORNER_OU_SEG6.value -> true
                else -> false
            }
        }

        fun getPlayCate(code: String?): PlayCate {
            return when (code) {
                ADVANCE.value -> ADVANCE
                BTS.value -> BTS
                BTS_1ST.value -> BTS_1ST
                BTS_2ST.value -> BTS_2ST
                BTS_OT.value -> BTS_OT
                CLSH.value -> CLSH
                CORNER_1ST_HDP.value -> CORNER_1ST_HDP
                CORNER_1ST_OE.value -> CORNER_1ST_OE
                CORNER_1ST_OU.value -> CORNER_1ST_OU
                CORNER_1ST_SINGLE.value -> CORNER_1ST_SINGLE
                CORNER_2ST_OU.value -> CORNER_2ST_OU
                CORNER_DC.value -> CORNER_DC
                CORNER_FIRST.value -> CORNER_FIRST
                CORNER_HDP.value -> CORNER_HDP
                CORNER_HDP_SEG1.value -> CORNER_HDP_SEG1
                CORNER_HDP_SEG2.value -> CORNER_HDP_SEG2
                CORNER_HDP_SEG3.value -> CORNER_HDP_SEG3
                CORNER_HDP_SEG4.value -> CORNER_HDP_SEG4
                CORNER_HDP_SEG5.value -> CORNER_HDP_SEG5
                CORNER_HDP_SEG6.value -> CORNER_HDP_SEG6
                CORNER_LAST.value -> CORNER_LAST
                CORNER_OE.value -> CORNER_OE
                CORNER_OU.value -> CORNER_OU
                CORNER_OU_SEG1.value -> CORNER_OU_SEG1
                CORNER_OU_SEG2.value -> CORNER_OU_SEG2
                CORNER_OU_SEG3.value -> CORNER_OU_SEG3
                CORNER_OU_SEG4.value -> CORNER_OU_SEG4
                CORNER_OU_SEG5.value -> CORNER_OU_SEG5
                CORNER_OU_SEG6.value -> CORNER_OU_SEG6
                CORNER_SINGLE.value -> CORNER_SINGLE
                CORNER_SINGLE_SEG1.value -> CORNER_SINGLE_SEG1
                CORNER_SINGLE_SEG2.value -> CORNER_SINGLE_SEG2
                CORNER_SINGLE_SEG3.value -> CORNER_SINGLE_SEG3
                CORNER_SINGLE_SEG4.value -> CORNER_SINGLE_SEG4
                CORNER_SINGLE_SEG5.value -> CORNER_SINGLE_SEG5
                CORNER_SINGLE_SEG6.value -> CORNER_SINGLE_SEG6
                CS.value -> CS
                CS_1ST_SD.value -> CS_1ST_SD
                CS_OT.value -> CS_OT
                CS_SEG.value -> CS_SEG
                CS_SEG1.value -> CS_SEG1
                LCS.value -> LCS
                DC.value -> DC
                DC_BTS.value -> DC_BTS
                DC_FLG.value -> DC_FLG
                DC_OT.value -> DC_OT
                DC_OU.value -> DC_OU
                DOUBLE_D.value -> DOUBLE_D
                EPS.value -> EPS
                EXTRA_TIME.value -> EXTRA_TIME
                FGLG.value -> FGLG
                FGM.value -> FGM
                FOUL_BALL_FIRST.value -> FOUL_BALL_FIRST
                FOUL_BALL_LAST.value -> FOUL_BALL_LAST
                FREE_KICK_FIRST.value -> FREE_KICK_FIRST
                FREE_KICK_LAST.value -> FREE_KICK_LAST
                FS_LD_C.value -> FS_LD_C
                FS_LD_H.value -> FS_LD_H
                FSM.value -> FSM
                GOAL_KICK_FIRST.value -> GOAL_KICK_FIRST
                GOAL_KICK_LAST.value -> GOAL_KICK_LAST
                GT1ST.value -> GT1ST
                GT3.value -> GT3
                HDP.value -> HDP
                HDP_1ST.value -> HDP_1ST
                HDP_2ST.value -> HDP_2ST
                HDP_1ST_OT.value -> HDP_1ST_OT
                HDP_2ST_INCL_OT.value -> HDP_2ST_INCL_OT
                HDP_INCL_OT.value -> HDP_INCL_OT
                HDP_OT.value -> HDP_OT
                HDP_SEG1.value -> HDP_SEG1
                HDP_SEG2.value -> HDP_SEG2
                HDP_SEG3.value -> HDP_SEG3
                HDP_SEG4.value -> HDP_SEG4
                HDP_SEG5.value -> HDP_SEG5
                HDP_SEG6.value -> HDP_SEG6
                HT_FT.value -> HT_FT
                HWMG.value -> HWMG
                HWMG_SINGLE.value -> HWMG_SINGLE
                KICK_OFF.value -> KICK_OFF
                LOSE_SEG1_CHAMP.value -> LOSE_SEG1_CHAMP
                LS_SEG1.value -> LS_SEG1
                LS_SEG2.value -> LS_SEG2
                LS_SEG3.value -> LS_SEG3
                LS_SEG4.value -> LS_SEG4
                LSM.value -> LSM
                N_CORNER_GOAL.value -> N_CORNER_GOAL
                NGOAL.value -> NGOAL
                NGOAL_OT.value -> NGOAL_OT
                NPENALTY.value -> NPENALTY
                OE.value -> OE
                OE_1ST.value -> OE_1ST
                OE_2ST_INCL_OT.value -> OE_2ST_INCL_OT
                OE_INCL_OT.value -> OE_INCL_OT
                OE_OT.value -> OE_OT
                OE_SEG1.value -> OE_SEG1
                OE_SEG2.value -> OE_SEG2
                OE_SEG3.value -> OE_SEG3
                OE_SEG4.value -> OE_SEG4
                OE_SEG5.value -> OE_SEG5
                OFFSIDE_FIRST.value -> OFFSIDE_FIRST
                OFFSIDE_LAST.value -> OFFSIDE_LAST
                OU.value -> OU
                OU_1ST.value -> OU_1ST
                OU_1ST_OT.value -> OU_1ST_OT
                OU_2ST.value -> OU_2ST
                OU_2ST_INCL_OT.value -> OU_2ST_INCL_OT
                OU_BTS.value -> OU_BTS
                OU_HDP.value -> OU_HDP
                OU_INCL_OT.value -> OU_INCL_OT
                OU_OE.value -> OU_OE
                OU_OT.value -> OU_OT
                OU_SEG1.value -> OU_SEG1
                OU_SEG2.value -> OU_SEG2
                OU_SEG3.value -> OU_SEG3
                OU_SEG4.value -> OU_SEG4
                OU_SEG5.value -> OU_SEG5
                OU_SEG6.value -> OU_SEG6
                OU_TTS1ST.value -> OU_TTS1ST
                OUTRIGHT.value -> OUTRIGHT
                OWN_GOAL.value -> OWN_GOAL
                P_ASSIST_OU.value -> P_ASSIST_OU
                P_BLOCK_OU.value -> P_BLOCK_OU
                P_DC.value -> P_DC
                P_HDP.value -> P_HDP
                P_HDP_1ST.value -> P_HDP_1ST
                P_OE.value -> P_OE
                P_OE_1ST.value -> P_OE_1ST
                P_OU.value -> P_OU
                P_OU_1ST.value -> P_OU_1ST
                P_REBOUND_OU.value -> P_REBOUND_OU
                P_SCO_OU.value -> P_SCO_OU
                P_SINGLE.value -> P_SINGLE
                P_SINGLE_1ST.value -> P_SINGLE_1ST
                P_STEAL_OU.value -> P_STEAL_OU
                P_THREE_OU.value -> P_THREE_OU
                PENALTY.value -> PENALTY
                PENALTY_1ST_HDP.value -> PENALTY_1ST_HDP
                PENALTY_1ST_OE.value -> PENALTY_1ST_OE
                PENALTY_1ST_OU.value -> PENALTY_1ST_OU
                PENALTY_1ST_SINGLE.value -> PENALTY_1ST_SINGLE
                PENALTY_AWARDED.value -> PENALTY_AWARDED
                PENALTY_DC.value -> PENALTY_DC
                PENALTY_FIRST.value -> PENALTY_FIRST
                PENALTY_HDP.value -> PENALTY_HDP
                PENALTY_HDP_SEG1.value -> PENALTY_HDP_SEG1
                PENALTY_HDP_SEG2.value -> PENALTY_HDP_SEG2
                PENALTY_HDP_SEG3.value -> PENALTY_HDP_SEG3
                PENALTY_HDP_SEG4.value -> PENALTY_HDP_SEG4
                PENALTY_HDP_SEG5.value -> PENALTY_HDP_SEG5
                PENALTY_HDP_SEG6.value -> PENALTY_HDP_SEG6
                PENALTY_LAST.value -> PENALTY_LAST
                PENALTY_OE.value -> PENALTY_OE
                PENALTY_OU.value -> PENALTY_OU
                PENALTY_OU_SEG1.value -> PENALTY_OU_SEG1
                PENALTY_OU_SEG2.value -> PENALTY_OU_SEG2
                PENALTY_OU_SEG3.value -> PENALTY_OU_SEG3
                PENALTY_OU_SEG4.value -> PENALTY_OU_SEG4
                PENALTY_OU_SEG5.value -> PENALTY_OU_SEG5
                PENALTY_OU_SEG6.value -> PENALTY_OU_SEG6
                PENALTY_SINGLE.value -> PENALTY_SINGLE
                PENALTY_SINGLE_SEG1.value -> PENALTY_SINGLE_SEG1
                PENALTY_SINGLE_SEG2.value -> PENALTY_SINGLE_SEG2
                PENALTY_SINGLE_SEG3.value -> PENALTY_SINGLE_SEG3
                PENALTY_SINGLE_SEG4.value -> PENALTY_SINGLE_SEG4
                PENALTY_SINGLE_SEG5.value -> PENALTY_SINGLE_SEG5
                PENALTY_SINGLE_SEG6.value -> PENALTY_SINGLE_SEG6
                PK.value -> PK
                QUALIFYING_METHOD.value -> QUALIFYING_METHOD
                RED_CARD_PLAYER.value -> RED_CARD_PLAYER
                RTG2.value -> RTG2
                RTG3.value -> RTG3
                RTP10.value -> RTP10
                RTP20.value -> RTP20
                SBH.value -> SBH//双半场进球
                SCO.value -> SCO//进球球员
                SEG_POINT.value -> SEG_POINT
                SET_HDP.value -> SET_HDP
                SINGLE.value -> SINGLE//独赢
                SINGLE_ND.value -> SINGLE_ND////独赢(无和)
                SINGLE_1ST.value -> SINGLE_1ST//独赢-上半場
                SINGLE_1ST_ND.value -> SINGLE_1ST_ND
                SINGLE_1ST_OT.value -> SINGLE_1ST_OT
                SINGLE_2ST.value -> SINGLE_2ST//独赢-下半場
                SINGLE_2ST_INCL_OT.value -> SINGLE_2ST_INCL_OT
                SINGLE_BTS.value -> SINGLE_BTS//独赢&双方球队进球
                SINGLE_FLG.value -> SINGLE_FLG//独赢-最先進球
                SINGLE_INCL_OT.value -> SINGLE_INCL_OT
                SINGLE_OT.value -> SINGLE_OT
                SINGLE_OU.value -> SINGLE_OU//独赢大/小
                SINGLE_SEG1.value -> SINGLE_SEG1
                SINGLE_SEG1_GAMES_1.value -> SINGLE_SEG1_GAMES_1
                SINGLE_SEG1_GAMES_2.value -> SINGLE_SEG1_GAMES_2
                SINGLE_SEG1_GAMES_3.value -> SINGLE_SEG1_GAMES_3
                SINGLE_SEG1_GAMES_4.value -> SINGLE_SEG1_GAMES_4
                SINGLE_SEG1_GAMES_5.value -> SINGLE_SEG1_GAMES_5
                SINGLE_SEG1_ND.value -> SINGLE_SEG1_ND
                SINGLE_SEG2.value -> SINGLE_SEG2
                SINGLE_SEG2_GAMES_1.value -> SINGLE_SEG2_GAMES_1
                SINGLE_SEG2_GAMES_2.value -> SINGLE_SEG2_GAMES_2
                SINGLE_SEG2_GAMES_3.value -> SINGLE_SEG2_GAMES_3
                SINGLE_SEG2_GAMES_4.value -> SINGLE_SEG2_GAMES_4
                SINGLE_SEG2_GAMES_5.value -> SINGLE_SEG2_GAMES_5
                SINGLE_SEG2_ND.value -> SINGLE_SEG2_ND
                SINGLE_SEG3.value -> SINGLE_SEG3
                SINGLE_SEG3_GAMES_1.value -> SINGLE_SEG3_GAMES_1
                SINGLE_SEG3_GAMES_2.value -> SINGLE_SEG3_GAMES_2
                SINGLE_SEG3_GAMES_3.value -> SINGLE_SEG3_GAMES_3
                SINGLE_SEG3_GAMES_4.value -> SINGLE_SEG3_GAMES_4
                SINGLE_SEG3_GAMES_5.value -> SINGLE_SEG3_GAMES_5
                SINGLE_SEG3_ND.value -> SINGLE_SEG3_ND
                SINGLE_SEG4.value -> SINGLE_SEG4
                SINGLE_SEG4_GAMES_1.value -> SINGLE_SEG4_GAMES_1
                SINGLE_SEG4_GAMES_2.value -> SINGLE_SEG4_GAMES_2
                SINGLE_SEG4_GAMES_3.value -> SINGLE_SEG4_GAMES_3
                SINGLE_SEG4_GAMES_4.value -> SINGLE_SEG4_GAMES_4
                SINGLE_SEG4_GAMES_5.value -> SINGLE_SEG4_GAMES_5
                SINGLE_SEG4_ND.value -> SINGLE_SEG4_ND
                SINGLE_SEG5.value -> SINGLE_SEG5
                SINGLE_SEG5_GAMES_1.value -> SINGLE_SEG5_GAMES_1
                SINGLE_SEG5_GAMES_2.value -> SINGLE_SEG5_GAMES_2
                SINGLE_SEG5_GAMES_3.value -> SINGLE_SEG5_GAMES_3
                SINGLE_SEG5_GAMES_4.value -> SINGLE_SEG5_GAMES_4
                SINGLE_SEG5_GAMES_5.value -> SINGLE_SEG5_GAMES_5
                SINGLE_SEG6.value -> SINGLE_SEG6
                SINGLE_SEG7.value -> SINGLE_SEG7
                SINGLE_SEG8.value -> SINGLE_SEG8
                SUBSTITUTION_FIRST.value -> SUBSTITUTION_FIRST
                SUBSTITUTION_LAST.value -> SUBSTITUTION_LAST
                TG.value -> TG//总进球数
                TG_1ST.value -> TG_1ST
                TG_OE_C.value -> TG_OE_C
                TG_OE_C_1ST.value -> TG_OE_C_1ST
                TG_OE_H.value -> TG_OE_H
                TG_OE_H_1ST.value -> TG_OE_H_1ST
                TG_OT.value -> TG_OT
                TG_OU.value -> TG_OU//球队进球数_大/小
                TG_OU_C.value -> TG_OU_C
                TG_OU_C_1ST.value -> TG_OU_C_1ST
                TG_OU_C_2ST_INCL_OT.value -> TG_OU_C_2ST_INCL_OT
                TG_OU_C_INCL_OT.value -> TG_OU_C_INCL_OT
                TG_OU_C_SEG1.value -> TG_OU_C_SEG1
                TG_OU_C_SEG2.value -> TG_OU_C_SEG2
                TG_OU_C_SEG3.value -> TG_OU_C_SEG3
                TG_OU_C_SEG4.value -> TG_OU_C_SEG4
                TG_OU_H.value -> TG_OU_H
                TG_OU_H_1ST.value -> TG_OU_H_1ST
                TG_OU_H_2ST_INCL_OT.value -> TG_OU_H_2ST_INCL_OT
                TG_OU_H_INCL_OT.value -> TG_OU_H_INCL_OT
                TG_OU_H_SEG1.value -> TG_OU_H_SEG1
                TG_OU_H_SEG2.value -> TG_OU_H_SEG2
                TG_OU_H_SEG3.value -> TG_OU_H_SEG3
                TG_OU_H_SEG4.value -> TG_OU_H_SEG4
                TG_OU_OT_C.value -> TG_OU_OT_C
                TG_OU_OT_H.value -> TG_OU_OT_H
                TIE_BREAK.value -> TIE_BREAK
                TRIPLE_D.value -> TRIPLE_D
                TWFB.value -> TWFB
                TWTN.value -> TWTN//零失球獲勝
                W3.value -> W3//三项让球
                WBH.value -> WBH//赢得所有半场
                WEH.value -> WEH//赢得任一半场
                WIN_SEG1_CHAMP.value -> WIN_SEG1_CHAMP
                WINNER.value -> WINNER
                WM.value -> WM//净胜球数
                WM_INCL_OT.value -> WM_INCL_OT
                WM_SD.value -> WM_SD
                WM_SEG1.value -> WM_SEG1
                WM_SEG2.value -> WM_SEG2
                WM_SEG3.value -> WM_SEG3
                WM_SEG4.value -> WM_SEG4
                WM3_SD_OT.value -> WM3_SD_OT
                WM3_SD_OT.value -> WM3_SD_OT
                GTD.value -> GTD
                MOV.value -> MOV
                MOV_UFC.value -> MOV_UFC
                ROUND.value -> ROUND
                ROUND_UFC.value -> ROUND_UFC
                TO_WIN_THE_TOSS.value -> TO_WIN_THE_TOSS
                TOP_TEAM_BATSMAN_H.value -> TOP_TEAM_BATSMAN_H
                TOP_TEAM_BATSMAN_C.value -> TOP_TEAM_BATSMAN_C
                TOP_TEAM_BOWLER_H.value -> TOP_TEAM_BOWLER_H
                TOP_TEAM_BOWLER_C.value -> TOP_TEAM_BOWLER_C
                MOST_MATCH_FOURS.value -> MOST_MATCH_FOURS
                MOST_MATCH_SIXES.value -> MOST_MATCH_SIXES
                HIGHEST_OPENING_PARTNERSHIP.value -> HIGHEST_OPENING_PARTNERSHIP
                RUN_AT_FALL_OF_1ST_WICKET_H.value -> RUN_AT_FALL_OF_1ST_WICKET_H
                RUN_AT_FALL_OF_1ST_WICKET_C.value -> RUN_AT_FALL_OF_1ST_WICKET_C
                WICKET_METHOD_1ST.value -> WICKET_METHOD_1ST
                WICKET_METHOD_H_1ST.value -> WICKET_METHOD_H_1ST
                WICKET_METHOD_C_1ST.value -> WICKET_METHOD_C_1ST
                OVER_RUNS_2_WAY_H_1ST.value -> OVER_RUNS_2_WAY_H_1ST
                OVER_RUNS_2_WAY_C_1ST.value -> OVER_RUNS_2_WAY_C_1ST
                TWTT.value -> TWTT
                T_BATSMAN_H.value -> T_BATSMAN_H
                T_BATSMAN_C.value -> T_BATSMAN_C
                T_BOWLER_H.value -> T_BOWLER_H
                T_BOWLER_C.value -> T_BOWLER_C
                MOST_FOUR.value -> MOST_FOUR
                MOST_SIX.value -> MOST_SIX
                HOP.value -> HOP
                RAFO_1ST_W_H.value -> RAFO_1ST_W_H
                RAFO_1ST_W_C.value -> RAFO_1ST_W_C
                W_METHOD_1ST.value -> W_METHOD_1ST
                W_METHOD_H_1ST.value -> W_METHOD_H_1ST
                W_METHOD_C_1ST.value -> W_METHOD_C_1ST
                O_R_2_WAY_H_1ST.value -> O_R_2_WAY_H_1ST
                O_R_2_WAY_C_1ST.value -> O_R_2_WAY_C_1ST
                NMO_1ST_H.value -> NMO_1ST_H
                NMO_1ST_C.value -> NMO_1ST_C
                NMO_2ND_H.value -> NMO_2ND_H
                NMO_2ND_C.value -> NMO_2ND_C
                FIL.value -> FIL
                MODW_1ST_H.value -> MODW_1ST_H
                MODW_1ST_C.value -> MODW_1ST_C
                MODW_2ND_H.value -> MODW_2ND_H
                MODW_2ND_C.value -> MODW_2ND_C
                S_RAFO_1ST_W_H.value -> S_RAFO_1ST_W_H
                S_RAFO_1ST_W_C.value -> S_RAFO_1ST_W_C
                S_RAFO_2ND_W_H.value -> S_RAFO_2ND_W_H
                S_RAFO_2ND_W_C.value -> S_RAFO_2ND_W_C
                S_MR_1ST_H.value -> S_MR_1ST_H
                S_MR_1ST_C.value -> S_MR_1ST_C
                S_MR_2ND_H.value -> S_MR_2ND_H
                S_MR_2ND_C.value -> S_MR_2ND_C
                NMO_H.value -> NMO_H
                NMO_C.value -> NMO_C
                S_MR_H.value -> S_MR_H
                S_MR_C.value -> S_MR_C
                MOD_W_H.value -> MOD_W_H
                MOD_W_C.value -> MOD_W_C
                OU_2_WAY_1ST_C.value -> OU_2_WAY_1ST_C
                OU_2_WAY_1ST_H.value -> OU_2_WAY_1ST_H
                BOTTOM_NAVIGATION.value -> BOTTOM_NAVIGATION
                NO_DATA.value -> NO_DATA
                else -> UNCHECK
            }
        }
    }
}